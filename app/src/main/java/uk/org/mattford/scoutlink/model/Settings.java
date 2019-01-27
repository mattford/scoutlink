package uk.org.mattford.scoutlink.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;

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
		String str =  prefs.getString(key, defValue);
        if ("".equals(str)) {
            return defValue; // Return the default value is the string is blank as opposed to null
        } else {
            return str;
        }
	}
	
	public Boolean getBoolean(String key) {
		return getBoolean(key, false);
	}
	
	public Boolean getBoolean(String key, Boolean defValue) {
		return prefs.getBoolean(key, defValue);
	}
	
	public void putString(String key, String value) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.apply();
	}
	
	public void putBoolean(String key, Boolean value) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		editor.apply();
	}

    public void putStringArray(String key, String[] value) {
		String joinedString = TextUtils.join(",", value);
        putString(key, joinedString);
    }

    public void putStringArrayList(String key, ArrayList<String> value) {
        String[] strArr = new String[value.size()];
        strArr = value.toArray(strArr);
        putStringArray(key, strArr);
    }

	public ArrayList<String> getStringArrayList(String key) {
		String string = getString(key);
		return new ArrayList<>(Arrays.asList(TextUtils.split(string, ",")));
	}

    public String[] getStringArray(String key) {
        String string = getString(key);
		return string.split(",");
    }

	public void putInteger(String key, Integer value) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(key, value);
		editor.apply();
	}

	public Integer getInteger(String key, Integer defaultValue) {
		return prefs.getInt(key, defaultValue);
	}
	
}
