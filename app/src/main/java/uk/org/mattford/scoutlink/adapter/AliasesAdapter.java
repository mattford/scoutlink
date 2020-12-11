package uk.org.mattford.scoutlink.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.database.entities.Alias;

public class AliasesAdapter extends RecyclerView.Adapter<AliasesAdapter.ViewHolder> {
    private final List<Alias> aliases = new ArrayList<>();
    private final AliasesAdapter.OnAliasClickListener listener;

    public AliasesAdapter(AliasesAdapter.OnAliasClickListener listener) {
        this.listener = listener;
    }

    public void setAliases(List<Alias> newAliases) {
        for (Alias a : newAliases) {
            Log.d("SL", a.commandName);
        }
        aliases.clear();
        aliases.addAll(newAliases);
        notifyDataSetChanged();
    }

    @Override
    public AliasesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alias_list_item, parent, false);
        return new AliasesAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final AliasesAdapter.ViewHolder holder, int position) {
        Alias alias = aliases.get(position);
        holder.mAliasCommandNameView.setText(alias.commandName);

        holder.mView.setOnClickListener(view -> listener.onAliasClick(holder.mAliasCommandNameView.getText().toString()));
    }

    @Override
    public int getItemCount() {
        return aliases.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mAliasCommandNameView;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mAliasCommandNameView = view.findViewById(R.id.alias_command_name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mAliasCommandNameView.getText() + "'";
        }
    }

    public interface OnAliasClickListener {
        void onAliasClick(String commandName);
    }
}
