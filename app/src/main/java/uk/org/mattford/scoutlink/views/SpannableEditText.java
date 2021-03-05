package uk.org.mattford.scoutlink.views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.PopupWindowCompat;

import java.util.ArrayList;

import uk.org.mattford.scoutlink.R;

public class SpannableEditText extends NickCompletionTextView implements TextFormatPopupWindow.OnTextFormatChangedListener {
    Drawable fontButton = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_text_format, getContext().getTheme());
    TextFormatPopupWindow textFormatPopup;

    public SpannableEditText(@NonNull Context context) {
        super(context);
        init();
    }

    public SpannableEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpannableEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Drawable[] drawables = getCompoundDrawables();
        fontButton.setBounds(0, 0, fontButton.getIntrinsicWidth(), fontButton.getIntrinsicHeight());
        fontButton.setColorFilter(getTextColors().getDefaultColor(), PorterDuff.Mode.SRC_IN);
        setCompoundDrawables(drawables[0], drawables[1], fontButton, drawables[3]);
        setOnTouchListener((View view, MotionEvent motionEvent) -> {
            if (motionEvent.getAction() != MotionEvent.ACTION_UP) {
                return false;
            }
            if (motionEvent.getX() > getWidth() - getPaddingRight() - fontButton.getIntrinsicWidth()) {
                handleFontClick();
                return false;
            }
            view.performClick();
            return false;
        });

    }

    @Override
    protected void onSelectionChanged(int start, int end) {
        super.onSelectionChanged(start, end);
        if (textFormatPopup != null && textFormatPopup.isShowing()) {
            textFormatPopup.setCurrentFormat(buildFormatFromSelection());
        }
    }

    private TextFormatPopupWindow.TextFormat buildFormatFromSelection() {
        TextFormatPopupWindow.TextFormat format = new TextFormatPopupWindow.TextFormat();
        StyleSpan[] styleSpans = getText().getSpans(getSelectionStart(), getSelectionEnd(), StyleSpan.class);
        for (StyleSpan span : styleSpans) {
            if (span.getStyle() == Typeface.BOLD || span.getStyle() == Typeface.BOLD_ITALIC) {
                format.bold = true;
            }
            if (span.getStyle() == Typeface.ITALIC || span.getStyle() == Typeface.BOLD_ITALIC) {
                format.italic = true;
            }
        }
        UnderlineSpan[] underlineSpans = getText().getSpans(getSelectionStart(), getSelectionEnd(), UnderlineSpan.class);
        format.underline = underlineSpans.length > 0;
        format.textColour = getTextColors().getDefaultColor();
        ForegroundColorSpan[] foregroundColorSpans = getText().getSpans(getSelectionStart(), getSelectionEnd(), ForegroundColorSpan.class);
        for (ForegroundColorSpan span : foregroundColorSpans) {
            format.textColour = span.getForegroundColor();
        }
        format.backgroundColour = getResources().getColor(android.R.color.transparent);
        BackgroundColorSpan[] backgroundColorSpans = getText().getSpans(getSelectionStart(), getSelectionEnd(), BackgroundColorSpan.class);
        for (BackgroundColorSpan span : backgroundColorSpans) {
            format.backgroundColour = span.getBackgroundColor();
        }
        return format;
    }

    private void handleFontClick() {
        if (textFormatPopup == null) {
            textFormatPopup = new TextFormatPopupWindow(getContext(), this);
        }
        if (textFormatPopup.isShowing()) {
            textFormatPopup.dismiss();
        } else {
            textFormatPopup.setCurrentFormat(buildFormatFromSelection());
            textFormatPopup.setInputMethodMode(TextFormatPopupWindow.INPUT_METHOD_NEEDED);
            PopupWindowCompat.showAsDropDown(textFormatPopup, this, 0, 0, Gravity.TOP);
            PopupWindowCompat.setOverlapAnchor(textFormatPopup, false);
        }
    }

    @Override
    public void onBoldClicked(boolean enabled) {
        onStyleToggled(enabled, Typeface.BOLD);
    }

    @Override
    public void onItalicClicked(boolean enabled) {
        onStyleToggled(enabled, Typeface.ITALIC);
    }

    @Override
    public void onUnderlineClicked(boolean enabled) {
        boolean isUnderlined = false;
        Editable text = getText();
        int start = getSelectionStart();
        int end = getSelectionEnd();
        UnderlineSpan[] underlineSpans = text.getSpans(start, end, UnderlineSpan.class);
        for (UnderlineSpan span : underlineSpans) {
            int spanStart = text.getSpanStart(span);
            int spanEnd = text.getSpanEnd(span);
            if (!enabled) {
                if (spanStart < start) {
                    text.setSpan(new UnderlineSpan(), spanStart, start, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
                if (spanEnd > end) {
                    text.setSpan(new UnderlineSpan(), end, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                }
                text.removeSpan(span);
            }
            isUnderlined = true;
        }
        if (enabled && !isUnderlined) {
            text.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        }
    }

    private void onStyleToggled(boolean enabled, int type) {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        Editable text = getText();
        StyleSpan[] styleSpans = text.getSpans(start, end, StyleSpan.class);
        ArrayList<Integer> appliedStyles = new ArrayList<>();
        for (StyleSpan span : styleSpans) {
            int spanStart = text.getSpanStart(span);
            int spanEnd = text.getSpanEnd(span);
            if (!enabled) {
                if (spanStart < start) {
                    text.setSpan(new StyleSpan(span.getStyle()), spanStart, start, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
                if (spanEnd > end) {
                    text.setSpan(new StyleSpan(span.getStyle()), end, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                }
                text.removeSpan(span);
            } else {
                appliedStyles.add(span.getStyle());
            }
        }
        if (enabled && !appliedStyles.contains(type)) {
            text.setSpan(new StyleSpan(type), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        }
    }

    @Override
    public void onTextColourChanged(int colour) {
        Editable text = getText();
        int start = getSelectionStart();
        int end = getSelectionEnd();
        ForegroundColorSpan[] foregroundColorSpans = text.getSpans(start, end, ForegroundColorSpan.class);
        for (ForegroundColorSpan span : foregroundColorSpans) {
            int spanStart = text.getSpanStart(span);
            int spanEnd = text.getSpanEnd(span);
            if (colour != span.getForegroundColor()) {
                if (spanStart < start) {
                    text.setSpan(new ForegroundColorSpan(span.getForegroundColor()), spanStart, start, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
                if (spanEnd > end) {
                    text.setSpan(new ForegroundColorSpan(span.getForegroundColor()), end, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                }
                text.removeSpan(span);
            }
        }
        text.setSpan(new ForegroundColorSpan(colour), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
    }

    @Override
    public void onBackgroundColourChanged(int colour) {
        Editable text = getText();
        int start = getSelectionStart();
        int end = getSelectionEnd();
        BackgroundColorSpan[] backgroundColorSpans = text.getSpans(start, end, BackgroundColorSpan.class);
        for (BackgroundColorSpan span : backgroundColorSpans) {
            int spanStart = text.getSpanStart(span);
            int spanEnd = text.getSpanEnd(span);
            if (colour != span.getBackgroundColor()) {
                if (spanStart < start) {
                    text.setSpan(new ForegroundColorSpan(span.getBackgroundColor()), spanStart, start, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
                if (spanEnd > end) {
                    text.setSpan(new ForegroundColorSpan(span.getBackgroundColor()), end, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                }
                text.removeSpan(span);
            }
        }
        if (colour != 0) {
            text.setSpan(new BackgroundColorSpan(colour), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        }
    }
}
