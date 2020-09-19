package main.java.ch.mko.fmm.model.score;

import main.java.ch.mko.fmm.model.enums.Version;

public interface HighscoreItem {

	public long getLastModified();
	
	public Version getVersion();
	
	public String getTooltipText(boolean showDetails, int roundIdx, int characterOrdinal, int race);
}
