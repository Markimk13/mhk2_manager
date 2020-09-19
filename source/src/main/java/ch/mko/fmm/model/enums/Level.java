package main.java.ch.mko.fmm.model.enums;

import main.java.ch.mko.fmm.i18n.I18NLocale;

public enum Level {
	MOORHUHN_X,
	WINTER,
	ISLAND,
	EGYPT,
	FACTORY,
	MINE,
	CASTLE,
	SWAMP;

	@Override
	public String toString() {
		return I18NLocale.getString(name().toLowerCase());
	}
}
