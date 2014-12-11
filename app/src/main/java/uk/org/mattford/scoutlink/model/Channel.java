package uk.org.mattford.scoutlink.model;

public class Channel extends Conversation {

	public Channel(String name) {
		super(name);
		setType(TYPE_CHANNEL);
	}

}
