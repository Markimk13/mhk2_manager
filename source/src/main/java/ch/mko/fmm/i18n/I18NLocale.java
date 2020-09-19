package main.java.ch.mko.fmm.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

import main.java.ch.mko.fmm.model.score.HighscoreSettings;

public enum I18NLocale {
	
	DEFAULT("", "", "Default"),
	de_DE("de", "DE", "DE"),
	en_US("en", "US", "EN");
	
	private static I18NLocale I18N_LOCALE;
	
	private final Locale m_currentLocale;
	
	private final ResourceBundle m_messages;
	
	private final String m_name;
	
	private I18NLocale(String language, String country, String name) {
		m_currentLocale = !language.isEmpty() ? new Locale(language, country) : Locale.getDefault();
        m_messages = ResourceBundle.getBundle(I18NLocale.class.getPackage().getName()
        		+ ".MessagesBundle", m_currentLocale);
        m_name = name;
	}
	
	@Override
	public String toString() {
		return m_name;
	}

	public static String getString(I18N i18n) {
		return getString(I18N_LOCALE, i18n);
	}
	
	public static String getString(String key) {
		return getString(I18N_LOCALE, key);
	}

	public static String getString(I18NLocale locale, I18N i18n) {
		return getString(locale, i18n.getKey());
	}
	
	public static String getString(I18NLocale locale, String key) {
		return locale.m_messages.getString(key);
	}
	
	public static void updateLocale() {
		I18N_LOCALE = HighscoreSettings.loadSettings().getLocale();
		Locale.setDefault(I18N_LOCALE.m_currentLocale);
	}
}