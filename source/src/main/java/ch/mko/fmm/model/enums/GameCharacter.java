package main.java.ch.mko.fmm.model.enums;

import java.io.IOException;

import main.java.ch.mko.fmm.i18n.I18NLocale;
import main.java.ch.mko.fmm.model.score.Settings;

public enum GameCharacter {
	MOORHUHN,
	LESSHUHN,
	MOORFROSCH,
	PUMPKIN,
	SNOWMAN,
	KROET,
	HANK;
	
	public static GameCharacter getCurrentCharacter() throws IOException {
		return GameCharacter.values()[new Settings(Settings.SETTINGS_PATH, true).getCurrentCharacter()];
	}
	
	@Override
	public String toString() {
		return I18NLocale.getString(name().toLowerCase());
	}
}
