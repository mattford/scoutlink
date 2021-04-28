package uk.org.mattford.scoutlink.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.PopupWindowCompat;

import java.util.ArrayList;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.databinding.MessageInputBoxBinding;
import uk.org.mattford.scoutlink.utils.MircColors;

public class MessageInputBox extends LinearLayout implements NickCompletionTextView.OnSelectionChangedListener {
    Drawable fontButton = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_text_format, getContext().getTheme());
    ArrayList<Integer> colours = new ArrayList<>();
    ColourSelectorPopupWindow backgroundColourSelector;
    ColourSelectorPopupWindow textColourSelector;
    MessageInputBoxBinding binding;
    TextFormat currentFormat;

    public MessageInputBox(Context context) {
        super(context);
        init();
    }

    public MessageInputBox(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MessageInputBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        binding = MessageInputBoxBinding.inflate(LayoutInflater.from(getContext()), this, true);
        Drawable[] drawables = binding.input.getCompoundDrawables();
        fontButton.setBounds(0, 0, fontButton.getIntrinsicWidth(), fontButton.getIntrinsicHeight());
        fontButton.setColorFilter(binding.input.getTextColors().getDefaultColor(), PorterDuff.Mode.SRC_IN);
        binding.input.setCompoundDrawables(drawables[0], drawables[1], fontButton, drawables[3]);
        binding.input.setOnTouchListener((View view, MotionEvent motionEvent) -> {
            if (motionEvent.getAction() != MotionEvent.ACTION_UP) {
                return false;
            }
            if (motionEvent.getX() > binding.input.getWidth() - binding.input.getPaddingRight() - fontButton.getIntrinsicWidth()) {
                handleFontClick();
                return true;
            }
            view.performClick();
            return false;
        });
        binding.input.setOnSelectionChangedListener(this);
        binding.bold.setOnClickListener(view -> onBoldClicked());
        binding.underline.setOnClickListener(view -> onUnderlineClicked());
        binding.italic.setOnClickListener(view -> onItalicClicked());

        for (int colour : MircColors.getColours()) {
            colours.add(colour | 0xFF000000);
        }

        binding.textColour.setOnClickListener(view -> {
            if (backgroundColourSelector != null && backgroundColourSelector.isShowing()) {
                backgroundColourSelector.dismiss();
            }
            if (textColourSelector == null) {
                textColourSelector = new ColourSelectorPopupWindow(getContext(), colours, (colour, popup) -> {
                    onTextColourChanged(colour);
                    popup.dismiss();
                });
            }
            if (textColourSelector.isShowing()) {
                textColourSelector.dismiss();
            } else {
                PopupWindowCompat.showAsDropDown(textColourSelector, binding.textColour, 0, 0, Gravity.TOP);
            }
        });

        binding.backgroundColour.setOnClickListener(view -> {
            if (textColourSelector != null && textColourSelector.isShowing()) {
                textColourSelector.dismiss();
            }
            if (backgroundColourSelector == null) {
                backgroundColourSelector = new ColourSelectorPopupWindow(getContext(), colours, (colour, popup) -> {
                    onBackgroundColourChanged(colour);
                    popup.dismiss();
                });
            }
            if (backgroundColourSelector.isShowing()) {
                backgroundColourSelector.dismiss();
            } else {
                PopupWindowCompat.showAsDropDown(backgroundColourSelector, binding.backgroundColour, 0, 0, Gravity.TOP);
            }
        });

    }

    public void onSelectionChanged(int start, int end) {
        if (binding.textFormatBar.getVisibility() == VISIBLE) {
            setCurrentFormat(buildFormatFromSelection());
        }
    }

    private TextFormat buildFormatFromSelection() {
        TextFormat format = new TextFormat();
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
        boolean isShowing = binding.textFormatBar.getVisibility() == VISIBLE;
        Drawable textFormatButton = binding.input.getCompoundDrawables()[2];
        if (isShowing) {
            binding.textFormatBar.setVisibility(GONE);
            textFormatButton.setColorFilter(getResources().getColor(android.R.color.black), PorterDuff.Mode.SRC_IN);
        } else {
            getEditText().setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            setCurrentFormat(buildFormatFromSelection());
            binding.textFormatBar.setVisibility(VISIBLE);
            textFormatButton.setColorFilter(getResources().getColor(R.color.scoutlink_orange), PorterDuff.Mode.SRC_IN);
        }
    }

    public void onBoldClicked() {
        currentFormat.bold = !currentFormat.bold;
        onStyleToggled(currentFormat.bold, Typeface.BOLD);
        updateUi();
    }

    public void onItalicClicked() {
        currentFormat.italic = !currentFormat.italic;
        onStyleToggled(currentFormat.italic, Typeface.ITALIC);
        updateUi();
    }

    public void onUnderlineClicked() {
        currentFormat.underline = !currentFormat.underline;
        boolean enabled = currentFormat.underline;
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
        updateUi();
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

    public void onTextColourChanged(int colour) {
        currentFormat.textColour = colour;
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
        updateUi();
    }

    public void onBackgroundColourChanged(int colour) {
        currentFormat.backgroundColour = colour;
        Editable text = getText();
        int start = getSelectionStart();
        int end = getSelectionEnd();
        BackgroundColorSpan[] backgroundColorSpans = text.getSpans(start, end, BackgroundColorSpan.class);
        for (BackgroundColorSpan span : backgroundColorSpans) {
            int spanStart = text.getSpanStart(span);
            int spanEnd = text.getSpanEnd(span);
            if (colour != span.getBackgroundColor()) {
                if (spanStart < start) {
                    text.setSpan(new BackgroundColorSpan(span.getBackgroundColor()), spanStart, start, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
                if (spanEnd > end) {
                    text.setSpan(new BackgroundColorSpan(span.getBackgroundColor()), end, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                }
                text.removeSpan(span);
            }
        }
        if (colour != 0) {
            text.setSpan(new BackgroundColorSpan(colour), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        }
        updateUi();
    }

    public Editable getText() {
        return binding.input.getText();
    }

    public int getSelectionStart() {
        return binding.input.getSelectionStart();
    }

    public int getSelectionEnd() {
        return binding.input.getSelectionEnd();
    }

    public ColorStateList getTextColors() {
        return binding.input.getTextColors();
    }

    public NickCompletionTextView getEditText() {
        return binding.input;
    }

    public void setCurrentFormat(TextFormat format) {
        currentFormat = format;
        updateUi();
    }

    public static class TextFormat {
        public boolean bold = false;
        public boolean italic = false;
        public boolean underline = false;
        public int textColour = 0x000000;
        public int backgroundColour = 0x0000000;
    }

    private void updateUi() {
        int black = ResourcesCompat.getColor(getResources(), android.R.color.black, getContext().getTheme());
        int orange = ResourcesCompat.getColor(getResources(), R.color.scoutlink_orange, getContext().getTheme());
        binding.bold.getDrawable().setColorFilter(currentFormat.bold ? orange : black, PorterDuff.Mode.SRC_IN);
        binding.underline.getDrawable().setColorFilter(currentFormat.underline ? orange : black, PorterDuff.Mode.SRC_IN);
        binding.italic.getDrawable().setColorFilter(currentFormat.italic ? orange : black, PorterDuff.Mode.SRC_IN);
        binding.textColour.getDrawable().setColorFilter(currentFormat.textColour, PorterDuff.Mode.SRC_IN);
        if (colours.contains(currentFormat.backgroundColour)) {
            binding.backgroundColour.getDrawable().setColorFilter(currentFormat.backgroundColour, PorterDuff.Mode.SRC_IN);
        } else {
            binding.backgroundColour.getDrawable().setColorFilter(null);
        }
    }

}
