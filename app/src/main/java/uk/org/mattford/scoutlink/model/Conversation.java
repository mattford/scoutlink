package uk.org.mattford.scoutlink.model;

import com.google.common.collect.ImmutableSortedSet;

import org.pircbotx.User;

import java.util.ArrayList;
import java.util.LinkedList;

public class Conversation {
	
	private String CONVERSATION_NAME;
	private int type;
	private LinkedList<Message> messages;
	private LinkedList<Message> buffer;
	
	public final static int TYPE_CHANNEL = 0;
	public final static int TYPE_QUERY = 1;
	public final static int TYPE_SERVER = 2;

    private boolean isSelected = false;

	protected Conversation(String name) {
		this.CONVERSATION_NAME = name;
		this.messages = new LinkedList<>();
		this.buffer = new LinkedList<>();
	}
	
	public String getName() {
		return this.CONVERSATION_NAME;
	}
	
	public LinkedList<Message> getMessages() {
		return this.messages;
	}
	
	public LinkedList<Message> getBuffer() {
		return this.buffer;
	}
	
	public Message pollBuffer() {
		Message message = buffer.pollFirst();
		messages.add(message);
		return message;
	}

	public ArrayList<User> getUsers() {
		return new ArrayList<>();
	}
	
	public boolean hasBuffer() {
		return buffer.size() != 0;
	}
	
	public void clearBuffer() {
		buffer.clear();
	}
	
	protected void setType(int type) {
		this.type = type;
	}
	
	public int getType() {
		return this.type;
	}
	
	public void addMessage(Message msg) {
		buffer.add(msg);
	}

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
