package uk.org.mattford.scoutlink.irc;

import java.util.ArrayList;

import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import android.content.Intent;
import android.util.Log;
import uk.org.mattford.scoutlink.Scoutlink;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.model.Server;

public class IRCConnection extends PircBot {
	
	private IRCService service;
	private Server server;
	
	private final String logTag = "ScoutLink/IRCConnection";
	
	public IRCConnection(IRCService service) {
		this.service = service;
		this.server = Scoutlink.getInstance().getServer();

	}
	
	public void createDefaultConversation() {
		Conversation serverConv = new Conversation("ScoutLink");
		server.addConversation(serverConv);
		Intent intent = new Intent().setAction(Broadcast.NEW_CONVERSATION).putExtra("target", "ScoutLink");
		service.sendBroadcast(intent);
	}
	
	public void setNickname(String nick) {
		this.setName(nick);
	}
	
	public void setIdent(String ident) {
		this.setLogin(ident);
	}
	
	public void setRealName(String name) {
		this.setVersion(name);
	}
	
	
	public void onConnect() {
		this.service.updateNotification("Connected as " + this.getNick());

		this.joinChannel("#test");
	}
	
	public void onDisconnect() {
		Log.v(logTag, "Disconnected from ScoutLink");
		this.service.updateNotification("Not connected");
	}
	
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		Message msg = new Message("<"+sender+"> "+message);
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel); 
	}
	
	public void onAction(String sender, String login, String hostname, String target, String action) {
		Message msg = new Message(sender + " " + action);
		if (target.startsWith("#")) {
			server.getConversation(target).addMessage(msg);
			sendNewMessageBroadcast(target);
		} else {
			// It's a private message.
			Conversation conversation = server.getConversation(sender);
			if (conversation == null) {
				conversation = new Conversation(sender);
				server.addConversation(conversation);
				Intent intent = new Intent().setAction(Broadcast.NEW_CONVERSATION).putExtra("target", sender);
				service.sendBroadcast(intent);
			}
			conversation.addMessage(msg);
			sendNewMessageBroadcast(sender);
		}
	}
	
	public void onPrivateMessage(String sender, String login, String hostname, String message) { // TODO: Might need to create conversation
		Conversation conversation = server.getConversation(sender);
		if (conversation == null) {
			conversation = new Conversation(sender);
			server.addConversation(conversation);
			Intent intent = new Intent().setAction(Broadcast.NEW_CONVERSATION).putExtra("target", sender);
			service.sendBroadcast(intent);
		}
		Message msg = new Message("<"+sender+"> "+message);
		conversation.addMessage(msg);
		sendNewMessageBroadcast(sender);
	}
	
	public void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
		Message msg = new Message("-"+sourceNick+"- "+notice);
		server.getConversation("ScoutLink").addMessage(msg);
		sendNewMessageBroadcast("ScoutLink");
	}
	
	public void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		Message msg = null;
		if (recipient.equals(this.getNick())) {
			msg = new Message(sourceNick + " gave you operator status!");
		} else {
			msg = new Message(sourceNick + " gave operator status to "+ recipient);
		}
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel); 
	}
	
	public void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		Message msg = null;
		if (recipient.equals(this.getNick())) {
			msg = new Message(sourceNick + " took away your operator status!");
		} else {
			msg = new Message(sourceNick + " took operator status from "+ recipient);
		}
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel); 
	}
	
	public void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		Message msg = null;
		if (recipient.equals(this.getNick())) {
			msg = new Message(sourceNick + " gave you voice!");
		} else {
			msg = new Message(sourceNick + " gave voice to "+ recipient);
		}
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel); 
	}
	
	public void onDevoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		Message msg = null;
		if (recipient.equals(this.getNick())) {
			msg = new Message(sourceNick + " took your voice status!");
		} else {
			msg = new Message(sourceNick + " took voice from "+ recipient);
		}
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel); 
	}
	
	public void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) {
		Intent intent = new Intent().setAction(Broadcast.INVITE).putExtra("target", channel);
		service.sendBroadcast(intent);
	}
	
	public void onJoin(String channel, String sender, String login, String hostname) {
		if (sender.equalsIgnoreCase(this.getNick())) {
			Conversation conv = new Conversation(channel);
			server.addConversation(conv);
			Intent intent = new Intent().setAction(Broadcast.NEW_CONVERSATION).putExtra("target", channel);
			service.sendBroadcast(intent);
		} else {
			Message msg = new Message(sender+" has joined "+channel);
			server.getConversation(channel).addMessage(msg);
			sendNewMessageBroadcast(channel); 
		}
	}
	
	public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		// Do nothing to avoid duplicated channel events. 
	}
	
	public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
		if (recipientNick.equals(this.getNick())) {
			// We were kicked from a channel.
			Message msg = new Message("You were kicked from "+channel+ " by "+ kickerNick + "("+reason+")");
			server.getConversation("ScoutLink").addMessage(msg);
			sendNewMessageBroadcast("ScoutLink");
			server.removeConversation(channel);
			Intent intent = new Intent().setAction(Broadcast.REMOVE_CONVERSATION).putExtra("target", channel);
			service.sendBroadcast(intent);
		} else {
			Message msg = new Message(recipientNick+" was kicked from "+channel+" by "+kickerNick+" ("+reason+")");
			server.getConversation(channel).addMessage(msg);
			sendNewMessageBroadcast(channel);
		}
	}
	
	public void onNickChange(String oldNick, String login, String hostname, String newNick) {
		Message msg = new Message(oldNick+" changed their nick to " + newNick);
		for (String channel : getConversationsContainingUser(newNick)) {
			server.getConversation(channel).addMessage(msg);
			sendNewMessageBroadcast(channel);
		}
	}
	
	public void onPart(String channel, String sender, String login, String hostname) {
		if (sender.equals(this.getNick())) {
			// We left a channel.
			server.removeConversation(channel);
			Intent intent = new Intent().setAction(Broadcast.REMOVE_CONVERSATION).putExtra("target", channel);
			service.sendBroadcast(intent);
		} else {
			Message msg = new Message(sender+ " has left " + channel);
			server.getConversation(channel).addMessage(msg);
			sendNewMessageBroadcast(channel); 			
		}

	}
	
	public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
		if (sourceNick.equals(this.getNick())) {
			// We have quit, do nothing.
			return;
		}
		Message msg = new Message(sourceNick+" has left ScoutLink ("+reason+")");
		for (String channel : getConversationsContainingUser(sourceNick)) {
			server.getConversation(channel).addMessage(msg);
			sendNewMessageBroadcast(channel);
		}
	}
	
	public void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
		Message msg = new Message(sourceNick+" has unbanned "+hostmask);
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel);
	}
	
	public void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
		Message msg = new Message(sourceNick+" has removed the channel key.");
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel);
	}
	
	public void onRemoveChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Message msg = new Message(sourceNick+" has removed the channel user limit.");
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel);
	}
	
	public void onRemoveInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Message msg = new Message(sourceNick +" has removed invite-only mode.");
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel);
	}
	
	public void onRemoveModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Message msg = new Message(sourceNick+" has disabled moderated mode.");
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel);
	}
	
	public void onRemoveNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Message msg = new Message(sourceNick+" has disallowed external messages.");
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel);
	}
	
	public void onRemovePrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Message msg = new Message(sourceNick+" has disabled private mode.");
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel);
	}
	
	public void onRemoveSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Message msg = new Message(sourceNick+" has disabled secret mode.");
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel);
	}
	
	public void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Message msg = new Message(sourceNick+" has disabled topic protection mode.");
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel);
	}
	
	public void onServerPing(String response) {
		Log.v(logTag, "Responding to server ping.");
		super.onServerPing(response);
	}
	
	public void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) {
		Log.v(logTag, "Received PING from "+sourceNick);
		Message msg = new Message(sourceNick+" pinged you!");
		server.getConversation("ScoutLink").addMessage(msg);
		sendNewMessageBroadcast("ScoutLink");
		super.onPing(sourceNick, sourceLogin, sourceHostname, target, pingValue);
	}
	
	public void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
		Message msg = new Message(sourceNick+" has banned "+hostmask);
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel);
	}
	
	public void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
		Message msg = new Message(sourceNick+ " has set the channel key to: "+key);
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel);
	}
	
	public void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit) {
		Message msg = new Message(sourceNick+" has set the channel user limit to: "+Integer.toString(limit));
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel);
	}
	
	public void onSetInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Message msg = new Message(sourceNick+" has set the channel to invite only.");
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel);
	}
	
	public void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Message msg = new Message(sourceNick+" has enabled moderated mode.");
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel);
	}
	
	public void onSetNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Message msg = new Message(sourceNick+" has disallowed external messages.");
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel);
	}
	
	public void onSetPrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Message msg = new Message(sourceNick+" has enabled private mode.");
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel);
	}
	
	public void onSetSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Message msg = new Message(sourceNick+" has enabled secret mode.");
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel);
	}
	
	public void onSetTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		Message msg = new Message(sourceNick+" has enabled topic protection.");
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel);
	}
	
	public void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
		Message msg = null;
		if (!changed) {
			msg = new Message("Topic of "+channel+" is "+topic+" (set by "+setBy+")");
		} else {
			msg = new Message(setBy+" changed the topic to "+topic);
		}
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel); 
	}
	
	public void onUserList(String channel, User[] users) {
		String userList = "";
		for (User user : users) {
			userList = userList + " " + user.getNick();
		}
		Message msg = new Message("Users on channel: " + userList);
		server.getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel); 
	}
	
	public void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		Message msg = new Message(sourceNick+ " sets mode: "+mode+" "+targetNick);
		server.getConversation("ScoutLink").addMessage(msg);
		sendNewMessageBroadcast("ScoutLink");
	}
	
	public void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		this.sendNotice(sourceNick, "ScoutLink for Android");
	}
	
	public void onServerResponse(int code, String message) {
		Message msg = new Message(message);
		server.getConversation("ScoutLink").addMessage(msg);
		sendNewMessageBroadcast("ScoutLink"); 
	}
	
	public void sendNewMessageBroadcast(String target) {
		Intent intent = new Intent().setAction(Broadcast.NEW_MESSAGE).putExtra("target", target);
		service.sendBroadcast(intent);
	}
	
	public ArrayList<String> getConversationsContainingUser(String u) {
		String[] channels = getChannels();
		ArrayList<String> returnChans = new ArrayList<String>();
		for (String channel : channels) {
			User[] users = getUsers(channel);
			for (User user : users) {
				if (user.getNick().equals(u)) {
					returnChans.add(channel);
				}
			}
		}
		return returnChans;
	}
	
	public ArrayList<String> getUsersAsStringArray(String target) {
		User[] users = getUsers(target);
		ArrayList<String> userlist = new ArrayList<String>();
		for (User user : users) {
			userlist.add(user.getPrefix()+user.getNick());
		}
		return userlist;
	}
	
	


}
