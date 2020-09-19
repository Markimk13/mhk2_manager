package main.java.ch.mko.fmm.model.score;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import main.java.ch.mko.fmm.MainFrame;
import main.java.ch.mko.fmm.i18n.I18N;
import main.java.ch.mko.fmm.i18n.I18NLocale;
import main.java.ch.mko.fmm.model.enums.Engine;
import main.java.ch.mko.fmm.model.enums.GameCharacter;
import main.java.ch.mko.fmm.model.enums.Level;
import main.java.ch.mko.fmm.model.enums.Version;
import main.java.ch.mko.fmm.model.score.HighscoreSettings.HighscoreMode;

public class HighscoreList {
	
	private final HighscoreSettings m_highscoreSettings;

	private final List<HighscoreItem> m_highscoreList = new ArrayList<>();
	
	private Highscore[] m_selectedHighscores;

	public HighscoreList(HighscoreSettings highscoreSettings) {
		m_highscoreSettings = highscoreSettings;
	}
	
	public HighscoreSettings getSettings() {
		return m_highscoreSettings;
	}
	
	public void generateHighscores(HighscoreMode mode, String champName, Level level, Engine engine) {
		File[] highscoreDirs = null;
		Level[] orderedLevels = Championship.getOrderedLevelsByName(champName);
		if (mode == HighscoreMode.TIME_TRIAL) {
			highscoreDirs = new File[] {new File(Phantom.PHANTOM_DIR + File.separator + "phantom-"
					+ (engine.ordinal() + 1) + "-" + String.format("%02d", level.ordinal() + 1))};
		
		} else if (mode == HighscoreMode.CHAMPIONSHIP_TIMES) {
			highscoreDirs = new File(Championship.CHAMPIONSHIP_DIR).listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File file, String name) {
					return name.startsWith("championship-" + (engine.ordinal() + 1))
							&& name.contains("-" + String.format("%02d", level.ordinal() + 1));
				}
			});
		} else if (mode == HighscoreMode.CHAMPIONSHIP) {
			highscoreDirs = new File[] {new File(Championship.CHAMPIONSHIP_DIR + File.separator + "championship-"
					+ (engine.ordinal() + 1) + "-" + champName)};
			
		} else if (mode == HighscoreMode.DUEL) {
			highscoreDirs = new File[] {new File(Duel.DUEL_DIR + File.separator + "duel-"
					+ (engine.ordinal() + 1) + "-" + String.format("%02d", level.ordinal() + 1))};
			
		} else {
			throw new IllegalArgumentException("Invalid highscore mode!");
		}
		
		m_highscoreSettings.setGenerator(mode, level, orderedLevels, engine);
		m_highscoreList.clear();
		
		for (File highscoreDir : highscoreDirs) {
			if (highscoreDir.isDirectory()) {
				File[] highscoreFiles = highscoreDir.listFiles();
				if (mode == HighscoreMode.TIME_TRIAL) {
					for (File highscoreFile : highscoreFiles) {
						try {
							Phantom phantom = new Phantom(highscoreFile.getAbsolutePath());
							if (phantom.isValid()) {
								m_highscoreList.add(phantom.getTrackTime());
							} else {
								MainFrame.LOG_PANEL.warn("Phantom " + highscoreFile.getAbsolutePath() + " is not valid!");
							}
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}
					}
				} else if (mode == HighscoreMode.DUEL) {
					for (File highscoreFile : highscoreFiles) {
						try {
							Duel duel = new Duel(new Settings(highscoreFile.getAbsolutePath(), false));
							duel.updateDuelInfos();
							if (duel.isValid()) {
								m_highscoreList.addAll(Arrays.asList(duel.getTrackTimes()));	
							} else {
								MainFrame.LOG_PANEL.warn("Duel " + highscoreFile.getAbsolutePath() + " is not valid!");
							}
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}
					}
				} else {
					for (File highscoreFile : highscoreFiles) {
						try {
							Championship championship = new Championship(new Settings(highscoreFile.getAbsolutePath(), false));
							championship.updateChampionshipInfos(true);
							if (championship.isValid()) {
								m_highscoreList.add(championship);
							} else {
								MainFrame.LOG_PANEL.warn("Championship " + highscoreFile.getAbsolutePath() + " is not valid!");
							}
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	public void selectHighscores(GameCharacter characterFilter, int roundsFilter, Player[] playerFilter, Version versionFilter) {
		m_highscoreSettings.setFilter(characterFilter, roundsFilter, playerFilter, versionFilter);
		
		List<Highscore> selectedHighscoreList = new ArrayList<>();
		if (m_highscoreSettings.getMode() == HighscoreMode.TIME_TRIAL || m_highscoreSettings.getMode() == HighscoreMode.DUEL) {
			for (HighscoreItem item : m_highscoreList) {
				TrackTime trackTime = (TrackTime) item;
				if (characterFilter == null || trackTime.getCharacter() == characterFilter) {
					if (versionFilter == null || trackTime.getVersion() == versionFilter) {
						selectedHighscoreList.addAll(Highscore.getHighscoresForRounds(trackTime, roundsFilter, Highscore.NOT_SPECIFIED));	
					}
				}
			}
		} else {
			for (HighscoreItem item : m_highscoreList) {
				Championship championship = (Championship) item;
				if (versionFilter == null || championship.getVersion() == versionFilter) {
					int[] charIdx = championship.getCharacterIdx();
					List<Integer> characterIdxList = new ArrayList<>();
					if (characterFilter == null) {
						for (int i = 0; i < 7; i++) {
							if (championship.getPlayer(i).isMember(playerFilter)) {
								characterIdxList.add(i);
							}
						}
					} else {
						int characterIndex = charIdx[characterFilter.ordinal()];
						if (championship.getPlayer(characterIndex).isMember(playerFilter)) {
							characterIdxList.add(characterIndex);
						}
					}
					
					if (m_highscoreSettings.getMode() == HighscoreMode.CHAMPIONSHIP_TIMES) {
						for (int characterIdx : characterIdxList) {
							List<Integer> levelRaces = new ArrayList<>();
							for (int i = 0; i < 4; i++) {
								if (m_highscoreSettings.getLevel() == championship.getLevels()[i]) {
									levelRaces.add(i);
								}
							}
							for (int race : levelRaces) {
								selectedHighscoreList.addAll(Highscore.getHighscoresForRounds(
										championship.getTrackTime(race, characterIdx), roundsFilter, race));
							}
						}
					} else {
						for (int characterIdx : characterIdxList) {
							selectedHighscoreList.add(new Highscore(championship, Highscore.NOT_SPECIFIED, characterIdx, Highscore.NOT_SPECIFIED));
						}
					}
				}
			}
		}
		
		Collections.sort(selectedHighscoreList);
		m_selectedHighscores = selectedHighscoreList.toArray(new Highscore[selectedHighscoreList.size()]);
	}
	
	public Highscore[] getHighscores(int limitToRank) {
		m_highscoreSettings.setRankLimit(limitToRank);
		return limitToRank < m_selectedHighscores.length && limitToRank != -1 ?
				Arrays.copyOf(m_selectedHighscores, limitToRank) : m_selectedHighscores;
	}
	
	public String[] getColumnNames() {
		if (m_highscoreSettings.getMode() == HighscoreMode.CHAMPIONSHIP) {
			Level[] orderedLevels = m_highscoreSettings.getChampionshipLevelsOrdered();
			return new String[] {
					I18NLocale.getString(I18N.RANK),
					I18NLocale.getString(I18N.DATE),
					I18NLocale.getString(I18N.TIME),
					orderedLevels[0].toString(), orderedLevels[1].toString(),
					orderedLevels[2].toString(), orderedLevels[3].toString(),
					I18NLocale.getString(I18N.POINTS),
					I18NLocale.getString(I18N.CHARACTER),
					I18NLocale.getString(I18N.PLAYER),
					I18NLocale.getString(I18N.VERSION)
			};
		} else {
			String[] columnNames = m_highscoreSettings.getRoundsFilter() != -1 ?
					new String[] {
							I18NLocale.getString(I18N.RANK),
							I18NLocale.getString(I18N.DATE),
							I18NLocale.getString(I18N.TIME),
							I18NLocale.getString(I18N.ROUND),
							I18NLocale.getString(I18N.CHARACTER),
							I18NLocale.getString(I18N.PLAYER),
							I18NLocale.getString(I18N.VERSION)
					} :
					new String[] {
							I18NLocale.getString(I18N.RANK),
							I18NLocale.getString(I18N.DATE),
							I18NLocale.getString(I18N.TIME),
							I18NLocale.getString(I18N.ROUND) + " 1",
							I18NLocale.getString(I18N.ROUND) + " 2",
							I18NLocale.getString(I18N.ROUND) + " 3",
							I18NLocale.getString(I18N.CHARACTER),
							I18NLocale.getString(I18N.PLAYER),
							I18NLocale.getString(I18N.VERSION)
					};
			return columnNames;
		}
	}
	
	public int[] getMaximumColumnWidths() {
		if (m_highscoreSettings.getMode() == HighscoreMode.CHAMPIONSHIP) {
			return new int[] {50, Integer.MAX_VALUE, 120, 80, 80, 80, 80, 50, Integer.MAX_VALUE, 115, 50};
			
		} else {
			int[] maxColumnWidths = m_highscoreSettings.getRoundsFilter() != -1 ?
					new int[] {50, Integer.MAX_VALUE, 200, 50, Integer.MAX_VALUE, 115, 50} :
					new int[] {50, Integer.MAX_VALUE, 150, 120, 120, 120, Integer.MAX_VALUE, 115, 50};
			return maxColumnWidths;
		}
	}
}
