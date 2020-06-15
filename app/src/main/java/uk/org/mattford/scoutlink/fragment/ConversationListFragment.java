package uk.org.mattford.scoutlink.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.adapter.ConversationListRecyclerViewAdapter;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.viewmodel.ConversationListViewModel;

/**
 * A fragment to show the list of conversations
 */
public class ConversationListFragment extends Fragment {
    private ConversationListRecyclerViewAdapter serverWindowAdapter;
    private ConversationListRecyclerViewAdapter channelsAdapter;
    private ConversationListRecyclerViewAdapter directMessagesAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversation_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ConversationListViewModel viewModel = new ViewModelProvider(requireActivity()).get(ConversationListViewModel.class);
        // Set the adapter
        RecyclerView serverWindowRecyclerView = view.findViewById(R.id.server_window_list);
        RecyclerView channelsRecyclerView = view.findViewById(R.id.channel_list);
        RecyclerView directMessagesRecyclerView = view.findViewById(R.id.direct_message_list);
        serverWindowAdapter = new ConversationListRecyclerViewAdapter(this);
        channelsAdapter = new ConversationListRecyclerViewAdapter(this);
        directMessagesAdapter = new ConversationListRecyclerViewAdapter(this);
        serverWindowRecyclerView.setAdapter(serverWindowAdapter);
        channelsRecyclerView.setAdapter(channelsAdapter);
        directMessagesRecyclerView.setAdapter(directMessagesAdapter);
        viewModel.getConversations().observe(getViewLifecycleOwner(), conversations -> {
            ArrayList<Conversation> serverWindows = new ArrayList<>();
            ArrayList<Conversation> channels = new ArrayList<>();
            ArrayList<Conversation> directMessages = new ArrayList<>();
            for (Conversation conversation: conversations) {
               switch (conversation.getType()) {
                   case Conversation.TYPE_CHANNEL:
                       channels.add(conversation);
                       break;
                   case Conversation.TYPE_QUERY:
                       directMessages.add(conversation);
                       break;
                   case Conversation.TYPE_SERVER:
                       serverWindows.add(conversation);
                       break;
               }
            }
            Collections.sort(serverWindows);
            Collections.sort(channels);
            Collections.sort(directMessages);
            serverWindowAdapter.setConversationList(serverWindows);
            channelsAdapter.setConversationList(channels);
            directMessagesAdapter.setConversationList(directMessages);
            view.findViewById(R.id.direct_message_label).setVisibility(directMessages.size() > 0 ? View.VISIBLE : View.GONE);
            viewModel.getActiveConversation().observe(getViewLifecycleOwner(), this::setActiveConversation);
            for (Conversation conversation : conversations) {
                conversation.getUnreadMessagesCount().observe(getViewLifecycleOwner(), unreadCount -> {
                    notifyConversationChanged(conversation);
                });
            }
        });
    }

    private void setActiveConversation(Conversation conversation) {
        serverWindowAdapter.setActiveConversation(conversation);
        channelsAdapter.setActiveConversation(conversation);
        directMessagesAdapter.setActiveConversation(conversation);
    }

    private void notifyConversationChanged(Conversation conversation) {
        serverWindowAdapter.notifyConversationChanged(conversation);
        channelsAdapter.notifyConversationChanged(conversation);
        directMessagesAdapter.notifyConversationChanged(conversation);
    }

    public void onItemClicked(Conversation conversation) {
        ConversationListViewModel viewModel = new ViewModelProvider(requireActivity()).get(ConversationListViewModel.class);
        viewModel.setActiveConversation(conversation);
    }
}
