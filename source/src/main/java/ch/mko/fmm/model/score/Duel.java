package main.java.ch.mko.fmm.model.score;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import crc.CRC96;
import main.java.ch.mko.fmm.i18n.I18N;
import main.java.ch.mko.fmm.i18n.I18NLocale;
import main.java.ch.mko.fmm.model.score.Player.PlayerOrigin;
import main.java.ch.mko.fmm.model.score.Player.PlayerType;
import main.java.ch.mko.fmm.util.FileUtils;

public class Duel extends SettingsItem {

	public static final String DUEL_DIR = "data" + File.separator + "Duel";
	
	private final Settings m_settings;
	
	private byte[] m_content;
	
	private byte[] m_frontContent;
	
	private final TrackTime[] m_trackTimes = new TrackTime[2];
	
	public Duel(Settings settings) throws IOException {
		m_settings = settings;
	}
	
	public String getDuelRelativePath() {
		return Paths.get(new File(m_settings.getInputFilePath()).getParentFile().getName(), 
				new File(m_settings.getInputFilePath()).getName()).toString();
	}
	
	public TrackTime[] getTrackTimes() {
		return m_trackTimes;
	}
	
	public long getLastModified() {
		return m_trackTimes[0].getLastModified();
	}
	
	public boolean updateDuelInfos() throws IOException {
		long lastModified = m_settings.getLastModified();

		byte[] content = Arrays.copyOf(m_settings.getContent(), 224);
		boolean changed = m_settings.hasTimeChanged(m_content);
		if (changed) {
			updateDuelInfos(m_settings.getFrontContent(), content, lastModified);
		}
		
		return changed;
	}
	
	private void updateDuelInfos(byte[] frontContent, byte[] content, long lastModified) throws IOException {
		int level = content[28];
		int engine = content[12];
		
		for (int i = 0; i < m_trackTimes.length; i++) {
			String version = m_settings.getVersion();
			int character = content[84+4*i];
			float time = ByteBuffer.wrap(Arrays.copyOfRange(content, 112+4*i, 116+4*i)).order(ByteOrder.LITTLE_ENDIAN).getFloat();
			float[] rounds = new float[3];
			for (int j = 0; j < rounds.length; j++) {
				rounds[j] = ByteBuffer.wrap(Arrays.copyOfRange(content, 140+12*i+4*j, 144+12*i+4*j)).order(ByteOrder.LITTLE_ENDIAN).getFloat();
			}

			String name = i == 0 ? m_settings.getPlayerName() : m_settings.getPlayerName2();
			Player player = new Player(name, i == 0 ? PlayerType.PLAYER_1 : PlayerType.PLAYER_2, PlayerOrigin.LOCAL);
			m_trackTimes[i] = new TrackTime(this);
			m_trackTimes[i].updateInfos(version, level, engine, character, time, rounds, lastModified, player);
		}
		
		m_content = content;
		m_frontContent = frontContent;
	}
	
	public String getDefaultName() {
		return m_trackTimes[0].getDefaultName("duel");
	}
	
	public String createDefaultBackupPath() throws IOException {
		String backupDir = DUEL_DIR + File.separator + getDefaultName();
		if (!new File(backupDir).isDirectory()) {
			Files.createDirectory(Paths.get(backupDir));
		}
		
		return FileUtils.getNextPathInFolder(backupDir, "duel-", ".dat");
	}
	
	public void saveDuel(String outputPath) throws IOException {
		byte[] completeContent = new byte[48 + 224];
		
		for (int i = 0; i < m_frontContent.length; i++) {
			completeContent[i] = m_frontContent[i];
		}

		byte[] hashBytes = getHashBytes();
		for (int i = 0; i < 12; i++) {
			completeContent[32+i] = hashBytes[i];
		}

		for (int i = 0; i < m_content.length; i++) {
			completeContent[48+i] = m_content[i];
		}
		
		Files.write(Paths.get(outputPath), completeContent);
	}

	@Override
	protected byte[] getHashBytes() {
		byte[] part1 = m_frontContent;
		byte[] part2 = m_content;
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
		return "<html>" + TrackTime.getDateTimeString(getLastModified()) + " - " + getDuelRelativePath()
				+ (showDetails ? "<br/><br/>"
				+ "<table>" + Arrays.stream(m_trackTimes)
						.map(t -> {
							boolean fromThisRace = t.getCharacter().ordinal() == characterOrdinal;
							return "<tr><td>" + I18NLocale.getString(I18N.PLAYER) + " " + t.getPlayer() + ":</td></tr><tr><td>" + t.getInfoTable(fromThisRace ? tableStyleBold : tableStyle, tdStyle,
									fromThisRace ? boldRows : null,
									new String[][] { new String[] { I18N.RANK.name(), "", "" 
											+ (Arrays.stream(m_trackTimes).filter(ot -> Highscore.compareTrackTimes(ot, t) < 0).count()+1)
									} }) + "</td></tr>";
						})
						.reduce("", (a, b) -> a + b)
				+ "</table>" : "") + "</html>";
	}
}
