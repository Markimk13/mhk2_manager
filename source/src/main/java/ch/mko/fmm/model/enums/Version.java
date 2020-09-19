package main.java.ch.mko.fmm.model.enums;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import crc.CRC96;
import main.java.ch.mko.fmm.MainFrame;

public enum Version {
	V1_0(1, 0),
	V1_1(1, 1);
	
	private static Map<String, Version> values = new HashMap<>();
	static {
		for (Version version : Version.values()) {
			values.put(version.getName(), version);
		}
	}
	
	private static Version gameVersion;
	
	private final String m_name;

	private static final int MHK2_00_V1_0_LENGTH = 144067648;
	private static final int MHK2_00_V1_1_LENGTH = 144124736;
	private static final int MHK2_01_V1_1_LENGTH = 942400;
	
	public static final boolean IS_V1_1_CONTAINED_IN_V1_0 =
			Paths.get("data", "mhk2-00.dat").toFile().length() == MHK2_00_V1_1_LENGTH;
	public static final String IS_V1_1_CONTAINED_MESSAGE =
			"V1.0 cannot be selected because V1.1 is contained in file of V1.0";
	
	private Version(int major, int minor) {
		m_name = "V" + major + "." + minor;
	}
	
	public static Version getVersion(String name) {
		return values.get(name);
	}
	
	public static Version getGameVersion() {
		return Version.gameVersion;
	}
	
	public static void updateGameVersion() throws IOException {
		gameVersion = getCurrentVersion();
	}
	
	public static void resetGameVersion() {
		gameVersion = null;
	}
	
	public static boolean isGameActive() {
		return gameVersion != null;
	}
	
	public static Version getCurrentVersion() throws IOException {
		Path path = Paths.get("data", "mhk2-00.dat");
		if (!path.toFile().exists()) {
			throw new IllegalStateException("File " + path.toString() + " does not exist!"
					+ " Make sure to put the 'mhk2_manager.jar' in the same folder as the exe-file which opens the game."
					+ " See the help section (File -> Help) if you need support.");
		}
		
		boolean isV1_0 = path.toFile().length() == MHK2_00_V1_0_LENGTH;
		boolean isV1_1 = path.toFile().length() == MHK2_00_V1_1_LENGTH;
		if (!isV1_0 && !isV1_1) {
			throw new IllegalStateException("File " + path.toString() + " is invalid!"
					+ " Please only use the default files. See the help section (File -> Help)"
					+ " if you need support.");
		}

		Path pathV1_1 = Paths.get("data", "mhk2-01.dat");
		if (pathV1_1.toFile().exists()) {
			if (isV1_1) {
				throw new IllegalStateException("File " + pathV1_1.toString() + " may not exist!");	
			} else { // isV1_0 == true in this case
				isV1_0 = false;
				isV1_1 = true;
				byte[] hashBytes = new byte[] { 3, 60, -56, -115, 56, -18, 77, 74, 73, -53, 71, -8 };
				if (pathV1_1.toFile().length() != MHK2_01_V1_1_LENGTH
						|| !Arrays.equals(CRC96.fromBytes(Files.readAllBytes(pathV1_1)).getValue(), hashBytes)) {
					throw new IllegalStateException("File " + pathV1_1.toString() + " is invalid!"
							+ " Please only use the default files. See the help section (File -> Help)"
							+ " if you need support.");
				}
			}
		}

		Path pathFurther = Paths.get("data", "mhk2-02.dat");
		if (pathFurther.toFile().exists()) {
			throw new IllegalStateException("File " + pathFurther.toString() + " may not exist!");
		}
		
		return isV1_1 ? V1_1 : V1_0;
	}
	
	public static void setCurrentVersion(Version version) {
		Path pathV1_1 = Paths.get("data", "mhk2-01.dat");
		Path sourcePath = Paths.get("data", "source", "mhk2-01.dat");
		boolean isV1_1 = pathV1_1.toFile().exists();
		
		if (version == V1_0 && IS_V1_1_CONTAINED_IN_V1_0) {
			throw new IllegalStateException(IS_V1_1_CONTAINED_MESSAGE + "!");
		}
		
		try {
			if (version == V1_0 && isV1_1) {
				Files.delete(pathV1_1);
				
			} else if (version == V1_1 && !isV1_1 && !IS_V1_1_CONTAINED_IN_V1_0){
				Files.copy(sourcePath, pathV1_1);
			}
		} catch (IOException e) {
			e.printStackTrace();
			MainFrame.LOG_PANEL.error("Could not write version: " + e.getMessage(), e);
		}
	}
	
	public String getName() {
		return m_name;
	}
	
	@Override
	public String toString() {
		return m_name;
	}
}
