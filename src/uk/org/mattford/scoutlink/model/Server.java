package uk.org.mattford.scoutlink.model;

import java.util.HashMap;

import android.util.Log;


public class Server {

	private HashMap<String, Conversation> conversations;
	
	private final String logTag = "ScoutLink/Model/Server";
	
	public Server() {
		this.conversations = new HashMap<String, Conversation>();
	}
	
	public Conversation getConversation(String name) {
		Log.d(logTag, "getConversation("+name+")");
		if (conversations.containsKey(name)) {
			return conversations.get(name);
		} else {
			return null;
		}
	}
	
	public HashMap<String, Conversation> getConversations() {
		return this.conversations;
	}
	
	public void addConversation(Conversation conv) {
		conversations.put(conv.getName(), conv);
	}
	
	public void removeConversation(String name) {
		conversations.remove(name);
	}
	
	public void clearConversations() {
		conversations.clear();
		conversations.put("ScoutLink", new Conversation("ScoutLink"));
	}
	
}