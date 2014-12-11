package uk.org.mattford.scoutlink.model;

import java.util.LinkedList;

public class Conversation {
	
	public String CONVERSATION_NAME;
	private String type;
	private LinkedList<Message> messages;
	private LinkedList<Message> buffer;
	
	public final static String TYPE_CHANNEL = "uk.org.mattford.scoutlink.Conversation.TYPE_CHANNEL";
	public final static String TYPE_QUERY = "uk.org.mattford.scoutlink.Conversation.TYPE_QUERY";
	public final static String TYPE_SERVER = "uk.org.mattford.scoutlink.Conversation.TYPE_SERVER";
	
	

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
		messages.add(message);
		return message;
	}
	
	public boolean hasBuffer() {
		return buffer.size() != 0;
	}
	
	public void clearBuffer() {
		this.buffer.clear();
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}
	
	public void addMessage(Message msg) {
		//Log.d("ScoutLink", "Adding new message from : "+msg.sender+" to conversation.");
		buffer.add(msg);
	}
}
