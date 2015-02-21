package uk.org.mattford.scoutlink.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

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
		editor.apply();
	}
	
	public void putBoolean(String key, Boolean value) {
		SharedPreferences.Editor editor = this.prefs.edit();
		editor.putBoolean(key, value);
		editor.apply();
	}

    public void putStringArray(String key, String[] value) {
        StringBuilder sb = new StringBuilder();
        for (String val : value) {
            sb.append(val).append(",");
        }
        putString(key, sb.toString());
    }

    public void putStringArrayList(String key, ArrayList<String> value) {
        String[] strArr = new String[value.size()];
        strArr = value.toArray(strArr);
        putStringArray(key, strArr);
    }

    public String[] getStringArray(String key) {
        String string = this.getString(key);
        return string.split(",");
    }

	
	
}
