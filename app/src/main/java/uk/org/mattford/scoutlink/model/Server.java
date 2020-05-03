package uk.org.mattford.scoutlink.model;

import android.util.Log;

import org.pircbotx.PircBotX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Server {
	private ArrayList<OnConversationListChangedListener> listeners;
	private LinkedHashMap<String, Conversation> conversations;
	private int status = 0;
    private ArrayList<String> channelList = new ArrayList<>();
    private PircBotX bot;
	
	public final static int STATUS_DISCONNECTED = 0;
	public final static int STATUS_CONNECTED = 1;

	private static Server instance;
	public static Server getInstance() {
		if (instance == null) {
			instance = new Server();
		}
		return instance;
	}

	private Server() {
		this.conversations = new LinkedHashMap<>();
		this.listeners = new ArrayList<>();
	}
	
	public Conversation getConversation(String name) {
		if (conversations.containsKey(name)) {
			return conversations.get(name);
		}
		return null;
	}

    public ArrayList<String> getChannelList() {
        return channelList;
    }
	
	public int getStatus() {
		return this.status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public LinkedHashMap<String, Conversation> getConversations() {
		return this.conversations;
	}
	
	public void addConversation(Conversation conv) {
		conversations.put(conv.getName(), conv);
		onConversationListChanged();
	}
	
	public void removeConversation(String name) {
		conversations.remove(name);
		onConversationListChanged();
	}
	
	public void clearConversations() {
		conversations.clear();
		onConversationListChanged();
	}

	public boolean isConnected() {
		return this.bot != null && this.bot.isConnected();
	}

	public PircBotX getConnection() {
		return this.bot;
	}

	public void setConnection(PircBotX bot) {
		this.bot = bot;
	}

	public void addOnConversationListChangedListener(OnConversationListChangedListener listener) {
		listeners.add(listener);
	}

	private void onConversationListChanged() {
		for (OnConversationListChangedListener listener : listeners) {
			listener.onConversationListChanged(this.conversations);
		}
	}

	public interface OnConversationListChangedListener {
		void onConversationListChanged(HashMap<String, Conversation> conversationHashMap);
	}
}
