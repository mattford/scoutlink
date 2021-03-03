package uk.org.mattford.scoutlink.views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.PopupWindow;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.PopupWindowCompat;

import java.util.ArrayList;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.databinding.PopupTextFormatBinding;
import uk.org.mattford.scoutlink.utils.MircColors;

public class TextFormatPopupWindow extends PopupWindow {
    private TextFormat currentFormat = new TextFormat();
    private final PopupTextFormatBinding binding;
    private final Context context;
    private final ArrayList<Integer> colours = new ArrayList<>();
    private ColourSelectorPopupWindow textColourSelector;
    private ColourSelectorPopupWindow backgroundColourSelector;
    public TextFormatPopupWindow(Context context, OnTextFormatChangedListener listener) {
        super(context);
        this.context = context;
        binding = PopupTextFormatBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());

        binding.bold.setOnClickListener(view -> {
            currentFormat.bold = !currentFormat.bold;
            listener.onBoldClicked(currentFormat.bold);
            updateUi();
        });
        binding.underline.setOnClickListener(view -> {
            currentFormat.underline = !currentFormat.underline;
            listener.onUnderlineClicked(currentFormat.underline);
            updateUi();
        });
        binding.italic.setOnClickListener(view -> {
            currentFormat.italic = !currentFormat.italic;
            listener.onItalicClicked(currentFormat.italic);
            updateUi();
        });

        for (int colour : MircColors.getColours()) {
            colours.add(colour | 0xFF000000);
        }

        binding.textColour.setOnClickListener(view -> {
            if (backgroundColourSelector != null && backgroundColourSelector.isShowing()) {
                backgroundColourSelector.dismiss();
            }
            if (textColourSelector == null) {
                textColourSelector = new ColourSelectorPopupWindow(context, colours, (colour, popup) -> {
                    currentFormat.textColour = colour;
                    listener.onTextColourChanged(colour);
                    updateUi();
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
                backgroundColourSelector = new ColourSelectorPopupWindow(context, colours, (colour, popup) -> {
                    currentFormat.backgroundColour = colour;
                    listener.onBackgroundColourChanged(colour);
                    updateUi();
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
        int black = ResourcesCompat.getColor(context.getResources(), android.R.color.black, context.getTheme());
        int orange = ResourcesCompat.getColor(context.getResources(), R.color.scoutlink_orange, context.getTheme());
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

    public interface OnTextFormatChangedListener {
        void onBoldClicked(boolean enabled);
        void onItalicClicked(boolean enabled);
        void onUnderlineClicked(boolean enabled);
        void onTextColourChanged(int colour);
        void onBackgroundColourChanged(int colour);
    }
}
