package uk.org.mattford.scoutlink.model;

import org.pircbotx.User;

import java.util.ArrayList;

public class Channel extends Conversation {

	private org.pircbotx.Channel channel;

	public Channel(String name, org.pircbotx.Channel channel) {
		super(name);
		setType(TYPE_CHANNEL);
		this.channel = channel;
	}

	public ArrayList<User> getUsers() {
		return new ArrayList<>(this.channel.getUsers());
	}
}
