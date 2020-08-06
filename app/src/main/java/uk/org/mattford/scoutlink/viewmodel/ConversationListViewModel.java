package uk.org.mattford.scoutlink.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Server;

public class ConversationListViewModel extends ViewModel implements Server.OnConversationListChangedListener, Server.OnActiveConversationChangedListener {
    private MutableLiveData<ArrayList<Conversation>> conversationLiveData;
    private MutableLiveData<Conversation> activeConversationLiveData;

    public ConversationListViewModel() {
        Server server = Server.getInstance();
        server.addOnConversationListChangedListener(this);
        server.addOnActiveConversationChangedListener(this);
        conversationLiveData = new MutableLiveData<>(new ArrayList<>());
        activeConversationLiveData = new MutableLiveData<>(null);
        onConversationListChanged(server.getConversations());
    }

    public LiveData<ArrayList<Conversation>> getConversations() {
        return conversationLiveData;
    }

    public LiveData<Conversation> getActiveConversation() {
        return this.activeConversationLiveData;
    }

    public void setActiveConversation(Conversation conversation) {
        if (getActiveConversation().getValue() != null) {
            getActiveConversation().getValue().setActive(false);
        }
        if (conversation != null) {
            conversation.setActive(true);
        }
        this.activeConversationLiveData.postValue(conversation);
    }

    public void onConversationListChanged(HashMap<String, Conversation> conversationHashMap) {
        ArrayList<Conversation> conversations = new ArrayList<>(conversationHashMap.values());
        conversationLiveData.postValue(conversations);
        Conversation activeConversation = getActiveConversation().getValue();
        if (activeConversation == null || !conversations.contains(activeConversation)) {
            if (conversations.size() > 0) {
                setActiveConversation(conversations.get(0));
            } else {
                setActiveConversation(null);
            }
        }
    }

    @Override
    public void onActiveConversationChanged(Conversation conversation) {
        setActiveConversation(conversation);
    }
}
