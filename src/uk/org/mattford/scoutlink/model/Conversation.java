package uk.org.mattford.scoutlink.model;

import java.util.ArrayList;

public class Conversation {
	
	public String CONVERSATION_NAME;
	private ArrayList<Message> messages;

	public Conversation(String name) {
		this.CONVERSATION_NAME = name;
		this.messages = new ArrayList<Message>();
	}
	
	public String getName() {
		return this.CONVERSATION_NAME;
	}
	
	public ArrayList<Message> getMessages() {
		return this.messages;
	}
	
	public void addMessage(String sender, String message) {
		Message msg = new Message(sender, message);
		messages.add(msg);
	}
}
