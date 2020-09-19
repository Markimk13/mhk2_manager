package main.java.ch.mko.fmm.model.score;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import crc.CRC96;
import main.java.ch.mko.fmm.i18n.I18N;
import main.java.ch.mko.fmm.i18n.I18NLocale;
import main.java.ch.mko.fmm.model.enums.GameCharacter;
import main.java.ch.mko.fmm.model.enums.Level;
import main.java.ch.mko.fmm.model.enums.Version;
import main.java.ch.mko.fmm.model.score.Player.PlayerOrigin;
import main.java.ch.mko.fmm.model.score.Player.PlayerType;
import main.java.ch.mko.fmm.model.score.Settings.SettingsType;
import main.java.ch.mko.fmm.util.FileUtils;

public class Championship extends SettingsItem implements HighscoreItem {

	public static final String CHAMPIONSHIP_DIR = "data" + File.separator + "Championship";
	
	public static final Level[] CHAMPIONSHIP_1 = Arrays.copyOf(Level.values(), 4);
	
	public static final Level[] CHAMPIONSHIP_2 = Arrays.copyOfRange(Level.values(), 4, 8);
	
	private static final int[] POINTS = new int[] { 10, 8, 6, 4, 2, 1, 0 };
	
	private final Settings m_settings;
	
	private final byte[][] m_content = new byte[][] { null, null, null, null };
	
	private byte[] m_frontContent;
	
	private final TrackTime[][] m_trackTimes = new TrackTime[4][7];
	
	private boolean m_twoPlayerMode;
	
	public Championship(Settings settings) {
		m_settings = settings;	
	}
	
	public static Level[] getOrderedLevelsByName(String champName) {
		return Arrays.stream(champName.split("-"))
			.mapToInt(Integer::parseInt)
			.mapToObj((i) -> Level.values()[i-1])
			.toArray(Level[]::new);
	}
	
	public boolean isTwoPlayerMode() {
		return m_twoPlayerMode;
	}
	
	public String getChampionshipRelativePath() {
		return Paths.get(new File(m_settings.getInputFilePath()).getParentFile().getName(), 
				new File(m_settings.getInputFilePath()).getName()).toString();
	}
	
	public TrackTime getTrackTime(int race, int characterIdx) {
		return m_trackTimes[race][characterIdx];
	}
	
	public Level[] getLevels() {
		return new Level[] {m_trackTimes[0][0].getLevel(), m_trackTimes[1][0].getLevel(), 
				m_trackTimes[2][0].getLevel(), m_trackTimes[3][0].getLevel()};
	}
	
	public int[] getLevelsOrderIndices() {
		int[] levels = new int[4];
		for (int i = 0; i < levels.length; i++) {
			levels[i] = m_trackTimes[i][0].getLevel().ordinal();
		}
		
		int[] indices = new int[4];
		List<Integer> idx = new ArrayList<>();
		idx.add(0);
		idx.add(1);
		idx.add(2);
		idx.add(3);
		for (int i = 0; i < 4; i++) {
			Integer min_index = idx.get(0);
			for (int j = 1; j < idx.size(); j++) {
				if (levels[idx.get(j)] < levels[min_index]) {
					min_index = idx.get(j);
				}
			}
			indices[i] = min_index;
			idx.remove(min_index);
		}
		
		return indices;
	}

	public GameCharacter getCharacter(int characterIdx) {
		return m_trackTimes[0][characterIdx].getCharacter();
	}
	
	public int[] getCharacterIdx() {
		int[] charIdx = new int[7];
		for (int i = 0; i < charIdx.length; i++) {
			for (int j = 0; j < charIdx.length; j++) {
				if (i == m_trackTimes[0][j].getCharacter().ordinal()) {
					charIdx[i] = j;
					break;
				}
			}
		}
		return charIdx;
	}

	public Player getPlayer(int characterIdx) {
		return m_trackTimes[0][characterIdx].getPlayer();
	}
	
	public float getTotalTime(int characterIdx) {
		return getTime(0, characterIdx) + getTime(1, characterIdx) + getTime(2, characterIdx) + getTime(3, characterIdx);
	}

	public float getTime(int race, int characterIdx) {
		return m_trackTimes[race][characterIdx].getTime();
	}
	
	public int getTotalPoints(int characterIdx) {
		return getPoints(0, characterIdx) + getPoints(1, characterIdx) + getPoints(2, characterIdx) + getPoints(3, characterIdx);
	}

	public int getPoints(int race, int characterIdx) {
		float ownTime = m_trackTimes[race][characterIdx].getTime();
		int place = 0;
		for (int i = 0; i < 7; i++) {
			place += m_trackTimes[race][i].getTime() < ownTime ? 1 : 0;
		}
		return POINTS[place];
	}
	
	@Override
	public long getLastModified() {
		return m_trackTimes[0][0].getLastModified();
	}
	
	@Override
	public Version getVersion() {
		return m_trackTimes[0][0].getVersion();
	}
	
	public boolean updateChampionshipInfos(boolean isFinishedChampionshipFile) throws IOException {
		long lastModified = m_settings.getLastModified();

		boolean changed = false;
		if (isFinishedChampionshipFile) {
			for (int i = 0; i < m_content.length; i++) {
				byte[] content = Arrays.copyOfRange(m_settings.getContent(), 224*i, 224*(i+1));
				int race = content[68];
				updateChampionshipInfos(race, m_settings.getFrontContent(), content, lastModified);
			}
		} else {
			byte[] content = Arrays.copyOf(m_settings.getContent(), 224);
			
			changed = true;
			int race = content[68];
			if (race > 0) {
				changed = m_content[race-1] != null && m_settings.hasTimeChanged(m_content[race-1]);
			}
			if (changed) {
				updateChampionshipInfos(race, m_settings.getFrontContent(), content, lastModified);
			}
		}
		
		return changed;
	}
	
	private void updateChampionshipInfos(int race, byte[] frontContent, byte[] raceContent, long lastModified) throws IOException {
		int level = raceContent[36+4*race];
		int engine = raceContent[12];
		m_twoPlayerMode = raceContent[24] == SettingsType.CHAMPIONSHIP_2_PLAYER.ordinal();
		
		for (int i = 0; i < m_trackTimes[race].length; i++) {
			String version = m_settings.getVersion();
			int character = raceContent[84+4*i];
			float time = ByteBuffer.wrap(Arrays.copyOfRange(raceContent, 112+4*i, 116+4*i)).order(ByteOrder.LITTLE_ENDIAN).getFloat();
			float[] rounds = new float[3];
			for (int j = 0; j < rounds.length; j++) {
				rounds[j] = ByteBuffer.wrap(Arrays.copyOfRange(raceContent, 140+12*i+4*j, 144+12*i+4*j)).order(ByteOrder.LITTLE_ENDIAN).getFloat();
			}

			String name = i == 0 ? m_settings.getPlayerName() : 
					m_twoPlayerMode && i == 1 ? m_settings.getPlayerName2() : null;
			Player player = new Player(name, i == 0 ? PlayerType.PLAYER_1 : 
				m_twoPlayerMode && i == 1 ? PlayerType.PLAYER_2 : PlayerType.PC, PlayerOrigin.LOCAL);
			m_trackTimes[race][i] = new TrackTime(this);
			m_trackTimes[race][i].updateInfos(version, level, engine, character, time, rounds, lastModified, player);
		}
		
		for (int i = race+1; i < m_content.length; i++) {
			m_content[i] = null;
		}
		m_content[race] = raceContent;
		m_frontContent = frontContent;
	}
	
	public String getDefaultName() {
		int[] idx = getLevelsOrderIndices();
		return String.format("championship-%d-%02d-%02d-%02d-%02d", m_trackTimes[0][0].getEngine().ordinal()+1,
				m_trackTimes[idx[0]][0].getLevel().ordinal()+1, m_trackTimes[idx[1]][0].getLevel().ordinal()+1,
				m_trackTimes[idx[2]][0].getLevel().ordinal()+1, m_trackTimes[idx[3]][0].getLevel().ordinal()+1);
	}
	
	public String createDefaultBackupPath() throws IOException {
		String backupDir = CHAMPIONSHIP_DIR + File.separator + getDefaultName();
		if (!new File(backupDir).isDirectory()) {
			Files.createDirectory(Paths.get(backupDir));
		}
		
		return FileUtils.getNextPathInFolder(backupDir, "championship-", ".dat");
	}
	
	public boolean isFinished() {
		return m_content[3] != null;
	}
	
	public void saveChampionship(String outputPath) throws IOException {
		if (!isFinished()) {
			throw new IOException("Could not save championship! It isn't finished.");
		}
		
		byte[] completeContent = new byte[48 + 4*224];
		
		for (int i = 0; i < m_frontContent.length; i++) {
			completeContent[i] = m_frontContent[i];
		}

		byte[] hashBytes = getHashBytes();
		for (int i = 0; i < 12; i++) {
			completeContent[32+i] = hashBytes[i];
		}

		byte[] content = getAllRacesContent();
		for (int i = 0; i < content.length; i++) {
			completeContent[48+i] = content[i];
		}
		
		Files.write(Paths.get(outputPath), completeContent);
	}

	private byte[] getAllRacesContent() {
		byte[] content = new byte[4 * 224];
		for (int i = 0; i < m_content.length; i++) {
			for (int j = 0; j < m_content[i].length; j++) {
				content[224*i + j] = m_content[i][j];
			}
		}
		return content;
	}
	
	@Override
	protected byte[] getHashBytes() {
		byte[] part1 = m_frontContent;
		byte[] part2 = getAllRacesContent();
		return CRC96.fromBytes2(part1, part2).getValue();
	}

	@Override
	protected byte[] getHashBytesContent() {
		return m_settings.getHashBytes();
	}
	
	@Override
	public String getTooltipText(boolean showDetails, int roundIdx, int characterOrdinal, int race) {
		String tableStyle = "style=\"border: 1px solid black\"";
		String tableStyleBold = "style=\"border: 2px solid black\"";
		String tdStyle = "style=\"border-bottom: 1px solid black\"";
		String[] boldRows = new String[] { roundIdx != -1 ? I18N.ROUNDS.name() + (roundIdx+1) : I18N.TIME.name() };
		
		Set<Integer> showCharacters = new LinkedHashSet<Integer>();
		showCharacters.add(0);
		if (m_twoPlayerMode) {
			showCharacters.add(1);
		}
		showCharacters.add(getCharacterIdx()[characterOrdinal]);
		
		return "<html>" + TrackTime.getDateTimeString(getLastModified()) + " - " + getChampionshipRelativePath()
				+ (showDetails ? "<br/><br/>"
				+ "<table>" + showCharacters.stream()
						.map(j -> "<tr><td>" + I18NLocale.getString(I18N.PLAYER) + " " + m_trackTimes[0][j].getPlayer() + ":</td></tr><tr>" + IntStream.range(0, 4)
							.mapToObj(i -> {
								boolean fromThisRace = (race == Highscore.NOT_SPECIFIED || race == i)
										&& m_trackTimes[i][j].getCharacter().ordinal() == characterOrdinal;
								return "<td>" + m_trackTimes[i][j].getInfoTable(fromThisRace ? tableStyleBold : tableStyle,
										tdStyle, fromThisRace ? boldRows : null,
										new String[][] { new String[] { I18N.POINTS.name(), "", "" + getPoints(i, j) } }) + "</td>";
							})
							.reduce("", (a, b) -> a + b) + "</tr>")
						.reduce("", (a, b) -> a + b)
				+ "</table>" : "") + "</html>";
	}
}
