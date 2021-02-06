package uk.org.mattford.scoutlink.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Settings {
	
	private final SharedPreferences prefs;

	public Settings(Context context) {
		this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public String getString(String key) {
		return this.getString(key, "");
	}
	
	public String getString(String key, String defValue) {
		String str = prefs.getString(key, defValue);
        if ("".equals(str)) {
            return defValue; // Return the default value is the string is blank as opposed to null
        }
        return str;
	}
	
	public Boolean getBoolean(String key) {
		return getBoolean(key, false);
	}
	
	public Boolean getBoolean(String key, Boolean defValue) {
		return prefs.getBoolean(key, defValue);
	}
	
	public void putString(String key, String value) {
		prefs.edit().putString(key, value).apply();
	}
	
	public void putBoolean(String key, Boolean value) {
		prefs.edit().putBoolean(key, value).apply();
	}

    public void putStringArrayList(String key, ArrayList<String> value) {
		prefs.edit().putStringSet(key, new HashSet<>(value)).apply();
    }

	public ArrayList<String> getStringArrayList(String key) {
		Set<String> value = prefs.getStringSet(key, null);
		if (value != null) {
			return new ArrayList<>(value);
		}
		String string = getString(key);
		return new ArrayList<>(Arrays.asList(TextUtils.split(string, ",")));
	}

	public Integer getInteger(String key, Integer defaultValue) {
		return prefs.getInt(key, defaultValue);
	}

	public ArrayList<String> getBlockedUsers() {
		return getStringArrayList("blocked_users");
	}

	public void blockUser(String nickname) {
		ArrayList<String> blockedUsers = getBlockedUsers();
		if (!blockedUsers.contains(nickname)) {
			blockedUsers.add(nickname);
			putStringArrayList("blocked_users", blockedUsers);
		}
	}

	public void unblockUser(String nickname) {
		ArrayList<String> blockedUsers = getBlockedUsers();
		if (blockedUsers.contains(nickname)) {
			blockedUsers.remove(nickname);
			putStringArrayList("blocked_users", blockedUsers);
		}
	}
}
