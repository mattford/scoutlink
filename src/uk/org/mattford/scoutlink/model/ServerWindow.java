package uk.org.mattford.scoutlink.model;

public class ServerWindow extends Conversation {

	public ServerWindow(String name) {
		super(name);
		setType(TYPE_SERVER);
	}

}
