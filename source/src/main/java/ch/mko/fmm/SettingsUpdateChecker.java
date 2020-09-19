package main.java.ch.mko.fmm;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import main.java.ch.mko.fmm.i18n.I18N;
import main.java.ch.mko.fmm.i18n.I18NLocale;
import main.java.ch.mko.fmm.model.enums.Version;
import main.java.ch.mko.fmm.model.score.Championship;
import main.java.ch.mko.fmm.model.score.Duel;
import main.java.ch.mko.fmm.model.score.HighscoreSettings;
import main.java.ch.mko.fmm.model.score.HighscoreSettings.PhantomMode;
import main.java.ch.mko.fmm.model.score.Phantom;
import main.java.ch.mko.fmm.model.score.Player.PlayerOrigin;
import main.java.ch.mko.fmm.model.score.Settings;
import main.java.ch.mko.fmm.views.CustomPhantom;
import main.java.ch.mko.fmm.views.HighscorePanel;

public class SettingsUpdateChecker {
	
	private ExecutorService m_updateService;
	
	private final Settings m_settings;
	
	private Phantom m_phantom;
	
	private final Championship m_championship;
	
	private final Duel m_duel;
	
	private final HighscorePanel m_highscorePanel;

	public SettingsUpdateChecker(HighscorePanel highscorePanel) throws IOException {
		m_settings = new Settings(Settings.SETTINGS_PATH, true);
		m_championship = new Championship(m_settings);
		m_duel = new Duel(m_settings);
		m_highscorePanel = highscorePanel;
	}
	
	@SuppressWarnings("unchecked")
	void updateSettingsUpdateCheck() {
		if (m_updateService != null) {
			m_updateService.shutdownNow();
		}
		
		if (HighscoreSettings.loadSettings().isAutoAddHighscores()) {
			m_updateService = Executors.newFixedThreadPool(2);
			m_updateService.execute(() -> {
				MainFrame.LOG_PANEL.log("Started checking for updates in settings.");
				WatchService watcherSettings = null;
				try {
					watcherSettings = FileSystems.getDefault().newWatchService();
				    new File(Settings.SETTINGS_PATH).getAbsoluteFile().getParentFile().toPath().register(watcherSettings,
	                           StandardWatchEventKinds.ENTRY_CREATE,
	                           StandardWatchEventKinds.ENTRY_MODIFY);
				} catch (IOException e) {
				    e.printStackTrace();
				    MainFrame.LOG_PANEL.error("Could not initialize update check for settings: " + e.getMessage(), e);
				}
				try {
					while (true) {
						WatchKey key = watcherSettings.take();
					    for (WatchEvent<?> event : key.pollEvents()) {
					        WatchEvent.Kind<?> kind = event.kind();
					        // Discard overflow events.
					        if (kind == StandardWatchEventKinds.OVERFLOW) {
					            continue;
					        }

					        // The file path is the context of the event.
					        String fileName = ((WatchEvent<Path>) event).context().toFile().getName();
					        boolean isSettingsFile = fileName.equals(new File(Settings.SETTINGS_PATH).getName());
					        if (isSettingsFile) {
						        Path filePath = Paths.get(new File(Settings.SETTINGS_PATH)
						        		.getAbsoluteFile().getParentFile().toPath().toString(), fileName);
					        	if (filePath.toFile().isFile()) {
					        		try {
					        			int currentCharacter = m_settings.getCurrentCharacter();
						        		if (m_settings.update()) {
							        		MainFrame.LOG_PANEL.log("Update in settings found.");
							        		
							        		if (Version.getGameVersion() == null) {
							        			MainFrame.LOG_PANEL.warn("The game wasn't opened by the \""
							        					+ I18NLocale.getString(I18N.OPEN_GAME) + "\" button! "
							        					+ "So if you make new highscores, they cannot be saved!");
							        		}
							        		
						        			switch (m_settings.getType()) {

											case CHAMPIONSHIP:
											case CHAMPIONSHIP_2_PLAYER:
												if (m_championship.updateChampionshipInfos(false)) {
													trySaveChampionship();
												} else {
													MainFrame.LOG_PANEL.log("No new information was found in settings.");
												}
												break;
											
											case DUEL_2_PLAYER:
												if (m_duel.updateDuelInfos()) {
													saveDuel();
												} else {
													MainFrame.LOG_PANEL.log("No new information was found in settings.");
												}
												break;
											
											case EMTPY:
												MainFrame.LOG_PANEL.log("Settings file was reset.");
												break;
												
											case TIME_TRIAL:
												// handled when phantom file changes
												break;
												
											default:
												MainFrame.LOG_PANEL.warn("Settings file has an unknown format!");
												break;
											}
						        		}
						        		
						        		if (currentCharacter != m_settings.getCurrentCharacter()) {
						        			CustomPhantom.updateShownPhantoms();
						        		}
						        		
									} catch (Exception e) {
										e.printStackTrace();
										MainFrame.LOG_PANEL.error("Exception occurred while checking for updates for settings: " + e.getMessage(), e);
									}
					        	}
					        }
					    }

					    // Reset the key -- this step is critical if you want to
					    // receive further watch events.  If the key is no longer valid,
					    // the directory is inaccessible so exit the loop.
					    boolean valid = key.reset();
					    if (!valid) {
					        throw new InterruptedException("Interrupted by invalid watch key for settings file!");
					    }
					}	
				} catch (InterruptedException e) {
					HighscoreSettings settings = HighscoreSettings.loadSettings();
					if (settings.isAutoAddHighscores()) {
						settings.setAutoAddHighscores(false);
						updateSettingsUpdateCheck();
					}
					MainFrame.LOG_PANEL.log("Stopped checking for updates in settings.");
				}
			});
			m_updateService.execute(() -> {
				MainFrame.LOG_PANEL.log("Started checking for updates in phantoms.");
				WatchService watcherPhantom = null;
				try {
					watcherPhantom = FileSystems.getDefault().newWatchService();
				    new File(Phantom.DEFAULT_INPUT_PATH).getAbsoluteFile().getParentFile().toPath().register(watcherPhantom,
	                           StandardWatchEventKinds.ENTRY_CREATE,
	                           StandardWatchEventKinds.ENTRY_MODIFY);
				} catch (IOException e) {
				    e.printStackTrace();
				    MainFrame.LOG_PANEL.error("Could not initialize update check for phantoms: " + e.getMessage(), e);
				}
				try {
					while (true) {
						WatchKey key = watcherPhantom.take();
					    for (WatchEvent<?> event : key.pollEvents()) {
					        WatchEvent.Kind<?> kind = event.kind();
					        // Discard overflow events.
					        if (kind == StandardWatchEventKinds.OVERFLOW) {
					            continue;
					        }

					        // The file path is the context of the event.
					        String fileName = ((WatchEvent<Path>) event).context().toFile().getName();
					        boolean isPhantomFile = fileName.equals(new File(Phantom.DEFAULT_INPUT_PATH).getName());
					        if (isPhantomFile) {
						        Path filePath = Paths.get(new File(Phantom.DEFAULT_INPUT_PATH)
						        		.getAbsoluteFile().getParentFile().toPath().toString(), fileName);
					        	if (filePath.toFile().isFile()) {
					        		try {
						        		MainFrame.LOG_PANEL.log("Update in phantoms found.");
						        		
					        			boolean changed = false;
										if (m_phantom != null) {
											changed = m_phantom.updatePhantomInfos();
										} else {
											m_phantom = new Phantom(Phantom.DEFAULT_INPUT_PATH);
											changed = m_phantom.isInitialized();
										}
										if (changed) {
											try {
												savePhantom();
											} catch (IllegalStateException e) {
												MainFrame.LOG_PANEL.error("Exception when saving phantom: " + e.getMessage());
											}
										} else {
											MainFrame.LOG_PANEL.log("No new information was found in phantoms.");
										}
									} catch (Exception e) {
										e.printStackTrace();
										MainFrame.LOG_PANEL.error("Exception occurred while checking for updates for phantoms: " + e.getMessage(), e);
									}
					        	}
					        }
					    }

					    // Reset the key -- this step is critical if you want to
					    // receive further watch events.  If the key is no longer valid,
					    // the directory is inaccessible so exit the loop.
					    boolean valid = key.reset();
					    if (!valid) {
					        throw new InterruptedException("Interrupted by invalid watch key for phantoms file!");
					    }
					}	
				} catch (InterruptedException e) {
					HighscoreSettings settings = HighscoreSettings.loadSettings();
					if (settings.isAutoAddHighscores()) {
						settings.setAutoAddHighscores(false);
						updateSettingsUpdateCheck();
					}
					MainFrame.LOG_PANEL.log("Stopped checking for updates in phantoms.");
				}
			});
		}
	}
	
	private void savePhantom(Phantom phantom, String outputPath) throws IOException {
		phantom.savePhantom(outputPath);
		MainFrame.LOG_PANEL.log("Successfully saved phantom to " + outputPath + ".");
	}

	private void savePhantom() throws IOException {
		if (!m_phantom.existsPhantom()) {
			String outputPath = m_phantom.getDefaultOutputPath();
			Phantom bestPhantom = null;
			try {
				bestPhantom = new Phantom(outputPath);
			} catch (IOException ioe) {
				bestPhantom = new Phantom(m_phantom.getSourcePath());
			}
			final Phantom bestPhantom2 = bestPhantom;
			Phantom phantom = CustomPhantom.getSelectedPhantoms(HighscoreSettings.loadSettings()).stream()
					.map(cp -> cp.getPhantom())
					.filter(p -> p.getTrackTime().getEngine() == bestPhantom2.getTrackTime().getEngine()
							&& p.getTrackTime().getLevel() == bestPhantom2.getTrackTime().getLevel())
					.findFirst()
					.orElse(null);
			
			String backupPath = m_phantom.createDefaultBackupPath();
			savePhantom(m_phantom, backupPath);
			m_highscorePanel.updateTableData();
			if (phantom == null || (phantom.getTrackTime().getPlayer().getPlayerOrigin() != PlayerOrigin.CUSTOM
					&& m_phantom.getTrackTime().getTime() < bestPhantom.getTrackTime().getTime())) {
				PhantomMode phantomMode = HighscoreSettings.loadSettings().getPhantomMode();
				if (phantomMode == PhantomMode.BEST_TIMES) {
					savePhantom(m_phantom, outputPath);
				} else {
					MainFrame.LOG_PANEL.log("Did not save better time as main phantom due to current phantom mode!");
				}
			} else {
				MainFrame.LOG_PANEL.log("Did not save as main phantom due to no new highscore!");
			}
		}
	}
	
	private void trySaveChampionship() throws IOException {
		if (m_championship.isFinished()) {
			String outputPath = m_championship.createDefaultBackupPath();
			m_championship.saveChampionship(outputPath);
			m_highscorePanel.updateTableData();
			MainFrame.LOG_PANEL.log("Successfully saved championship to " + outputPath + ".");
		} else {
			MainFrame.LOG_PANEL.log("Did not save championship so far. It isn't finished or has already been saved.");
		}
	}
	
	private void saveDuel() throws IOException {
		String outputPath = m_duel.createDefaultBackupPath();
		m_duel.saveDuel(outputPath);
		m_highscorePanel.updateTableData();
		MainFrame.LOG_PANEL.log("Successfully saved duel to " + outputPath + ".");
	}
}
