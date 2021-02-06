package uk.org.mattford.scoutlink.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.util.ArrayList;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.preference.StringListPreference;

public class StringListPreferenceFragment extends PreferenceDialogFragmentCompat {
    private static final String SAVE_STATE_TEXT = "StringListPreferenceFragment.strings";

    private ArrayList<String> mStrings;
    private String mFirstChar;
    private ArrayAdapter<String> adapter;

    public static StringListPreferenceFragment newInstance(String key, String firstChar) {
        final StringListPreferenceFragment
                fragment = new StringListPreferenceFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        if (firstChar != null) {
            b.putString("firstChar", firstChar);
        }
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mStrings = getStringListPreference().getStrings();
            mFirstChar = getStringListPreference().getFirstChar();
        } else {
            mStrings = savedInstanceState.getStringArrayList(SAVE_STATE_TEXT);
            mFirstChar = savedInstanceState.getString("firstChar", "");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.preference_string_array, container, false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(SAVE_STATE_TEXT, mStrings);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        EditText newItem = view.findViewById(R.id.new_item);
        Button addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(view1 -> {
            String newItemText = newItem.getText().toString();
            mStrings.add(mFirstChar + newItemText);
            newItem.setText("");
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });

        ListView lv = view.findViewById(R.id.list);
        adapter = new ArrayAdapter<>(getContext(),  android.R.layout.simple_list_item_1, mStrings);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener((adapterView, view12, i, l) -> {
            String clickedItem = adapter.getItem(i);
            AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
            adb.setTitle(getString(R.string.edit_list_remove_item_confirm_title));
            adb.setMessage(getString(R.string.edit_list_remove_item_confirm_text));
            adb.setPositiveButton("Yes", (dialog, which) -> {
                mStrings.remove(clickedItem);
                adapter.notifyDataSetChanged();
            });
            adb.setNegativeButton("No", (dialog, which) -> {
                // Do nothing.
            });
            adb.show();
        });

        TextView firstCharView = view.findViewById(R.id.first_char);
        firstCharView.setText(mFirstChar);
        firstCharView.setVisibility(mFirstChar != null && !"".equalsIgnoreCase(mFirstChar) ? View.VISIBLE : View.GONE);
    }

    private StringListPreference getStringListPreference() {
        return (StringListPreference) getPreference();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            ArrayList<String> value = mStrings;
            final StringListPreference preference = getStringListPreference();
            if (preference.callChangeListener(value)) {
                preference.setStrings(value);
            }
        }
    }

}
