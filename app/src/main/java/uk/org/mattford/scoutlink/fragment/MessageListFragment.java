package uk.org.mattford.scoutlink.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import uk.org.mattford.scoutlink.adapter.MessageListAdapter;
import uk.org.mattford.scoutlink.databinding.MessageListViewBinding;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;

public class MessageListFragment extends Fragment {
    private MessageListAdapter adapter;
    private MessageListViewBinding binding;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = MessageListViewBinding.inflate(inflater, container, false);

        RecyclerView lv = binding.list;
        lv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            binding.notAtBottom.setVisibility(lv.canScrollVertically(1) ? View.VISIBLE : View.GONE);
            }
        });
        binding.notAtBottom.setVisibility(lv.canScrollVertically(1) ? View.VISIBLE : View.GONE);
        binding.notAtBottom.setOnClickListener((v) -> scrollToBottom(true));

        return binding.getRoot();
    }

    public void setDataSource(Conversation conversation) {
        RecyclerView recyclerView = binding.list;
        // Reset the adapter to null when the source changes so we don't keep a reference
        // to the previous active conversation.
        adapter = null;
        conversation.getMessages().observe(getViewLifecycleOwner(), messages -> {
            binding.list.setVisibility(messages != null && messages.size() > 0 ? View.VISIBLE : View.GONE);
            binding.empty.setVisibility(messages != null && messages.size() > 0 ? View.GONE : View.VISIBLE);
            boolean isScrolledToBottom = adapter == null || !recyclerView.canScrollVertically(1);
            if (adapter == null) {
                adapter = new MessageListAdapter(messages);
                recyclerView.setAdapter(adapter);
            }
            adapter.notifyDataSetChanged();
            if (isScrolledToBottom) {
                scrollToBottom(false);
            }
        });
    }

    public void setDataSource(LinkedList<Message> messages) {
        RecyclerView recyclerView = binding.list;
        binding.list.setVisibility(messages != null && messages.size() > 0 ? View.VISIBLE : View.GONE);
        binding.empty.setVisibility(messages != null && messages.size() > 0 ? View.GONE : View.VISIBLE);
        adapter = new MessageListAdapter(messages);
        recyclerView.setAdapter(adapter);
        scrollToBottom(false);
    }

    private void scrollToBottom(boolean smooth) {
        int itemCount = adapter.getItemCount();
        if (itemCount > 0) {
            if (smooth) {
                binding.list.smoothScrollToPosition(itemCount - 1);
            } else {
                binding.list.scrollToPosition(itemCount - 1);
            }
        }
    }
}
