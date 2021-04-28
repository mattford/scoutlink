package uk.org.mattford.scoutlink.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;

import uk.org.mattford.scoutlink.adapter.ColourArrayAdapter;
import uk.org.mattford.scoutlink.databinding.ColourSelectorPopupWindowBinding;

public class ColourSelectorPopupWindow extends PopupWindow {
    public ColourSelectorPopupWindow(Context context, ArrayList<Integer> colours, OnColourSelectedListener listener) {
        super(context);
        ColourSelectorPopupWindowBinding binding = ColourSelectorPopupWindowBinding.inflate(LayoutInflater.from(context));
        binding.grid.setLayoutManager(new GridLayoutManager(context, 7));
        binding.grid.setAdapter(new ColourArrayAdapter(colours, colour -> listener.onColourSelected(colour, this)));

        setContentView(binding.getRoot());
    }

    interface OnColourSelectedListener {
        void onColourSelected(int colour, ColourSelectorPopupWindow popup);
    }
}
