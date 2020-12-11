package uk.org.mattford.scoutlink.model;

import org.pircbotx.User;

import java.util.ArrayList;
import java.util.LinkedList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import uk.org.mattford.scoutlink.database.LogDatabase;
import uk.org.mattford.scoutlink.database.entities.LogMessage;

public class Conversation implements Comparable<Conversation> {
	private boolean isActive;
	private final String name;
	private int type;
	private final LinkedList<Message> messages = new LinkedList<>();
	private final MutableLiveData<ArrayList<User>> usersLiveData = new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<LinkedList<Message>> messagesLiveData = new MutableLiveData<>(new LinkedList<>());
	private final MutableLiveData<Integer> unreadMessagesLiveData = new MutableLiveData<>(0);

	public final static int TYPE_CHANNEL = 0;
	public final static int TYPE_QUERY = 1;
	public final static int TYPE_SERVER = 2;

	protected Conversation(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
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
		this.addMessage(msg, true);
	}

	public void addMessage(Message msg, boolean log) {
		messages.add(msg);
		if (!isActive) {
			int unreadMessages = 0;
			if (getUnreadMessagesCount().getValue() != null) {
				unreadMessages = this.getUnreadMessagesCount().getValue();
			}
			this.unreadMessagesLiveData.postValue(unreadMessages + 1);
		}
		if (log) {
			LogMessage logMessage = new LogMessage(this, msg);
			LogDatabase db = LogDatabase.getInstance();
			if (db != null) {
			    new Thread(() -> db.logMessageDao().insert(logMessage)).start();
			}
		}
		onMessagesChanged();
	}

	public LiveData<Integer> getUnreadMessagesCount() {
		return this.unreadMessagesLiveData;
	}

	public void setActive(boolean active) {
		this.isActive = active;
		this.unreadMessagesLiveData.postValue(0);
	}

	@Override
	public int compareTo(Conversation other) {
		return getName().compareTo(other.getName());
	}
}
