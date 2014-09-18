package uk.org.mattford.scoutlink.irc;

import org.jibble.pircbot.PircBot;

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
		Log.d("ScoutLink", "Sending broadcast...");
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
		Scoutlink.getInstance().getServer().getConversation(channel).addMessage(new Message(sender, message));
		Intent intent = new Intent();
		intent.setAction(Broadcast.NEW_MESSAGE);
		intent.putExtra("target", channel);
		service.sendBroadcast(intent);
	}
	
	public void onPrivateMessage() {
		
	}
	
	public void onNotice() {
		
	}
	
	public void onOp() {
		
	}
	
	public void onDeop() {
		
	}
	
	public void onVoice() {
		
	}
	
	public void onDevoice() {
		
	}
	
	public void onInvite() {
		
	}
	
	public void onJoin(String channel, String sender, String login, String hostname) {
		if (sender.equalsIgnoreCase(this.getNick())) {
			Conversation conv = new Conversation(channel);
			Scoutlink.getInstance().getServer().addConversation(conv);
			Intent intent = new Intent();
			intent.setAction(Broadcast.NEW_CONVERSATION);
			intent.putExtra("target", channel);
			service.sendBroadcast(intent);
		} else {
			Message msg = new Message(sender, "joined channel");
			Scoutlink.getInstance().getServer().getConversation(channel).addMessage(msg);
			Intent intent = new Intent();
			intent.setAction(Broadcast.NEW_MESSAGE);
			intent.putExtra("target", channel);
			service.sendBroadcast(intent);
		}
	}
	
	public void onMode() {
		
	}
	
	public void onKick() {
		
	}
	
	public void onNickChange() {
		
	}
	
	public void onPart() {
		
	}
	
	public void onQuit() {
		
	}
	
	public void onRemoveChannelBan() {
		
	}
	
	public void onServerResponse(int code, String message) {
		Scoutlink.getInstance().getServer().getConversation("ScoutLink").addMessage(new Message("ScoutLink", message));
		Intent intent = new Intent();
		intent.setAction(Broadcast.NEW_MESSAGE);
		intent.putExtra("target", "ScoutLink");
		service.sendBroadcast(intent);
	}
	
	


}
