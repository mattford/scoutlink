package uk.org.mattford.scoutlink.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

public class NickCompletionTextView extends AppCompatAutoCompleteTextView {

    public NickCompletionTextView(@NonNull Context context) {
        super(context);
    }

    public NickCompletionTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NickCompletionTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean enoughToFilter() {
        // Find last instance of @
        int cursorLocation = getSelectionStart();
        String text = getText().toString();
        int lastAtSymbol = text.substring(0, cursorLocation).lastIndexOf("@");
        return lastAtSymbol > -1 && !text.substring(lastAtSymbol, cursorLocation).contains(" ");
    }

    @Override
    public void performFiltering(CharSequence text, int keyCode) {
        // Find last instance of @
        int cursorLocation = getSelectionStart();
        int lastAtSymbol = text.toString().substring(0, cursorLocation).lastIndexOf("@");
        if (lastAtSymbol > -1) {
            CharSequence searchString = text.subSequence(lastAtSymbol + 1, cursorLocation);
            super.performFiltering(searchString, keyCode);
        }
    }

    @Override
    protected void replaceText(CharSequence selectedNickname) {
        // Find last instance of @
        int cursorLocation = getSelectionStart();
        String currentText = getText().toString();
        int lastAtSymbol = currentText.substring(0, cursorLocation).lastIndexOf("@");
        if (lastAtSymbol > -1) {
            // Go to next space, or length of suggestion
            String sb = currentText.substring(0, lastAtSymbol + 1) +
                    selectedNickname +
                    currentText.substring(cursorLocation);
            setText(sb);
            setSelection(lastAtSymbol + selectedNickname.length() + 1);
        }
    }
}
