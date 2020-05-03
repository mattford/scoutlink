package uk.org.mattford.scoutlink.model;

import org.pircbotx.User;

import java.util.ArrayList;
import java.util.LinkedList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class Conversation {
	
	private String CONVERSATION_NAME;
	private int type;
	private LinkedList<Message> messages;
	MutableLiveData<ArrayList<User>> usersLiveData;
	private MutableLiveData<LinkedList<Message>> messagesLiveData;
	private int unreadMessages = 0;

	public final static int TYPE_CHANNEL = 0;
	public final static int TYPE_QUERY = 1;
	public final static int TYPE_SERVER = 2;

	protected Conversation(String name) {
		this.CONVERSATION_NAME = name;
		this.messages = new LinkedList<>();
		this.usersLiveData = new MutableLiveData<>(new ArrayList<>());
		this.messagesLiveData = new MutableLiveData<>(this.messages);
	}
	
	public String getName() {
		return this.CONVERSATION_NAME;
	}
	
	public LiveData<LinkedList<Message>> getMessages() {
		return this.messagesLiveData;
	}

	private void onMessagesChanged() {
		this.messagesLiveData.postValue(this.messages);
	}

	public void onUserListChanged() {
		usersLiveData.postValue(new ArrayList<>());
	}

	public LiveData<ArrayList<User>> getUsers() {
		return usersLiveData;
	}

	public org.pircbotx.Channel getChannel() {
		return null;
	}
	
	void setType(int type) {
		this.type = type;
	}
	
	public int getType() {
		return this.type;
	}

	public void addMessage(Message msg) {
		messages.add(msg);
		onMessagesChanged();
	}
}
