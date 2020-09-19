package main.java.ch.mko.fmm.model.score;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import main.java.ch.mko.fmm.i18n.I18N;
import main.java.ch.mko.fmm.i18n.I18NLocale;
import main.java.ch.mko.fmm.model.enums.Engine;
import main.java.ch.mko.fmm.model.enums.GameCharacter;
import main.java.ch.mko.fmm.model.enums.Level;
import main.java.ch.mko.fmm.model.enums.Version;

public class TrackTime implements HighscoreItem {
	
	private SettingsItem m_source;
	
	private String m_version;
	
	private int m_level;
	
	private int m_engine;
	
	private int m_character;
	
	private float m_time;
	
	private final float[] m_rounds = new float[3];
	
	private long m_lastModified;
	
	private Player m_player;

	private final boolean m_fromChampionship;
	
	public TrackTime(SettingsItem source) {
		m_source = source;
		m_fromChampionship = !(source instanceof Phantom);
	}
	
	protected void updateInfos(String version, int level, int engine, int character, float time, float[] rounds, long lastModified, Player player) {
		m_version = version;
		m_level = level;
		m_engine = engine;
		m_character = character;
		m_time = time;
		for (int i = 0; i < m_rounds.length; i++) {
			m_rounds[i] = rounds[i];
		}
		m_lastModified = lastModified;
		m_player = player;
	}
	
	public SettingsItem getSource() {
		return m_source;
	}
	
	@Override
	public Version getVersion() {
		return Version.getVersion(m_version);
	}
	
	public Level getLevel() {
		return Level.values()[m_level];
	}
	
	public Engine getEngine() {
		return Engine.values()[m_engine];
	}
	
	public GameCharacter getCharacter() {
		return GameCharacter.values()[m_character];
	}

	public float getTime() {
		return m_time;
	}

	public String getTimeString() {
		return getTimeString(m_time);
	}
	
	public float getRound(int round) {
		return m_rounds[round];
	}
	
	public String getRoundString(int round) {
		return getTimeString(m_rounds[round]);
	}
	
	@Override
	public long getLastModified() {
		return m_lastModified;
	}
	
	public String getLastModifiedString() {
		return getDateString(m_lastModified);
	}
	
	public boolean isFromChampionship() {
		return m_fromChampionship;
	}
	
	public Player getPlayer() {
		return m_player;
	}
	
	public String[] getLevelInfo() {
		return new String[] { I18N.LEVEL.name(), "" + (m_level+1), Level.values()[m_level].toString() };
	}
	
	public String[] getEngineInfo() {
		return new String[] { I18N.ENGINE.name(), "" + (m_engine+1), Engine.values()[m_engine].toString() };
	}
	
	public String[] getCharacterInfo() {
		return new String[] { I18N.CHARACTER.name(), "" + (m_character+1), GameCharacter.values()[m_character].toString() };
	}
	
	public String[] getTimeInfo() {
		return new String[] { I18N.TIME.name(), "", getTimeString(m_time) };
	}
	
	public String[] getRoundsInfo() {
		return new String[] { I18N.ROUNDS.name(), "1", getTimeString(m_rounds[0]),
				"2", getTimeString(m_rounds[1]), "3", getTimeString(m_rounds[2]) };
	}
	
	public String[][] getInfo() {
		return new String[][] { getLevelInfo(), getEngineInfo(), getCharacterInfo(), getTimeInfo(), getRoundsInfo() };
	}
	
	public String getInfoTable(String tableStyle, String tdStyle, String[] boldRows, String[][] additionalRows) {
		final List<String> boldRowsList = boldRows != null ? Arrays.asList(boldRows) : new ArrayList<>();
		additionalRows = additionalRows != null ? additionalRows : new String[0][];
		
		BiFunction<String, String, String> makeRowBold = (key, value) -> {
			return boldRowsList.contains(key) ? "<b>" + value + "</b>" : value;
		};
		String tdStart = "<td " + tdStyle + ">";
		return "<table " + tableStyle + ">" + Stream.concat(Arrays.stream(getInfo()), Arrays.stream(additionalRows))
				.map(info -> IntStream.range(0, (info.length-1)/2)
						.mapToObj(i -> {
							String key = info[0];
							String key_I18N = I18NLocale.getString(key.toLowerCase());
							return "<tr>" + tdStart + (i == 0 ? makeRowBold.apply(key + info[2*i+1], key_I18N): "") + "</td>"
									+ tdStart + makeRowBold.apply(key + info[2*i+1], info[2*i+1]) + "</td>"
									+ tdStart + makeRowBold.apply(key + info[2*i+1], info[2*i+2]) + "</td></tr>";
						})
						.reduce("", (a, b) -> a + b))
				.reduce("", (a, b) -> a + b) + "</table>";
	}
	
	public static String getTimeString(float time) {
		int centiSecs = Math.round(100*time);
		return new SimpleDateFormat("mm:ss").format(1000 * (centiSecs/100)) + "."
				+ String.format("%02d", centiSecs % 100);
	}
	
	public static String getDateString(long date) {
		return new SimpleDateFormat("dd.MM.yyyy").format(date);
	}
	
	public static String getDateTimeString(long date) {
		return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(date);
	}

	public static String getDefaultName(String prefix, int engine, int level) {
		return String.format("%s-%d-%02d", prefix, engine+1, level+1);
	}

	public String getDefaultName(String prefix) {
		return getDefaultName(prefix, m_engine, m_level);
	}

	@Override
	public String getTooltipText(boolean showDetails, int roundIdx, int characterOrdinal, int race) {
		return m_source.getTooltipText(showDetails, roundIdx, characterOrdinal, race);
	}
}
