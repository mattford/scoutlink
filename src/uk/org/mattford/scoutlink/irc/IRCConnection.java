package uk.org.mattford.scoutlink.irc;

import java.util.ArrayList;

import org.jibble.pircbot.PircBot;

import android.content.Intent;
import uk.org.mattford.scoutlink.ConversationsActivity;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Conversation;

public class IRCConnection extends PircBot {
	
	private IRCService service;
	private ArrayList<Conversation> conversations;
	
	public IRCConnection(IRCService service) {
		this.service = service;
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
		Conversation conversation = new Conversation("ScoutLink");
		this.service.join("#test");
	}
	
	public void onDisconnect() {
		this.service.updateNotification("Not connected");
	}
	
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
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
		Intent intent = new Intent();
		intent.setAction("uk.org.mattford.scoutlink.NEW_CONVERSATION");
		intent.putExtra("target", channel);
		service.sendBroadcast(intent);
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
	
	


}
