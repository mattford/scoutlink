package uk.org.mattford.scoutlink.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import uk.org.mattford.scoutlink.R;

public class ColourArrayAdapter extends RecyclerView.Adapter<ColourArrayAdapter.ViewHolder> {
    ArrayList<Integer> colours;
    OnColourSelectedListener listener;
    public ColourArrayAdapter(ArrayList<Integer> colours, OnColourSelectedListener listener) {
        super();
        this.listener = listener;
        this.colours = colours;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.colour_selector_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int colour = colours.get(position);
        holder.colourView.setOnClickListener(view -> listener.onColourSelected(colour));
        holder.colourView.setBackgroundColor(colour);
    }

    @Override
    public int getItemCount() {
        return colours.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView colourView;
        ViewHolder(View view) {
            super(view);
            mView = view;
            colourView = view.findViewById(R.id.colour_view);
        }
    }

    public interface OnColourSelectedListener {
        void onColourSelected(int colour);
    }
}
