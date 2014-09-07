package uk.org.mattford.scoutlink.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {
	
	private SharedPreferences prefs;

	public Settings(Context context) {
		this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public SharedPreferences getPrefs() {
		return this.prefs;
	}
	
	public String getString(String key) {
		return this.getString(key, "");
	}
	
	public String getString(String key, String defValue) {
		return this.prefs.getString(key, defValue);
	}
	
	public Boolean getBoolean(String key) {
		return this.getBoolean(key, false);
	}
	
	public Boolean getBoolean(String key, Boolean defValue) {
		return this.prefs.getBoolean(key, defValue);
	}
	
	public void putString(String key, String value) {
		SharedPreferences.Editor editor = this.prefs.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public void putBoolean(String key, Boolean value) {
		SharedPreferences.Editor editor = this.prefs.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	
}
