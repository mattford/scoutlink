package uk.org.mattford.scoutlink.irc;

import org.jibble.pircbot.PircBot;

public class IRCConnection extends PircBot {
	
	IRCService service;
	
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
	}
	
	public void onDisconnect() {
		this.service.updateNotification("Not connected");
	}
	
	public void onMessage() {
		
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
	
	public void onJoin() {
		
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
