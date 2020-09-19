package main.java.ch.mko.fmm.model.score;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import main.java.ch.mko.fmm.Application;
import main.java.ch.mko.fmm.model.enums.Version;

public class Settings {
	
	public static final String SETTINGS_DIR = ".";
	
	public static final String SETTINGS_PATH = "settings.dat";
	
	public static enum SettingsType {
		TIME_TRIAL(1),
		CHAMPIONSHIP(7),
		DUEL_2_PLAYER(2),
		CHAMPIONSHIP_2_PLAYER(7),
		EMTPY(-1),
		UNKNOWN(-1);
		
		private static final int NUM_MODES = 4;
		
		private int m_neededTimesCount;
		
		private SettingsType(int neededTimesCount) {
			m_neededTimesCount = neededTimesCount;
		}
		
		private int getNeededTimesCount() {
			return m_neededTimesCount;
		}
	}
	
	private final String m_inputFilePath;
	
	private final boolean m_isDefaultSettingsFile;
	
	private SettingsType m_type;
	
	private byte[] m_content;
	
	private long m_lastModified;
	
	private byte[] m_frontContent;
	
	private String m_version;
	
	private String m_playerName;
	
	private String m_playerName2;
	
	private byte[] m_hashBytes;
	
	private int m_currentCharacter;
	
	public Settings(String inputFilePath, boolean isDefaultSettingsFile) throws IOException {
		if (isValidSettingsFile(inputFilePath)) {
			m_inputFilePath = inputFilePath;
			m_isDefaultSettingsFile = isDefaultSettingsFile;
			if (new File(m_inputFilePath).exists()) {
				update();	
			}
		} else {
			throw new IOException("Invalid settings path!");
		}
	}
	
	public static boolean isValidSettingsFile(String path) {
		return (path.equals(SETTINGS_PATH) || new File(path).exists()) && path.endsWith(".dat");
	}
	
	public String getInputFilePath() {
		return m_inputFilePath;
	}
	
	public SettingsType getType() {
		return m_type;
	}
	
	public byte[] getContent() {
		return m_content;
	}
	
	public long getLastModified() {
		return m_lastModified;
	}
	
	public byte[] getFrontContent() {
		return m_frontContent;
	}
	
	public String getVersion() {
		return m_version;
	}
	
	public String getPlayerName() {
		return m_playerName;
	}
	
	public String getPlayerName2() {
		return m_playerName2;
	}
	
	public byte[] getHashBytes() {
		return m_hashBytes;
	}
	
	public int getCurrentCharacter() {
		return m_currentCharacter;
	}
	
	public boolean hasTimeChanged(byte[] content) {
		if (m_content != null && content != null) {
			return !Arrays.equals(Arrays.copyOfRange(m_content, 112, 224), Arrays.copyOfRange(content, 112, 224));
		} else {
			return m_content != content;
		}
	}
	
	public boolean update() throws IOException {
		File file = new File(m_inputFilePath);
		long newLastModified = file.lastModified();
		byte[] settingsContent = Files.readAllBytes(file.toPath());
		if (settingsContent.length == 0) {
			return false;
		}
		
		byte[] content = m_isDefaultSettingsFile ? settingsContent :
				Arrays.copyOfRange(settingsContent, 48, settingsContent.length);
		
		m_currentCharacter = content[84];
		
		boolean changedAndHasContent = m_lastModified != newLastModified && hasTimeChanged(content);
		if (changedAndHasContent) {
			if (Application.isDebug() && m_inputFilePath.equals(SETTINGS_PATH)) {
				File debugDir = new File("debug");
				if (!debugDir.isDirectory()) {
					Files.createDirectory(debugDir.toPath());
				}
				Files.write(Paths.get(debugDir.getPath(), "settings-" + debugDir.listFiles().length + ".dat"), content);
			}
			
			m_content = content;
			m_lastModified = newLastModified;
			if (m_isDefaultSettingsFile) {
				m_version = Version.getGameVersion() != null ? Version.getGameVersion().getName() : "";
				HighscoreSettings settings = HighscoreSettings.loadSettings();
				m_playerName = settings.getCurrentName();
				m_playerName2 = settings.getCurrentName2();
				m_hashBytes = new byte[12];
			} else {
				m_version = new String(Arrays.copyOfRange(settingsContent, 0, 4));
				m_playerName = new String(Arrays.copyOfRange(settingsContent, 8, 20)).trim();
				m_playerName2 = new String(Arrays.copyOfRange(settingsContent, 20, 32)).trim();
				m_hashBytes = Arrays.copyOfRange(settingsContent, 32, 44);
			}
			m_frontContent = new byte[32];
			byte[] versionBytes = m_version.getBytes();
			for (int i = 0; i < versionBytes.length; i++) {
				m_frontContent[i] = versionBytes[i];
			}
			byte[] playerNameBytes = m_playerName.getBytes();
			for (int i = 0; i < 12; i++) {
				m_frontContent[8+i] =  i < playerNameBytes.length ? playerNameBytes[i] : 0;
			}
			byte[] playerName2Bytes = m_playerName2.getBytes();
			for (int i = 0; i < 12; i++) {
				m_frontContent[20+i] = i < playerName2Bytes.length ? playerName2Bytes[i] : 0;
			}
			
			int timesCount = 0;
			for (int i = 0; i < 7; i++) {
				float time = ByteBuffer.wrap(Arrays.copyOfRange(m_content, 112+4*i, 116+4*i)).order(ByteOrder.LITTLE_ENDIAN).getFloat();
				if (time != 0) {
					timesCount++;
				}
			}
			
			int mode = m_content[24];
			if (timesCount == 0) {
				m_type = SettingsType.EMTPY;
			} else {
				SettingsType type = mode >= 0 && mode < SettingsType.NUM_MODES ?
						SettingsType.values()[mode] : SettingsType.UNKNOWN;
				m_type = timesCount == type.getNeededTimesCount() ? type : SettingsType.UNKNOWN;
			}
		}
		
		return changedAndHasContent;
	}
}
