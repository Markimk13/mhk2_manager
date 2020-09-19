package main.java.ch.mko.fmm.model.score;

import java.util.Arrays;

public abstract class SettingsItem {
	
	protected abstract byte[] getHashBytes();

	protected abstract byte[] getHashBytesContent();
	
	public boolean isValid() {
		return Arrays.equals(getHashBytes(), getHashBytesContent());
	}
	
	public abstract String getTooltipText(boolean showDetails, int roundIdx, int characterOrdinal, int race);
}
