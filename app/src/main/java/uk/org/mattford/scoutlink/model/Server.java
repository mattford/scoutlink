package uk.org.mattford.scoutlink.model;

import org.pircbotx.ChannelListEntry;
import org.pircbotx.PircBotX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Server {
	private final ArrayList<OnConversationListChangedListener> onConversationListChangedListeners;
	private final ArrayList<OnConnectionStatusChangedListener> onConnectionStatusChangedListeners;
	private final ArrayList<OnActiveConversationChangedListener> onActiveConversationChangedListeners;
	private final LinkedHashMap<String, Conversation> conversations;
	private int status = 0;
    private PircBotX bot;
    private List<ChannelListEntry> channelList;
	
	public final static int STATUS_DISCONNECTED = 0;
	public final static int STATUS_CONNECTING = 1;
	public final static int STATUS_CONNECTED = 2;

	private static Server instance;
	public static Server getInstance() {
		if (instance == null) {
			instance = new Server();
		}
		return instance;
	}

	private Server() {
		this.conversations = new LinkedHashMap<>();
		this.channelList = new ArrayList<>();
		this.onConversationListChangedListeners = new ArrayList<>();
		this.onConnectionStatusChangedListeners = new ArrayList<>();
		this.onActiveConversationChangedListeners = new ArrayList<>();
	}
	
	public Conversation getConversation(String name) {
		if (conversations.containsKey(name.toLowerCase())) {
			return conversations.get(name.toLowerCase());
		}
		return null;
	}
	
	public int getStatus() {
		return this.status;
	}
	
	public void setStatus(int status) {
		this.status = status;
		onConnectionStatusChanged();
	}
	
	public LinkedHashMap<String, Conversation> getConversations() {
		return this.conversations;
	}

	public void addConversation(Conversation conversation, boolean active) {
		conversations.put(conversation.getName().toLowerCase(), conversation);
		onConversationListChanged();
		if (active) {
			onActiveConversationChanged(conversation);
		}
	}

	public void addConversation(Conversation conversation) {
		addConversation(conversation, false);
	}
	
	public void removeConversation(String name) {
		conversations.remove(name.toLowerCase());
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
		onConversationListChangedListeners.add(listener);
	}

	private void onConversationListChanged() {
		for (OnConversationListChangedListener listener : onConversationListChangedListeners) {
			listener.onConversationListChanged(this.conversations);
		}
	}

	public void setChannelList(List<ChannelListEntry> channelList) {
		this.channelList = channelList;
	}

	public List<ChannelListEntry> getChannelList() {
		return this.channelList;
	}

	public interface OnConversationListChangedListener {
		void onConversationListChanged(HashMap<String, Conversation> conversationHashMap);
	}

	public void addOnConnectionStatusChangedListener(OnConnectionStatusChangedListener listener) {
		onConnectionStatusChangedListeners.add(listener);
	}

	private void onConnectionStatusChanged() {
		for (OnConnectionStatusChangedListener listener: onConnectionStatusChangedListeners) {
			listener.onConnectionStatusChanged();
		}
	}

	public interface OnConnectionStatusChangedListener {
		void onConnectionStatusChanged();
	}

	public void addOnActiveConversationChangedListener(OnActiveConversationChangedListener listener) {
		onActiveConversationChangedListeners.add(listener);
	}

	private void onActiveConversationChanged(Conversation conversation) {
		for (OnActiveConversationChangedListener listener: onActiveConversationChangedListeners) {
			listener.onActiveConversationChanged(conversation);
		}
	}

	public interface OnActiveConversationChangedListener {
		void onActiveConversationChanged(Conversation conversation);
	}
}
