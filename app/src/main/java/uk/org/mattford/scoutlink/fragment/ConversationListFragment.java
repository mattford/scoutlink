package uk.org.mattford.scoutlink.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.adapter.ConversationListRecyclerViewAdapter;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.viewmodel.ConversationListViewModel;

/**
 * A fragment to show the list of conversations
 */
public class ConversationListFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversation_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ConversationListViewModel viewModel = new ViewModelProvider(requireActivity()).get(ConversationListViewModel.class);
        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        viewModel.getConversations().observe(getViewLifecycleOwner(), conversations -> {
            viewModel.getActiveConversation().observe(getViewLifecycleOwner(), activeConversation -> {
                recyclerView.setAdapter(new ConversationListRecyclerViewAdapter(this, conversations, activeConversation));
            });
        });
    }

    public void onItemClicked(Conversation conversation) {
        ConversationListViewModel viewModel = new ViewModelProvider(requireActivity()).get(ConversationListViewModel.class);
        viewModel.setActiveConversation(conversation);
    }
}
