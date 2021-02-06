package uk.org.mattford.scoutlink.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import androidx.preference.DialogPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import uk.org.mattford.scoutlink.R;

public class StringListPreference extends DialogPreference {
    private ArrayList<String> mStrings = new ArrayList<>();
    private String firstChar = "";

    public StringListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSummaryProvider(StringListPreference.SimpleSummaryProvider.getInstance());
        setDialogLayoutResource(R.layout.preference_string_array);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.uk_org_mattford_scoutlink_preference_StringListPreference);
        String firstChar = a.getString(R.styleable.uk_org_mattford_scoutlink_preference_StringListPreference_firstChar);
        if (firstChar != null) {
            this.firstChar = firstChar;
        }
        a.recycle();
    }

    public String getFirstChar() {
        return this.firstChar;
    }

    /**
     * Saves the text to the current data storage.
     *
     * @param strings The text to save
     */
    public void setStrings(ArrayList<String> strings) {
        final boolean wasBlocking = shouldDisableDependents();

        mStrings = strings;

        persistStringArray(strings);

        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }

        notifyChanged();
    }

    /**
     * Gets the text from the current data storage.
     *
     * @return The current preference value
     */
    public ArrayList<String> getStrings() {
        return mStrings;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        setStrings(getPersistedStringArray());
    }

    @Override
    public boolean shouldDisableDependents() {
        return mStrings.isEmpty() || super.shouldDisableDependents();
    }

    protected ArrayList<String> getPersistedStringArray() {
        Set<String> strings = getPersistedStringSet(null);
        if (strings != null) {
            return new ArrayList<>(strings);
        }
        String csValues = getSharedPreferences().getString(getKey(), "");
        return new ArrayList<>(Arrays.asList(TextUtils.split(csValues, ",")));
    }

    protected void persistStringArray(ArrayList<String> strings) {
        persistStringSet(new HashSet<>(strings));
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final StringListPreference.SavedState myState = new StringListPreference.SavedState(superState);
        myState.mStrings = getStrings();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(StringListPreference.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        StringListPreference.SavedState myState = (StringListPreference.SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setStrings(myState.mStrings);
    }


    private static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<StringListPreference.SavedState> CREATOR =
                new Parcelable.Creator<StringListPreference.SavedState>() {
                    @Override
                    public StringListPreference.SavedState createFromParcel(Parcel in) {
                        return new StringListPreference.SavedState(in);
                    }

                    @Override
                    public StringListPreference.SavedState[] newArray(int size) {
                        return new StringListPreference.SavedState[size];
                    }
                };

        ArrayList<String> mStrings;

        SavedState(Parcel source) {
            super(source);
            source.readStringList(mStrings);
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeStringList(mStrings);
        }
    }

    public static final class SimpleSummaryProvider implements SummaryProvider<StringListPreference> {

        private static StringListPreference.SimpleSummaryProvider sSimpleSummaryProvider;

        private SimpleSummaryProvider() {}

        public static StringListPreference.SimpleSummaryProvider getInstance() {
            if (sSimpleSummaryProvider == null) {
                sSimpleSummaryProvider = new SimpleSummaryProvider();
            }
            return sSimpleSummaryProvider;
        }

        @Override
        public CharSequence provideSummary(StringListPreference preference) {
            if (preference.getStrings().isEmpty()) {
                return (preference.getContext().getString(R.string.not_set));
            } else {
                return preference.getStrings().size() + " item(s)";
            }
        }
    }
}
