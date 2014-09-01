package uk.org.mattford.scoutlink.irc;

import org.jibble.pircbot.PircBot;

public class IRCConnection extends PircBot {
	
	IRCService service;
	
	public void registerService(IRCService service) {
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


}
