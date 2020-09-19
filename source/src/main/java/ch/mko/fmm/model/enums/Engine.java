package main.java.ch.mko.fmm.model.enums;

import main.java.ch.mko.fmm.i18n.I18NLocale;

public enum Engine {
	ENGINE_50,
	ENGINE_100,
	ENGINE_150;

	@Override
	public String toString() {
		return I18NLocale.getString(name().toLowerCase());
	}
}
