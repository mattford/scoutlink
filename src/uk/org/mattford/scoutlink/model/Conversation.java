package uk.org.mattford.scoutlink.model;

import java.util.LinkedList;

public class Conversation {
	
	public String CONVERSATION_NAME;
	private LinkedList<Message> messages;
	private LinkedList<Message> buffer;

	public Conversation(String name) {
		this.CONVERSATION_NAME = name;
		this.messages = new LinkedList<Message>();
		this.buffer = new LinkedList<Message>();
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
		return message;
	}
	
	public boolean hasBuffer() {
		return buffer.size() != 0;
	}
	
	public void clearBuffer() {
		this.buffer.clear();
	}
	
	public void addMessage(Message msg) {
		//Log.d("ScoutLink", "Adding new message from : "+msg.sender+" to conversation.");
		buffer.add(msg);
	}
}
