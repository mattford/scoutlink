package uk.org.mattford.scoutlink.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.pircbotx.Channel;
import org.pircbotx.User;

import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.model.Settings;

public class UserListRecyclerViewAdapter extends RecyclerView.Adapter<UserListRecyclerViewAdapter.ViewHolder> {
    private final OnUserListItemClickListener mListener;
    private ArrayList<User> users = new ArrayList<>();
    private Channel channel;
    private final Settings settings;
    private ArrayList<User> filteredUsers = new ArrayList<>();
    private String filterText = "";

    public UserListRecyclerViewAdapter(Settings settings, OnUserListItemClickListener listener) {
        mListener = listener;
        this.settings = settings;
    }

    public void updateConversation(Channel channel, ArrayList<User> users) {
        this.channel = channel;
        this.users = users;
        refreshFilteredUsers();
    }

    public void setFilter(String filterText) {
        this.filterText = filterText;
        refreshFilteredUsers();
    }

    public void refreshFilteredUsers() {
        this.filteredUsers = new ArrayList<>();
        Collections.sort(this.users, (user, user2) -> {
            int rolePriorityUser1 = getUserRolePriority(user);
            int rolePriorityUser2 = getUserRolePriority(user2);
            if (rolePriorityUser1 != rolePriorityUser2) {
                return rolePriorityUser1 < rolePriorityUser2 ? 1 : -1;
            }
            return user.getNick().toLowerCase().compareTo(user2.getNick().toLowerCase());
        });
        for (User user : this.users) {
            if ("".equalsIgnoreCase(this.filterText) || user.getNick().toLowerCase().contains(this.filterText.toLowerCase())) {
                this.filteredUsers.add(user);
            }
        }
        notifyDataSetChanged();
    }

    private int getUserRolePriority(User user) {
        if (channel != null) {
            if (channel.isOwner(user)) {
                return 5;
            } else if (channel.isSuperOp(user)) {
                return 4;
            } else if (channel.isOp(user)) {
                return 3;
            } else if (channel.isHalfOp(user)) {
                return 2;
            } else if (channel.hasVoice(user)) {
                return 1;
            }
        }
        return 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ArrayList<String> blockedUsers = settings.getBlockedUsers();
        holder.mItem = this.filteredUsers.get(position);
        holder.mUserNameView.setText(holder.mItem.getNick());
        // Show the highest level of access
        String role = null;
        if (channel != null) {
            if (channel.isOwner(holder.mItem)) {
                role = "Owner";
            } else if (channel.isSuperOp(holder.mItem)) {
                role = "Admin";
            } else if (channel.isOp(holder.mItem)) {
                role = "Op";
            } else if (channel.isHalfOp(holder.mItem)) {
                role = "Half-op";
            } else if (channel.hasVoice(holder.mItem)) {
                role = "Voice";
            }
        }

        holder.mBlockedIconView.setVisibility(blockedUsers.contains(holder.mItem.getNick()) ? View.VISIBLE : View.GONE);

        holder.mUserRoleView.setVisibility(View.GONE);
        if (role != null && !blockedUsers.contains(holder.mItem.getNick())) {
            holder.mUserRoleView.setText(role);
            holder.mUserRoleView.setVisibility(View.VISIBLE);
        }
        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onUserListItemClicked(v, holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mUserNameView;
        final TextView mUserRoleView;
        final ImageView mBlockedIconView;
        User mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mUserNameView = view.findViewById(R.id.user_name);
            mUserRoleView = view.findViewById(R.id.role_label);
            mBlockedIconView = view.findViewById(R.id.blocked_icon);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mUserNameView.getText() + "'";
        }
    }

    public interface OnUserListItemClickListener {
        void onUserListItemClicked(View view, User user);
    }
}
