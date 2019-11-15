package uk.org.mattford.scoutlink.fragment;

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.adapter.ConversationsPagerAdapter;

public class ConversationListRecyclerViewAdapter extends RecyclerView.Adapter<ConversationListRecyclerViewAdapter.ViewHolder> {

    private final ConversationListFragment.OnConversationListFragmentInteractionListener mListener;
    private ConversationsPagerAdapter adapter;
    private ViewPager pager;

    ConversationListRecyclerViewAdapter(ViewPager pager, ConversationListFragment.OnConversationListFragmentInteractionListener listener) {
        mListener = listener;

        this.pager = pager;
        this.adapter = (ConversationsPagerAdapter)pager.getAdapter();
        pager.addOnPageChangeListener(new OnPageChangeListener());
        PagerAdapter adapter = pager.getAdapter();
        if (adapter != null) {
            adapter.registerDataSetObserver(new ConversationListDataSetObserver());
        }

        pager.addOnAdapterChangeListener((viewPager, oldAdapter, newAdapter) -> {
            this.adapter = (ConversationsPagerAdapter)newAdapter;
            if (this.adapter != null) {
                this.adapter.registerDataSetObserver(new ConversationListDataSetObserver());
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = this.adapter.getItemInfo(position);
        holder.mConversationNameView.setText(holder.mItem.conv.getName());

        // TODO: Actually implement this
        holder.mUnreadMessagesView.setText("5");
        holder.mUnreadMessagesView.setVisibility(View.VISIBLE);

        if (position == pager.getCurrentItem()) {
            holder.mConversationNameView.setTextColor(holder.mConversationNameView.getContext().getResources().getColor(R.color.scoutlink_orange));
        } else {
            holder.mConversationNameView.setTextColor(holder.mConversationNameView.getContext().getResources().getColor(R.color.white));
        }

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onConversationSelected(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return adapter.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mConversationNameView;
        final TextView mUnreadMessagesView;
        ConversationsPagerAdapter.ConversationInfo mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mConversationNameView = view.findViewById(R.id.conversation_name);
            mUnreadMessagesView = view.findViewById(R.id.unread_message_count);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mConversationNameView.getText() + "'";
        }
    }

    private class ConversationListDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            notifyDataSetChanged();
        }
    }

    private class OnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            notifyDataSetChanged();
        }

        @Override
        public void onPageSelected(int position) {
            notifyDataSetChanged();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // Do nothing
        }
    }
}
