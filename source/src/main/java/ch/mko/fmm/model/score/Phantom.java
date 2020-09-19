package main.java.ch.mko.fmm.model.score;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import crc.CRC96;
import main.java.ch.mko.fmm.Application;
import main.java.ch.mko.fmm.i18n.I18N;
import main.java.ch.mko.fmm.i18n.I18NLocale;
import main.java.ch.mko.fmm.model.enums.Version;
import main.java.ch.mko.fmm.model.score.Player.PlayerOrigin;
import main.java.ch.mko.fmm.model.score.Player.PlayerType;
import main.java.ch.mko.fmm.util.FileUtils;

public class Phantom extends SettingsItem {

	public static final String PHANTOM_DIR = "data" + File.separator + "Phantom";
	public static final String SOURCE_DIR = PHANTOM_DIR + File.separator + "source";
	public static final String CUSTOM_DIR = PHANTOM_DIR + File.separator + "custom";
	
	public static final String DEFAULT_INPUT_PATH = Phantom.PHANTOM_DIR + File.separator + "new-phantom.mhk2";
	
	private String m_inputFilePath;
	
	private byte[] m_content;
	
	private TrackTime m_trackTime;

	public Phantom(String inputFilePath) throws IOException {
		m_trackTime = new TrackTime(this);
		setInputFilePath(inputFilePath);
	}
	
	public static boolean isValidPhantomFile(String path) {
		return new File(path).isFile() && path.endsWith(".mhk2");
	}
	
	public String getInputFilePath() {
		return m_inputFilePath;
	}
	
	public boolean isInitialized() {
		return m_content != null;
	}

	public boolean setInputFilePath(String inputFilePath) throws IOException {
		if (isValidPhantomFile(inputFilePath)) {
			boolean changed = !inputFilePath.equals(m_inputFilePath);
			m_inputFilePath = inputFilePath;
			changed |= updatePhantomInfos();
			return changed;
			
		} else {
			throw new IOException("Invalid phantom path " + inputFilePath + "!");
		}
	}
	
	public String getPhantomRelativePath() {
		return Paths.get(new File(m_inputFilePath).getParentFile().getName(), new File(m_inputFilePath).getName()).toString();
	}
	
	public TrackTime getTrackTime() {
		return m_trackTime;
	}
	
	public long getLastModified() {
		return m_trackTime.getLastModified();
	}
	
	public boolean hasTimeChanged(byte[] content) {
		if (m_content != null && content != null) {
			return !Arrays.equals(Arrays.copyOfRange(m_content, 88, 104), Arrays.copyOfRange(content, 88, 104));
		} else {
			return m_content != content;
		}
	}
	
	@SuppressWarnings("unused")
	public boolean updatePhantomInfos() throws IOException {
		File file = new File(m_inputFilePath);
		long newLastModified = file.lastModified();
		byte[] content = Files.readAllBytes(file.toPath());
		boolean changed = content.length > 0 && m_trackTime.getLastModified() != newLastModified
				&& hasTimeChanged(content);
		
		if (changed) {
			if (Application.isDebug() && m_inputFilePath.equals(DEFAULT_INPUT_PATH)) {
				File debugDir = new File("debug");
				if (!debugDir.isDirectory()) {
					Files.createDirectory(debugDir.toPath());
				}
				Files.write(Paths.get(debugDir.getPath(), "phantom-" + debugDir.listFiles().length + ".mhk2"), content);
			}
			
			m_content = content;

			String version = new String(Arrays.copyOfRange(m_content, 0, 4));
			String name = new String(Arrays.copyOfRange(m_content, 8, 20)).trim();
			int level = m_content[76] - 1;
			int engine = m_content[80];
			int character = m_content[84];
			
			float time = ByteBuffer.wrap(Arrays.copyOfRange(m_content, 88, 92)).order(ByteOrder.LITTLE_ENDIAN).getFloat();
			float[] rounds = new float[3];
			for (int i = 0; i < rounds.length; i++) {
				rounds[i] = ByteBuffer.wrap(Arrays.copyOfRange(m_content, 92+4*i, 96+4*i)).order(ByteOrder.LITTLE_ENDIAN).getFloat();
			}
			
			String containingFolder = new File(m_inputFilePath).getAbsoluteFile().getParent();
			PlayerOrigin playerOrigin = new File(SOURCE_DIR).getAbsolutePath().equals(containingFolder) ? PlayerOrigin.SOURCE :
				new File(CUSTOM_DIR).getAbsolutePath().equals(containingFolder) ? PlayerOrigin.CUSTOM : PlayerOrigin.LOCAL;
			PlayerType playerType = playerOrigin == PlayerOrigin.SOURCE ? PlayerType.PC : PlayerType.PLAYER_1;
			Player player = new Player(name, playerType, playerOrigin);
			m_trackTime.updateInfos(version, level, engine, character, time, rounds, newLastModified, player);
		}
		
		return changed;
	}
	
	public String getDefaultName() {
		return m_trackTime.getDefaultName("phantom");
	}
	
	public String getDefaultOutputPath() {
		return PHANTOM_DIR + File.separator + getDefaultName() + ".mhk2";
	}
	
	public String getSourcePath() {
		return SOURCE_DIR + File.separator + getDefaultName() + ".mhk2";
	}
	
	public String createDefaultBackupPath() throws IOException {
		String backupDir = PHANTOM_DIR + File.separator + getDefaultName();
		if (!new File(backupDir).isDirectory()) {
			Files.createDirectory(Paths.get(backupDir));
		}
		
		return FileUtils.getNextPathInFolder(backupDir, "trial-", ".mhk2");
	}
	
	public boolean existsPhantom() {
		File backupDir = new File(PHANTOM_DIR + File.separator + getDefaultName());
		boolean exists = backupDir.isDirectory();
		if (exists) {
			exists = false;
			for (File file : backupDir.listFiles()) {
				if (m_trackTime.getLastModified() == file.lastModified()) {
					exists = true;
					break;
				}
			}
		}
		
		return exists;
	}
	
	public void savePhantom(String outputPath) throws IOException {
		Version gameVersion = Version.getGameVersion();
		if (gameVersion == null) {
			throw new IllegalStateException("Phantom cannot be saved because the game "
					+ "wasn't opened by the \"" + I18NLocale.getString(I18N.OPEN_GAME) + "\" button!");
		}
		
		byte[] versionContent = gameVersion.getName().getBytes();
		for (int i = 0; i < 4; i++) {
			m_content[i] = versionContent[i];
		}
		
		String currentName = HighscoreSettings.loadSettings().getCurrentName();
		byte[] currentNameContent = currentName.getBytes();
		for (int i = 0; i < 12; i++) {
			m_content[8+i] = i < currentNameContent.length ? currentNameContent[i] : 0;
		}
		
		Path sourcePath = Paths.get(SOURCE_DIR, getDefaultName() + ".mhk2");
		byte[] sourceContent = Files.readAllBytes(sourcePath);
		for (int i = 40; i < 72; i++) {
			m_content[i] = sourceContent[i];
		}

		byte[] hashBytes = getHashBytes();
		for (int i = 0; i < 12; i++) {
			m_content[20+i] = hashBytes[i];
		}
		
		Files.write(Paths.get(outputPath), m_content);
	}
	
	@Override
	protected byte[] getHashBytes() {
		byte[] part1 = Arrays.copyOfRange(m_content, 0, 20);
		byte[] part2 = Arrays.copyOfRange(m_content, 32, m_content.length);
		return CRC96.fromBytes2(part1, part2).getValue();
	}

	@Override
	protected byte[] getHashBytesContent() {
		return Arrays.copyOfRange(m_content, 20, 32);
	}
	
	public boolean contentEquals(Phantom other) {
		return Arrays.equals(getHashBytesContent(), other.getHashBytesContent());
	}
	
	@Override
	public String getTooltipText(boolean showDetails, int roundIdx, int characterOrdinal, int race) {
		String tableStyle = "style=\"border: 1px solid black\"";
		String tdStyle = "style=\"border-bottom: 1px solid black\"";
		String[] boldRows = new String[] { roundIdx != -1 ? I18N.ROUNDS.name() + (roundIdx+1) : I18N.TIME.name() };
		return "<html>" + TrackTime.getDateTimeString(getLastModified()) + " - " + getPhantomRelativePath()
				+ (showDetails ? "<br/><br/>"
				+ "<table><tr><td>" + I18NLocale.getString(I18N.PLAYER) + " " + m_trackTime.getPlayer() + ":</td></tr><tr><td>"
				+ m_trackTime.getInfoTable(tableStyle, tdStyle, boldRows, null)
				+ "</td></tr></table>" : "") + "</html>";
	}
}
