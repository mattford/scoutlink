package uk.org.mattford.scoutlink;

import uk.org.mattford.scoutlink.model.Server;

public class Scoutlink {
	private static Scoutlink instance;
	
	private Server server;
	
	public static Scoutlink getInstance() {
		if (instance == null) {
			instance = new Scoutlink();
		}
		return instance;
	}
	
	public Server getServer() {
		if (server == null) {
			server = new Server();
		}
		return server;
	}
}
