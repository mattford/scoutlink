package uk.org.mattford.scoutlink.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pircbotx.User;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;
import uk.org.mattford.scoutlink.R;

public class UserListRecyclerViewAdapter extends RecyclerView.Adapter<UserListRecyclerViewAdapter.ViewHolder> {

    private final UserListFragment.OnUserListFragmentInteractionListener mListener;
    private ArrayList<User> users;

    UserListRecyclerViewAdapter(ArrayList<User> users, UserListFragment.OnUserListFragmentInteractionListener listener) {
        mListener = listener;
        this.users = users;
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

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onUserListItemClicked(holder.mItem.getNick());
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mUserNameView;
        User mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mUserNameView = view.findViewById(R.id.user_name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mUserNameView.getText() + "'";
        }
    }

}
