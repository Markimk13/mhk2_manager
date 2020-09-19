package main.java.ch.mko.fmm;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

import main.java.ch.mko.fmm.i18n.I18N;
import main.java.ch.mko.fmm.i18n.I18NLocale;

public class Application {
	
	private static ResourceBundle messages = ResourceBundle.getBundle(
			Application.class.getPackage().getName() + ".application", Locale.ROOT);
	
	public static boolean isDebug() {
		return new Boolean(messages.getString("debug"));
	}
	
	public static String getName() {
		return messages.getString("name");
	}
	
	public static String getVersion() {
		return messages.getString("version");
	}
	
	public static String getNameWithVersion() {
		return getName() + " " + getVersion(); 
	}
	
	public static LocalDate getVersionDate() {
		return LocalDate.parse(messages.getString("version_date"));
	}
	
	public static String getCredits() {
		return I18NLocale.getString(I18N.CREDITS);
	}
	
	public static String getEmail() {
		return messages.getString("email");
	}
	
	public static String getHelpVideo() {
		return messages.getString("help_video");
	}
	
	public static String getFullCredits() {
		return getName() + " " + getVersion() + " ("
				+ getVersionDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + ")\n\n" + getCredits();
	}
}
