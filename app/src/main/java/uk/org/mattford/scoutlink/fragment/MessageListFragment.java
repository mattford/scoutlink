package uk.org.mattford.scoutlink.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import uk.org.mattford.scoutlink.adapter.MessageListAdapter;
import uk.org.mattford.scoutlink.databinding.MessageListViewBinding;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.viewmodel.ConversationListViewModel;

public class MessageListFragment extends Fragment {
    private MessageListAdapter adapter;
    private MessageListViewBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = MessageListViewBinding.inflate(inflater, container, false);

        RecyclerView lv = binding.list;
        lv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                binding.notAtBottom.setVisibility(lv.canScrollVertically(1) ? View.VISIBLE : View.GONE);
            }
        });
        binding.notAtBottom.setOnClickListener((v) -> binding.list.smoothScrollToPosition(adapter.getItemCount() - 1));

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ConversationListViewModel viewModel = new ViewModelProvider(requireActivity()).get(ConversationListViewModel.class);
        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = binding.list;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    public void setDataSource(Conversation conversation) {
        RecyclerView recyclerView = binding.list;
        // Reset the adapter to null when the source changes so we don't keep a reference
        // to the previous active conversation.
        adapter = null;
        conversation.getMessages().observe(getViewLifecycleOwner(), messages -> {
            binding.list.setVisibility(messages != null && messages.size() > 0 ? View.VISIBLE : View.GONE);
            binding.empty.setVisibility(messages != null && messages.size() > 0 ? View.GONE : View.VISIBLE);
            boolean isScrolledToBottom = !recyclerView.canScrollVertically(1);
            if (adapter == null) {
                adapter = new MessageListAdapter(getContext(), messages);
                recyclerView.setAdapter(adapter);
            }
            adapter.notifyDataSetChanged();
            if (isScrolledToBottom) {
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        });
    }

    public void setDataSource(LinkedList<Message> messages) {
        RecyclerView recyclerView = binding.list;
        binding.list.setVisibility(messages != null && messages.size() > 0 ? View.VISIBLE : View.GONE);
        binding.empty.setVisibility(messages != null && messages.size() > 0 ? View.GONE : View.VISIBLE);
        adapter = new MessageListAdapter(getContext(), messages);
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }
}
