package main.java.ch.mko.fmm.model.score;

import java.util.ArrayList;
import java.util.List;

public class Highscore implements Comparable<Highscore> {
	
	public static final int NOT_SPECIFIED = -1;
	
	private final HighscoreItem m_highscoreItem;

	private final int m_round;
	
	private final int m_championshipCharacterIdx;
	
	private final int m_race;
	
	public Highscore(HighscoreItem highscoreItem, int round, int characterIdx, int race) {
		m_highscoreItem = highscoreItem;
		m_round = round;
		m_championshipCharacterIdx = characterIdx;
		m_race = race;
	}
	
	public Highscore(TrackTime trackTime, int round) {
		this(trackTime, round, NOT_SPECIFIED, NOT_SPECIFIED);
	}
	
	public static List<Highscore> getHighscoresForRounds(TrackTime trackTime, int restrictToRound, int race) {
		List<Highscore> highscoreList = new ArrayList<>();
		
		if (restrictToRound != 0) {
			highscoreList.add(new Highscore(trackTime, restrictToRound != -1 ? restrictToRound-1 : -1, NOT_SPECIFIED, race));
		} else {
			for (int i = 0; i < 3; i++) {
				highscoreList.add(new Highscore(trackTime, i, NOT_SPECIFIED, race));
			}
		}
		
		return highscoreList;
	}

	public HighscoreItem getHighscoreItem() {
		return m_highscoreItem;
	}

	public int getRound() {
		return m_round;
	}
	
	public String[] getProperties() {
		if (m_highscoreItem instanceof TrackTime) {
			TrackTime trackTime = (TrackTime) m_highscoreItem;
			String player = trackTime.isFromChampionship() ? trackTime.getPlayer().toString() : trackTime.getPlayer().getPlayerName();
			String[] properties = m_round != -1 ? new String[] {null, trackTime.getLastModifiedString(), trackTime.getRoundString(m_round),
					"" + (m_round+1), trackTime.getCharacter().toString(), player, trackTime.getVersion().getName()}
					: new String[] {null, trackTime.getLastModifiedString(), trackTime.getTimeString(),
							trackTime.getRoundString(0), trackTime.getRoundString(1), trackTime.getRoundString(2), trackTime.getCharacter().toString(), player, trackTime.getVersion().getName()};
			return properties;
			
		} else {
			Championship championship = (Championship) m_highscoreItem;
			int[] orderedIdx = championship.getLevelsOrderIndices();
			return new String[] {
					null, TrackTime.getDateString(championship.getLastModified()),
					TrackTime.getTimeString(championship.getTotalTime(m_championshipCharacterIdx)),
					TrackTime.getTimeString(championship.getTime(orderedIdx[0], m_championshipCharacterIdx)),
					TrackTime.getTimeString(championship.getTime(orderedIdx[1], m_championshipCharacterIdx)),
					TrackTime.getTimeString(championship.getTime(orderedIdx[2], m_championshipCharacterIdx)),
					TrackTime.getTimeString(championship.getTime(orderedIdx[3], m_championshipCharacterIdx)),
					"" + championship.getTotalPoints(m_championshipCharacterIdx),
					championship.getCharacter(m_championshipCharacterIdx).toString(),
					championship.getPlayer(m_championshipCharacterIdx).toString(),
					championship.getVersion().getName()
			};
		}
	}
	
	public float getTimeValue() {
		if (m_highscoreItem instanceof TrackTime) {
			return m_round != -1 ? ((TrackTime) m_highscoreItem).getRound(m_round) : ((TrackTime) m_highscoreItem).getTime();
		} else {
			return ((Championship) m_highscoreItem).getTotalTime(m_championshipCharacterIdx);
		}
	}

	@Override
	public int compareTo(Highscore h) {
		int result = Float.compare(getTimeValue(), h.getTimeValue());
		if (result != 0) {
			return result;
		} else {
			return Long.compare(m_highscoreItem.getLastModified(), h.getHighscoreItem().getLastModified());
		}
	}
	
	public static int compareTrackTimes(TrackTime t1, TrackTime t2) {
		return new Highscore(t1, -1).compareTo(new Highscore(t2, -1));
	}
	
	public String getTooltipText(boolean showDetails) {
		int characterOrdinal = m_highscoreItem instanceof TrackTime ?
				((TrackTime) m_highscoreItem).getCharacter().ordinal() :
				((Championship) m_highscoreItem).getCharacter(m_championshipCharacterIdx).ordinal();
		return m_highscoreItem.getTooltipText(showDetails, m_round, characterOrdinal, m_race);
	}
}
