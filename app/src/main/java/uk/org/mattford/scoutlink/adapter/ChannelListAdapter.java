package uk.org.mattford.scoutlink.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.pircbotx.ChannelListEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.org.mattford.scoutlink.R;

public class ChannelListAdapter extends RecyclerView.Adapter<ChannelListAdapter.ViewHolder> {
    private final List<ChannelListEntry> channels = new ArrayList<>();
    private final ChannelListAdapter.OnChannelListItemClickListener listener;

    public ChannelListAdapter(ChannelListAdapter.OnChannelListItemClickListener listener) {
        this.listener = listener;
    }

    public void setChannels(List<ChannelListEntry> channelList) {
        channels.clear();
        for (ChannelListEntry channel : channelList) {
            if (channel.getName().startsWith("#")) {
                channels.add(channel);
            }
        }
        Collections.sort(channels, (lhs, rhs) -> lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase()));
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChannelListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.channel_list_item, parent, false);
        return new ChannelListAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ChannelListAdapter.ViewHolder holder, int position) {
        holder.mChannel = channels.get(position);
        holder.mChannelNameView.setText(holder.mChannel.getName());
        Pattern topicPattern = Pattern.compile("\\[[^]]*] (.*)");
        Matcher topicMatcher = topicPattern.matcher(holder.mChannel.getTopic());
        if (topicMatcher.find()) {
            holder.mChannelTopicView.setText(topicMatcher.group(1));
        } else {
            holder.mChannelTopicView.setText("");
        }
        holder.mUsersCountView.setText(Integer.toString(holder.mChannel.getUsers()));
        holder.mView.setOnClickListener(view -> listener.onChannelListItemClick(holder.mChannel.getName()));
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        ChannelListEntry mChannel;
        final TextView mChannelNameView;
        final TextView mChannelTopicView;
        final TextView mUsersCountView;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mChannelNameView = view.findViewById(R.id.channel_name);
            mChannelTopicView = view.findViewById(R.id.channel_topic);
            mUsersCountView = view.findViewById(R.id.channel_user_count);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mChannelNameView.getText() + "'";
        }
    }

    public interface OnChannelListItemClickListener {
        void onChannelListItemClick(String channel);
    }
}
