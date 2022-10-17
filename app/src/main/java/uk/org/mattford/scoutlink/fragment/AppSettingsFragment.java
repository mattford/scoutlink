package uk.org.mattford.scoutlink.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;

import java.util.ArrayList;
import java.util.Set;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.preference.StringListPreference;

public class AppSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        EditTextPreference nickservPasswordPreference = findPreference("nickserv_password");

        if (nickservPasswordPreference != null) {
            nickservPasswordPreference.setOnBindEditTextListener(
                    editText -> editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD));
        }

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof StringListPreference) {
            PreferenceDialogFragmentCompat f = StringListPreferenceFragment.newInstance(preference.getKey(), ((StringListPreference) preference).getFirstChar());
            f.setTargetFragment(this, 0);
            f.show(getParentFragmentManager(), "APP_SETTINGS_DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("notify_list".equalsIgnoreCase(key)) {
            Set<String> value = sharedPreferences.getStringSet(key, null);
            if (value != null) {
                Intent intent = new Intent(getContext(), IRCService.class)
                        .setAction(IRCService.ACTION_SET_NOTIFY_LIST)
                        .putExtra("items", new ArrayList<>(value));
                getActivity().startService(intent);
            }

        }
    }
}
