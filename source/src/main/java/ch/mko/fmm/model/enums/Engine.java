package main.java.ch.mko.fmm.model.enums;

import main.java.ch.mko.fmm.i18n.I18NLocale;

public enum Engine {
	ENGINE_50,
	ENGINE_100,
	ENGINE_150,
	// added by the patch for MHK2-XXL.exe (class 3, 4, 5; phantom files phantom-4/5/6-NN.mhk2)
	ENGINE_200,
	ENGINE_250,
	ENGINE_TUNING;

	/** The game ships source phantoms (data\Phantom\source) only for the three stock classes. */
	public boolean hasSourcePhantoms() {
		return ordinal() <= ENGINE_150.ordinal();
	}

	@Override
	public String toString() {
		return I18NLocale.getString(name().toLowerCase());
	}
}
