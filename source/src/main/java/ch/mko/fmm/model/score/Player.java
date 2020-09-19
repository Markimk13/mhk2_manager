package main.java.ch.mko.fmm.model.score;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import main.java.ch.mko.fmm.i18n.I18N;
import main.java.ch.mko.fmm.i18n.I18NLocale;

public class Player implements Serializable {
	
	private static final long serialVersionUID = 4125060672398388518L;

	public static enum PlayerType {
		PLAYER_1("1"), PLAYER_2("2"), PC("PC");
		
		private String m_name;
		
		private PlayerType(String name) {
			m_name = name;
		}
		
		@Override
		public String toString() {
			return m_name;
		}
	}
	
	public static final Player[] HUMAN_PLAYERS = new Player[] { new Player(PlayerType.PLAYER_1), new Player(PlayerType.PLAYER_2) };
	
	public static final HashMap<I18N, Player[]> OPTIONS = new LinkedHashMap<>();
	static {
		OPTIONS.put(I18N.ALL_PLAYER_TYPES, Arrays.stream(PlayerType.values())
				.map(pt -> new Player(pt))
				.toArray(Player[]::new));
		OPTIONS.put(I18N.PLAYER_1, new Player[] { new Player(PlayerType.PLAYER_1) });
		OPTIONS.put(I18N.PLAYER_2, new Player[] { new Player(PlayerType.PLAYER_2) });
		OPTIONS.put(I18N.PC, new Player[] { new Player(PlayerType.PC) });
		// OPTIONS.put("Average players 1+2", HUMAN_PLAYERS);  TODO maybe implement some time
		OPTIONS.put(I18N.HUMAN, HUMAN_PLAYERS);
	}

	public static enum PlayerOrigin {
		CUSTOM,
		LOCAL,
		SOURCE;
		
		@Override
		public String toString() {
			return I18NLocale.getString(name().toLowerCase());
		}
	}
	
	private final String m_playerName;
	
	private final PlayerType m_playerType;
	
	private final PlayerOrigin m_playerOrigin;

	public Player(String playerName, PlayerType playerType, PlayerOrigin playerOrigin) {
		m_playerName = playerName;
		m_playerType = playerType;
		m_playerOrigin = playerOrigin;
	}
	
	public Player(PlayerType playerType) {
		this(null, playerType, null);
	}
	
	public String getPlayerName() {
		return m_playerName;
	}

	public PlayerType getPlayerType() {
		return m_playerType;
	}

	public PlayerOrigin getPlayerOrigin() {
		return m_playerOrigin;
	}
	
	public boolean isSimilar(Player other) {
		return (m_playerName == null || other.getPlayerName() == null || m_playerName.equals(other.getPlayerName()))
				&& (m_playerType == null || other.getPlayerType() == null || m_playerType == other.getPlayerType())
				&& (m_playerOrigin == null || other.getPlayerOrigin() == null || m_playerOrigin == other.getPlayerOrigin());
	}
	
	public boolean isMember(Player[] players) {
		return Arrays.stream(players).anyMatch(p -> p.isSimilar(this));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Player)) {
			return false;
		}
		Player other = (Player) obj;
		boolean namesEqual = m_playerName != null ? m_playerName.equals(other.getPlayerName()) : other.getPlayerName() == null;
		return namesEqual && other.getPlayerType() == m_playerType;
	}
	
	@Override
	public String toString() {
		return m_playerType.toString() + (m_playerName != null && !m_playerName.isEmpty() ? " - " + m_playerName : "");
	}
}
