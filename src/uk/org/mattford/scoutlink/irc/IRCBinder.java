package uk.org.mattford.scoutlink.irc;

import android.os.Binder;
import android.widget.Toast;

public class IRCBinder extends Binder {
	
	private IRCService service;
	
	public IRCBinder(IRCService service) {
		this.service = service;
	}
	
	public void connect(String nick, String ident, String gecos) {
		this.service.connect(nick,  ident,  gecos);
	}
	
	public IRCService getService() {
		return this.service;
	}

}
