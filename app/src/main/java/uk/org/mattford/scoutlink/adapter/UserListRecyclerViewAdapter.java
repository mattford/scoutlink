package uk.org.mattford.scoutlink.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pircbotx.Channel;
import org.pircbotx.User;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;
import uk.org.mattford.scoutlink.R;

public class UserListRecyclerViewAdapter extends RecyclerView.Adapter<UserListRecyclerViewAdapter.ViewHolder> {

    private OnUserListItemClickListener mListener;
    private ArrayList<User> users;
    private Channel channel;

    public UserListRecyclerViewAdapter(ArrayList<User> users, Channel channel, OnUserListItemClickListener listener) {
        mListener = listener;
        this.users = users;
        this.channel = channel;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = this.users.get(position);
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

        if (role != null) {
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
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mUserNameView;
        final TextView mUserRoleView;
        User mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mUserNameView = view.findViewById(R.id.user_name);
            mUserRoleView = view.findViewById(R.id.role_label);
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
