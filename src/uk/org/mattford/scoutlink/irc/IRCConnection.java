package uk.org.mattford.scoutlink.irc;

import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import android.content.Intent;
import android.util.Log;
import uk.org.mattford.scoutlink.Scoutlink;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;

public class IRCConnection extends PircBot {
	
	private IRCService service;
	
	public IRCConnection(IRCService service) {
		this.service = service;
		Conversation serverConv = new Conversation("ScoutLink");
		Scoutlink.getInstance().getServer().addConversation(serverConv);
		Intent intent = new Intent();
		intent.setAction(Broadcast.NEW_CONVERSATION);
		intent.putExtra("target", "ScoutLink");
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
		this.service.updateNotification("Not connected");
	}
	
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		Message msg = new Message("<"+sender+"> "+message);
		Scoutlink.getInstance().getServer().getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel); 
	}
	
	public void onPrivateMessage(String sender, String login, String hostname, String message) { // TODO: Might need to create conversation
		Message msg = new Message("<"+sender+"> "+message);
		Scoutlink.getInstance().getServer().getConversation(sender).addMessage(msg);
		sendNewMessageBroadcast(sender); 
	}
	
	public void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
		Message msg = new Message("-"+sourceNick+"- "+notice);
		Scoutlink.getInstance().getServer().getConversation("ScoutLink").addMessage(msg);
		sendNewMessageBroadcast("ScoutLink");
	}
	
	public void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		Message msg = null;
		if (recipient.equals(this.getNick())) {
			msg = new Message(sourceNick + " gave you operator status!");
		} else {
			msg = new Message(sourceNick + " gave operator status to "+ recipient);
		}
		Scoutlink.getInstance().getServer().getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel); 
	}
	
	public void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		Message msg = null;
		if (recipient.equals(this.getNick())) {
			msg = new Message(sourceNick + " took away your operator status!");
		} else {
			msg = new Message(sourceNick + " took operator status from "+ recipient);
		}
		Scoutlink.getInstance().getServer().getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel); 
	}
	
	public void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		Message msg = null;
		if (recipient.equals(this.getNick())) {
			msg = new Message(sourceNick + " gave you voice!");
		} else {
			msg = new Message(sourceNick + " gave voice to "+ recipient);
		}
		Scoutlink.getInstance().getServer().getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel); 
	}
	
	public void onDevoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		Message msg = null;
		if (recipient.equals(this.getNick())) {
			msg = new Message(sourceNick + " took your voice status!");
		} else {
			msg = new Message(sourceNick + " took voice from "+ recipient);
		}
		Scoutlink.getInstance().getServer().getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel); 
	}
	
	public void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) {
		
	}
	
	public void onJoin(String channel, String sender, String login, String hostname) {
		if (sender.equalsIgnoreCase(this.getNick())) {
			Conversation conv = new Conversation(channel);
			Scoutlink.getInstance().getServer().addConversation(conv);
			Intent intent = new Intent().setAction(Broadcast.NEW_CONVERSATION).putExtra("target", channel);
			service.sendBroadcast(intent);
		} else {
			Message msg = new Message(sender+" has joined "+channel);
			Scoutlink.getInstance().getServer().getConversation(channel).addMessage(msg);
			sendNewMessageBroadcast(channel); 
		}
	}
	
	public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		Message msg = new Message(sourceNick+" sets mode: "+mode);
		Scoutlink.getInstance().getServer().getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel); 
	}
	
	public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
		
	}
	
	public void onNickChange(String oldNick, String login, String hostname, String newNick) {
		// ...
	}
	
	public void onPart(String channel, String sender, String login, String hostname) {
		Message msg = new Message(sender+ " has left " + channel);
		Scoutlink.getInstance().getServer().getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel); 
	}
	
	public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
		
	}
	
	public void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
		
	}
	
	public void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
		
	}
	
	public void onRemoveChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		
	}
	
	public void onRemoveInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		
	}
	
	public void onRemoveModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		
	}
	
	public void onRemoveNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		
	}
	
	public void onRemovePrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		
	}
	
	public void onRemoveSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		
	}
	
	public void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		
	}
	
	public void onServerPing(String response) {
		
	}
	
	public void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
		
	}
	
	public void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
		
	}
	
	public void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit) {
		
	}
	
	public void onSetInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		
	}
	
	public void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		
	}
	
	public void onSetNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		
	}
	
	public void onSetPrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		
	}
	
	public void onSetSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		
	}
	
	public void onSetTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		
	}
	
	public void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
		Message msg = null;
		if (!changed) {
			msg = new Message("Topic of "+channel+" is "+topic+" (set by "+setBy+")");
		} else {
			msg = new Message(setBy+" changed the topic to "+topic);
		}
		Scoutlink.getInstance().getServer().getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel); 
	}
	
	public void onUserList(String channel, User[] users) {
		String userList = "";
		for (User user : users) {
			userList = userList + " " + user.getNick();
		}
		Message msg = new Message("Users on channel: " + userList);
		Scoutlink.getInstance().getServer().getConversation(channel).addMessage(msg);
		sendNewMessageBroadcast(channel); 
	}
	
	public void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		
	}
	
	public void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		
	}
	
	public void onServerResponse(int code, String message) {
		Message msg = new Message(message);
		Scoutlink.getInstance().getServer().getConversation("ScoutLink").addMessage(msg);
		sendNewMessageBroadcast("ScoutLink"); 
	}
	
	private void sendNewMessageBroadcast(String target) {
		Intent intent = new Intent().setAction(Broadcast.NEW_MESSAGE).putExtra("target", target);
		service.sendBroadcast(intent);
	}
	
	


}
