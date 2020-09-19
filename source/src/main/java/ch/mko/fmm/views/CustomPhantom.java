package main.java.ch.mko.fmm.views;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import main.java.ch.mko.fmm.MainFrame;
import main.java.ch.mko.fmm.i18n.I18N;
import main.java.ch.mko.fmm.i18n.I18NLocale;
import main.java.ch.mko.fmm.model.enums.Engine;
import main.java.ch.mko.fmm.model.enums.GameCharacter;
import main.java.ch.mko.fmm.model.enums.Level;
import main.java.ch.mko.fmm.model.enums.Version;
import main.java.ch.mko.fmm.model.score.Highscore;
import main.java.ch.mko.fmm.model.score.HighscoreSettings;
import main.java.ch.mko.fmm.model.score.HighscoreSettings.PhantomMode;
import main.java.ch.mko.fmm.model.score.Phantom;
import main.java.ch.mko.fmm.model.score.Player.PlayerOrigin;
import main.java.ch.mko.fmm.model.score.TrackTime;

public class CustomPhantom implements Serializable, Cloneable, Comparable<CustomPhantom> {

	private static final long serialVersionUID = -5485403151459873202L;

	private final String m_inputFilePath;
	
	private transient Phantom m_phantom;
	
	private boolean m_usePhantom;
	
	private VersionFilter m_versionFilter;
	
	public enum VersionFilter {
		ALL_VERSIONS,
		CURRENT_VERSION,
		USE_MAIN_SETTING;
		
		@Override
		public String toString() {
			return I18NLocale.getString(name().toLowerCase());
		}
	}
	
	private CharacterFilter m_characterFilter;
	
	public enum CharacterFilter {
		ALL_CHARACTERS,
		CURRENT_CHARACTER,
		USE_MAIN_SETTING;
		
		@Override
		public String toString() {
			return I18NLocale.getString(name().toLowerCase());
		}
	}

	public CustomPhantom(Phantom phantom) throws IOException {
		this(phantom, VersionFilter.USE_MAIN_SETTING, CharacterFilter.USE_MAIN_SETTING);
	}
	
	private CustomPhantom(Phantom phantom, VersionFilter versionFilter, CharacterFilter characterFilter) throws IOException {
		m_inputFilePath = phantom.getInputFilePath();
		m_phantom = phantom;
		m_usePhantom = true;
		m_versionFilter = versionFilter;
		m_characterFilter = characterFilter;
	}
	
	public String getInputFilePath() {
		return m_inputFilePath;
	}
	
	public Phantom getPhantom() {
		return m_phantom;
	}
	
	public void loadPhantom() throws IOException {
		m_phantom = new Phantom(m_inputFilePath);
	}
	
	public boolean isUsedPhantom() {
		return m_usePhantom;
	}
	
	public boolean isActuallyUsed(boolean useCustomPhantoms, boolean onlyCurrentVersion, Version currentVersion,
			boolean onlyCurrentCharacter, GameCharacter currentCharacter) {
		return useCustomPhantoms && m_usePhantom
				&& (getActualVersionFilter(onlyCurrentVersion) == VersionFilter.ALL_VERSIONS
						|| getPhantom().getTrackTime().getVersion() == currentVersion)
				&& (getActualCharacterFilter(onlyCurrentCharacter) == CharacterFilter.ALL_CHARACTERS
						|| getPhantom().getTrackTime().getCharacter() == currentCharacter);
	}
	
	public boolean isActuallyUsedInList(boolean useCustomPhantoms, List<CustomPhantom> customPhantoms,
			boolean onlyCurrentVersion, Version currentVersion,
			boolean onlyCurrentCharacter, GameCharacter currentCharacter) {
		CustomPhantom usedCustomPhantom = customPhantoms.stream()
				.filter(cp -> getPhantom().getDefaultName().equals(cp.getPhantom().getDefaultName()))
				.filter(cp -> cp.isActuallyUsed(useCustomPhantoms, onlyCurrentVersion, currentVersion,
						onlyCurrentCharacter, currentCharacter))
				.findFirst()
				.orElse(null);
		return usedCustomPhantom == (getPlayerOrigin() == PlayerOrigin.CUSTOM ? this : null);
	}
	
	public void setUsedPhantom(boolean usePhantom) {
		m_usePhantom = usePhantom;
	}
	
	public VersionFilter getVersionFilter() {
		return m_versionFilter;
	}
	
	public VersionFilter getActualVersionFilter(boolean onlyCurrentVersion) {
		return m_versionFilter != VersionFilter.USE_MAIN_SETTING ? m_versionFilter : 
			onlyCurrentVersion ? VersionFilter.CURRENT_VERSION : VersionFilter.ALL_VERSIONS;
	}
	
	public void setVersionFilter(VersionFilter versionFilter) {
		m_versionFilter = versionFilter;
	}
	
	public CharacterFilter getCharacterFilter() {
		return m_characterFilter;
	}
	
	public CharacterFilter getActualCharacterFilter(boolean onlyCurrentCharacter) {
		return m_characterFilter != CharacterFilter.USE_MAIN_SETTING ? m_characterFilter : 
			onlyCurrentCharacter ? CharacterFilter.CURRENT_CHARACTER : CharacterFilter.ALL_CHARACTERS;
	}
	
	public void setCharacterFilter(CharacterFilter characterFilter) {
		m_characterFilter = characterFilter;
	}
	
	public PlayerOrigin getPlayerOrigin() {
		return m_phantom.getTrackTime().getPlayer().getPlayerOrigin();
	}

	public static String[] getColumnNames() {
		return new String[] {
				"Used",
				I18NLocale.getString(I18N.ENGINE), 
				I18NLocale.getString(I18N.LEVEL), 
				I18NLocale.getString(I18N.VERSION), 
				I18NLocale.getString(I18N.CHARACTER),
				I18NLocale.getString(I18N.PLAYER),
				I18NLocale.getString(I18N.PLAYER_ORIGIN),
				I18NLocale.getString(I18N.DATE), 
				I18NLocale.getString(I18N.TIME),
				"Used for ...", 
				I18NLocale.getString(I18N.USED_FOR_VERSIONS),
				I18NLocale.getString(I18N.USED_FOR_CHARACTERS)
		};
	}

	public static int[] getMaximumColumnWidths() {
		return new int[] {40, 50, 70, 50, Integer.MAX_VALUE, 115, 50, Integer.MAX_VALUE, 120, 70, 80, 80};
	}
	
	public Object[] getProperties(boolean useCustomPhantoms, List<CustomPhantom> customPhantoms,
			boolean onlyCurrentVersion, Version currentVersion,
			boolean onlyCurrentCharacter, GameCharacter currentCharacter) {
		TrackTime trackTime = m_phantom.getTrackTime();
		return new Object[] { isActuallyUsedInList(useCustomPhantoms, customPhantoms, onlyCurrentVersion, currentVersion,
						onlyCurrentCharacter, currentCharacter),
				trackTime.getEngine().toString(), trackTime.getLevel().toString(),
				trackTime.getVersion().toString(), trackTime.getCharacter().toString(),
				trackTime.getPlayer().toString(), getPlayerOrigin().toString(),
				TrackTime.getDateString(trackTime.getLastModified()), TrackTime.getTimeString(trackTime.getTime()),
				isUsedPhantom(), m_versionFilter.toString(), m_characterFilter.toString()};
	}

	public static boolean isColumnEditable(int col) {
		return col == 0 || col >= getColumnNames().length - 3;
	}
	
	public String getTooltipText(boolean showDetails) {
		return m_phantom.getTooltipText(showDetails, Highscore.NOT_SPECIFIED, Highscore.NOT_SPECIFIED, Highscore.NOT_SPECIFIED);
	}

	public static List<CustomPhantom> getSelectedPhantoms(HighscoreSettings settings) throws IOException {
		List<CustomPhantom> allPhantoms = getAllPhantoms(settings.getPhantomMode(), settings.isOnlyCurrentVersion(),
				settings.isOnlyCurrentCharacter(), settings.isUseCustomPhantoms(), settings.getCustomPhantoms());

		Version version = Version.getCurrentVersion();
		GameCharacter character = GameCharacter.getCurrentCharacter();
		List<CustomPhantom> selectedPhantoms = allPhantoms.stream()
				.collect(Collectors.groupingBy(cp -> cp.getPhantom().getDefaultName()))
				.entrySet()
				.stream()
				.map(entry -> entry.getValue().stream()
						.filter(cp -> cp.isActuallyUsed(settings.isUseCustomPhantoms(), settings.isOnlyCurrentVersion(), version,
								settings.isOnlyCurrentCharacter(), character))
						.findFirst()
						.orElse(null))
				.collect(Collectors.toList());

		return selectedPhantoms;
	}
	
	public static List<CustomPhantom> getAllPhantoms(PhantomMode phantomMode, boolean onlyCurrentVersion,
			boolean onlyCurrentCharacter, boolean useCurrentPhantoms, List<CustomPhantom> customPhantoms) throws IOException {
		List<CustomPhantom> allPhantoms = useCurrentPhantoms ? new ArrayList<>(customPhantoms) : new ArrayList<>();
		int numLevels = Level.values().length;
		int numEngines = Engine.values().length;
		switch (phantomMode) {
		case BEST_TIMES:
			Version version = Version.getCurrentVersion();
			GameCharacter character = GameCharacter.getCurrentCharacter();
			for (int i = 0; i < numEngines; i++) {
				for (int j = 0; j < numLevels; j++) {
					String defaultName = TrackTime.getDefaultName("phantom", i, j);
					File[] phantomFiles = Paths.get(Phantom.PHANTOM_DIR, defaultName).toFile().listFiles();
					phantomFiles = phantomFiles != null ? phantomFiles : new File[0];
					TrackTime bestPhantomTrackTime = (TrackTime) Arrays.stream(phantomFiles)
							.map(f -> {
								try {
									return new Phantom(f.getAbsolutePath());
								} catch (IOException e) {
									e.printStackTrace();
									return null;
								}
							})
							.filter(p -> p != null
									&& (!onlyCurrentVersion || p.getTrackTime().getVersion() == version)
									&& (!onlyCurrentCharacter || p.getTrackTime().getCharacter() == character))
							.map(p -> new Highscore(p.getTrackTime(), -1))
							.sorted()
							.findFirst()
							.orElseGet(() -> {
								try {
									Phantom p = new Phantom(Paths.get(Phantom.SOURCE_DIR, defaultName + ".mhk2").toString());
									return new Highscore(p.getTrackTime(), -1);
								} catch (IOException e) {
									e.printStackTrace();
									return null;
								}
							})
							.getHighscoreItem();
					Phantom bestPhantom = (Phantom) bestPhantomTrackTime.getSource();
					allPhantoms.add(new CustomPhantom(bestPhantom, VersionFilter.ALL_VERSIONS, CharacterFilter.ALL_CHARACTERS));
				}
			}
			break;
		case SOURCE:
			for (int i = 0; i < numEngines; i++) {
				for (int j = 0; j < numLevels; j++) {
					String defaultName = TrackTime.getDefaultName("phantom", i, j);
					Path sourcePhantomPath = Paths.get(Phantom.SOURCE_DIR, defaultName + ".mhk2");
					Phantom sourcePhantom = new Phantom(sourcePhantomPath.toString());
					allPhantoms.add(new CustomPhantom(sourcePhantom, VersionFilter.ALL_VERSIONS, CharacterFilter.ALL_CHARACTERS));
				}
			}
			break;
		default:
			MainFrame.LOG_PANEL.error("Could not update phantoms due to missing phantom mode!");
			break;
		}
		
		allPhantoms.removeIf(cp -> {
			boolean valid = cp.getPhantom().isValid();
			if (!valid) {
				MainFrame.LOG_PANEL.warn("Custom Phantom " + cp.getInputFilePath() + " is not valid!");	
			}
			return !valid;
		});
		
		Collections.sort(allPhantoms);
		return allPhantoms;
	}
		
	public static void updateShownPhantoms() throws IOException {
		List<CustomPhantom> customPhantoms = getSelectedPhantoms(HighscoreSettings.loadSettings());
		for (CustomPhantom customPhantom : customPhantoms) {
			Phantom phantom = customPhantom.getPhantom();
			Path sourcePhantomPath = Paths.get(phantom.getInputFilePath());
			Path defaultPhantomPath = Paths.get(Phantom.PHANTOM_DIR, phantom.getDefaultName() + ".mhk2");
			Files.copy(sourcePhantomPath, defaultPhantomPath, StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
	@Override
	public CustomPhantom clone() {
		CustomPhantom customPhantom = null;
		
		try {
			customPhantom = new CustomPhantom(m_phantom);
			customPhantom.setVersionFilter(m_versionFilter);
			customPhantom.setCharacterFilter(m_characterFilter);
			customPhantom.setUsedPhantom(m_usePhantom);
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
			MainFrame.LOG_PANEL.error("Could not clone custom phantom: " + ioe.getMessage(), ioe);
		}
		
		return customPhantom;
	}

	@Override
	public int compareTo(CustomPhantom o) {
		TrackTime t = m_phantom.getTrackTime();
		TrackTime ot = o.getPhantom().getTrackTime();
		int result = t.getEngine().compareTo(ot.getEngine());
		if (result == 0) {
			result = t.getLevel().compareTo(ot.getLevel());
		}
		if (result == 0) {
			result = getPlayerOrigin().compareTo(o.getPlayerOrigin());
		}
		if (result == 0) {
			result = -getVersionFilter().compareTo(o.getVersionFilter());
		}
		if (result == 0) {
			result = -getCharacterFilter().compareTo(o.getCharacterFilter());
		}
		
		return result;
	}
}