package uk.org.mattford.scoutlink.irc;

import android.os.Binder;

public class IRCBinder extends Binder {
	
	private IRCService service;
	
	public IRCBinder(IRCService serv) {
		this.service = serv;
	}
	
	
	public IRCService getService() {
		return this.service;
	}

}
