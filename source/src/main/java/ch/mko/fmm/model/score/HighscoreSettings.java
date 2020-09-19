package main.java.ch.mko.fmm.model.score;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import main.java.ch.mko.fmm.MainFrame;
import main.java.ch.mko.fmm.i18n.I18NLocale;
import main.java.ch.mko.fmm.model.enums.Engine;
import main.java.ch.mko.fmm.model.enums.GameCharacter;
import main.java.ch.mko.fmm.model.enums.Level;
import main.java.ch.mko.fmm.model.enums.Version;
import main.java.ch.mko.fmm.util.FileUtils;
import main.java.ch.mko.fmm.views.CustomPhantom;

public class HighscoreSettings implements Serializable {

	private static final long serialVersionUID = 7578371788716236315L;

	private static final File SETTINGS_FILE = new File("data" + File.separator + "highscore_settings.ser");
	
	private static final File BACKUP_SETTINGS_FILE = new File("data" + File.separator + "highscore_settings_backup.ser");
	
	private static HighscoreSettings settings;

	private boolean m_autoAddHighscores = true;
	
	private boolean m_showDetailsInTooltips = true;
	
	private String m_currentName = "Player";
	
	private String m_currentName2 = "Player 2";
	
	private I18NLocale m_locale = I18NLocale.DEFAULT;
	
	private HighscoreMode m_mode = HighscoreMode.TIME_TRIAL;

	public static enum HighscoreMode {
		TIME_TRIAL,
		CHAMPIONSHIP_TIMES,
		CHAMPIONSHIP,
		DUEL;
		
		@Override
		public String toString() {
			return I18NLocale.getString(name().toLowerCase());
		}
	}
	
	private PhantomMode m_phantomMode = PhantomMode.BEST_TIMES;
	
	private boolean m_onlyCurrentVersion = false;
	
	private boolean m_onlyCurrentCharacter = false;
	
	public static enum PhantomMode {
		BEST_TIMES,
		SOURCE;
		
		public String getName() {
			return I18NLocale.getString(name().toLowerCase());
		}
		
		public String getDescription() {
			return I18NLocale.getString(name().toLowerCase() + "_text");
		}
	}
	
	private boolean m_useCustomPhantoms = true;
	
	private List<CustomPhantom> m_customPhantoms = new ArrayList<>();

	private Level m_level = Level.MOORHUHN_X;
	
	private Level[] m_championshipLevelsOrdered = Championship.CHAMPIONSHIP_1;
	
	private Engine m_engine = Engine.ENGINE_50;
	
	private GameCharacter m_characterFilter = null;
	
	private int m_roundsFilter = -1;
	
	private Version m_versionFilter = null;
	
	private int m_rankLimit = -1;
	
	private Player[] m_playerFilter = Player.HUMAN_PLAYERS;
	
	private HighscoreSettings() {
	}
	
	@SuppressWarnings("unchecked")
	public static HighscoreSettings loadSettings() {
		if (settings == null) {
			settings = new HighscoreSettings();
			if (SETTINGS_FILE.exists()) {
				try {
					ObjectInputStream in = new ObjectInputStream(new FileInputStream(SETTINGS_FILE));
					while (in.available() > 0) {
						String fieldName = in.readUTF();
						try {
							switch (fieldName) {
							case "m_autoAddHighscores":
								settings.m_autoAddHighscores = (Boolean) in.readObject();
								break;
							case "m_showDetailsInTooltips":
								settings.m_showDetailsInTooltips = (Boolean) in.readObject();
								break;
							case "m_currentName":
								settings.m_currentName = in.readUTF();
								break;
							case "m_currentName2":
								settings.m_currentName2 = in.readUTF();
								break;
							case "m_locale":
								settings.m_locale = (I18NLocale) in.readObject();
								break;
							case "m_mode":
								settings.m_mode = (HighscoreMode) in.readObject();
								break;
							case "m_phantomMode":
								settings.m_phantomMode = (PhantomMode) in.readObject();
								break;
							case "m_onlyCurrentVersion":
								settings.m_onlyCurrentVersion = (Boolean) in.readObject();
								break;
							case "m_onlyCurrentCharacter":
								settings.m_onlyCurrentCharacter = (Boolean) in.readObject();
								break;
							case "m_useCustomPhantoms":
								settings.m_useCustomPhantoms = (Boolean) in.readObject();
								break;
							case "m_customPhantoms":
								settings.m_customPhantoms = (List<CustomPhantom>) in.readObject();
								break;
							case "m_level":
								settings.m_level = (Level) in.readObject();
								break;
							case "m_championshipLevelsOrdered":
								settings.m_championshipLevelsOrdered = (Level[]) in.readObject();
								break;
							case "m_engine":
								settings.m_engine = (Engine) in.readObject();
								break;
							case "m_characterFilter":
								settings.m_characterFilter = (GameCharacter) in.readObject();
								break;
							case "m_roundsFilter":
								settings.m_roundsFilter = (Integer) in.readObject();
								break;
							case "m_versionFilter":
								settings.m_versionFilter = (Version) in.readObject();
								break;
							case "m_rankLimit":
								settings.m_rankLimit = (Integer) in.readObject();
								break;
							case "m_playerFilter":
								settings.m_playerFilter = (Player[]) in.readObject();
								break;
							default:
								MainFrame.LOG_PANEL.warn("Could not find settings field " + fieldName);
								createBackupAfterError();
							}
						} catch (Exception e) {
							MainFrame.LOG_PANEL.error("Could not restore settings field " + fieldName + ": " + e.getMessage(), e);
							createBackupAfterError();
						}
					}
					in.close();
				} catch (IOException e) {
					MainFrame.LOG_PANEL.error("Could not restore settings: " + e.getMessage(), e);
					createBackupAfterError();
				}
			}
			settings.loadCustomPhantoms();
			settings.saveSettings();
		}
		
		return settings;
	}
	
	private static synchronized void createBackupAfterError() {
		File backupDir = BACKUP_SETTINGS_FILE.getParentFile();
		String backupName = FileUtils.getUniqueName(backupDir, BACKUP_SETTINGS_FILE.getName());
		Path backupPath = Paths.get(backupDir.getPath(), backupName);
		try {
			Files.copy(SETTINGS_FILE.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING);
			MainFrame.LOG_PANEL.log("Copied settings due to error as backup " + backupPath.toString());
			
		} catch (IOException e) {
			MainFrame.LOG_PANEL.error("Could not copy to backup settings: " + e.getMessage(), e);
		}
	}
	
	private void loadCustomPhantoms() {
		for (CustomPhantom customPhantom : m_customPhantoms) {
			try {
				customPhantom.loadPhantom();
			} catch (IOException e) {
				e.printStackTrace();
				MainFrame.LOG_PANEL.error("Could not load custom phantom "
						+ customPhantom.getInputFilePath() + ": " + e.getMessage(), e);
			}
		}
	}

	private void saveSettings() {
		try {
			if (!SETTINGS_FILE.exists()) {
				Files.createFile(SETTINGS_FILE.toPath());
			}
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SETTINGS_FILE));
			out.writeUTF("m_autoAddHighscores");
			out.writeObject(new Boolean(m_autoAddHighscores));
			out.writeUTF("m_showDetailsInTooltips");
			out.writeObject(new Boolean(m_showDetailsInTooltips));
			out.writeUTF("m_currentName");
			out.writeUTF(m_currentName);
			out.writeUTF("m_currentName2");
			out.writeUTF(m_currentName2);
			out.writeUTF("m_locale");
			out.writeObject(m_locale);
			out.writeUTF("m_mode");
			out.writeObject(m_mode);
			out.writeUTF("m_phantomMode");
			out.writeObject(m_phantomMode);
			out.writeUTF("m_onlyCurrentVersion");
			out.writeObject(new Boolean(m_onlyCurrentVersion));
			out.writeUTF("m_onlyCurrentCharacter");
			out.writeObject(new Boolean(m_onlyCurrentCharacter));
			out.writeUTF("m_useCustomPhantoms");
			out.writeObject(new Boolean(m_useCustomPhantoms));
			out.writeUTF("m_customPhantoms");
			out.writeObject(m_customPhantoms);
			out.writeUTF("m_level");
			out.writeObject(m_level);
			out.writeUTF("m_championshipLevelsOrdered");
			out.writeObject(m_championshipLevelsOrdered);
			out.writeUTF("m_engine");
			out.writeObject(m_engine);
			out.writeUTF("m_characterFilter");
			out.writeObject(m_characterFilter);
			out.writeUTF("m_roundsFilter");
			out.writeObject(new Integer(m_roundsFilter));
			out.writeUTF("m_versionFilter");
			out.writeObject(m_versionFilter);
			out.writeUTF("m_rankLimit");
			out.writeObject(new Integer(m_rankLimit));
			out.writeUTF("m_playerFilter");
			out.writeObject(m_playerFilter);
			out.close();
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
			MainFrame.LOG_PANEL.error("Error while saving settings: " + ioe.getMessage(), ioe);
		}
	}
	
	public boolean isAutoAddHighscores() {
		return m_autoAddHighscores;
	}
	
	public void setAutoAddHighscores(boolean autoAddHighscores) {
		m_autoAddHighscores = autoAddHighscores;
		saveSettings();
	}
	
	public boolean isShowDetailsInTooltips() {
		return m_showDetailsInTooltips;
	}
	
	public void setShowDetailsInTooltips(boolean showDetailsInTooltips) {
		m_showDetailsInTooltips = showDetailsInTooltips;
		saveSettings();
	}
	
	public String getCurrentName() {
		return m_currentName;
	}
	
	public void setCurrentName(String currentName) {
		m_currentName = currentName;
		saveSettings();
	}
	
	public String getCurrentName2() {
		return m_currentName2;
	}
	
	public void setCurrentName2(String currentName2) {
		m_currentName2 = currentName2;
		saveSettings();
	}
	
	public I18NLocale getLocale() {
		return m_locale;
	}
	
	public void setLocale(I18NLocale locale) {
		m_locale = locale;
		I18NLocale.updateLocale();
		saveSettings();
	}
	
	public HighscoreMode getMode() {
		return m_mode;
	}
	
	public PhantomMode getPhantomMode() {
		return m_phantomMode;
	}
	
	public void setPhantomMode(PhantomMode phantomMode) {
		m_phantomMode = phantomMode;
		saveSettings();
	}

	public boolean isOnlyCurrentVersion() {
		return m_onlyCurrentVersion;
	}

	public void setOnlyCurrentVersion(boolean onlyCurrentVersion) {
		m_onlyCurrentVersion = onlyCurrentVersion;
		saveSettings();
	}

	public boolean isOnlyCurrentCharacter() {
		return m_onlyCurrentCharacter;
	}

	public void setOnlyCurrentCharacter(boolean onlyCurrentCharacter) {
		m_onlyCurrentCharacter = onlyCurrentCharacter;
		saveSettings();
	}

	public boolean isUseCustomPhantoms() {
		return m_useCustomPhantoms;
	}

	public void setUseCustomPhantoms(boolean useCustomPhantoms) {
		m_useCustomPhantoms = useCustomPhantoms;
		saveSettings();
	}
	
	public List<CustomPhantom> getCustomPhantoms() {
		return m_customPhantoms.stream()
				.map(cp -> cp.clone())
				.collect(Collectors.toList());
	}
	
	public void setCustomPhantoms(List<CustomPhantom> customPhantoms) {
		m_customPhantoms = customPhantoms.stream()
				.map(cp -> cp.clone())
				.collect(Collectors.toList());
		saveSettings();
		for (File customPhantomFile : new File(Phantom.CUSTOM_DIR).listFiles()) {
			if (!m_customPhantoms.stream()
					.anyMatch(cp -> new File(cp.getInputFilePath()).getName().equals(customPhantomFile.getName()))) {
				try {
					Files.delete(customPhantomFile.toPath());
				} catch (IOException e) {
					e.printStackTrace();
					MainFrame.LOG_PANEL.error("Could not delete non-used custom phantom file "
							+ customPhantomFile.getName() + ": " + e.getMessage(), e);
				}
			}
		}
	}

	public Level getLevel() {
		return m_level;
	}

	public Level[] getChampionshipLevelsOrdered() {
		return m_championshipLevelsOrdered;
	}

	public Engine getEngine() {
		return m_engine;
	}

	public GameCharacter getCharacterFilter() {
		return m_characterFilter;
	}

	public int getRoundsFilter() {
		return m_roundsFilter;
	}
	
	public Player[] getPlayerFilter() {
		return m_playerFilter;
	}
	
	public Version getVersionFilter() {
		return m_versionFilter;
	}
	
	public int getRankLimit() {
		return m_rankLimit;
	}
	
	public void setRankLimit(int rankLimit) {
		if (m_rankLimit != rankLimit) {
			m_rankLimit = rankLimit;
			saveSettings();
		}
	}

	public void setGenerator(HighscoreMode mode, Level level, Level[] championshipLevelsOrdered, Engine engine) {
		if (m_mode != mode || m_level != level || m_engine != engine
				|| !Arrays.deepEquals(m_championshipLevelsOrdered, championshipLevelsOrdered)) {
			m_mode = mode;
			m_level = level;
			m_championshipLevelsOrdered = championshipLevelsOrdered;
			m_engine = engine;
			saveSettings();
		}
	}
	
	public void setFilter(GameCharacter characterFilter, int roundsFilter, Player[] playerFilter, Version versionFilter) {
		if (m_characterFilter != characterFilter || m_roundsFilter != roundsFilter
				|| m_playerFilter != playerFilter || m_versionFilter != versionFilter) {
			m_characterFilter = characterFilter;
			m_roundsFilter = roundsFilter;
			m_playerFilter = playerFilter;
			m_versionFilter = versionFilter;
			saveSettings();
		}
	}
}
