package uk.org.mattford.scoutlink.model;

import java.util.ArrayList;

public class Channel extends Conversation {

	private final org.pircbotx.Channel channel;

	public Channel(String name, org.pircbotx.Channel channel) {
		super(name);
		setType(TYPE_CHANNEL);
		this.channel = channel;
		onUserListChanged();
	}

	public org.pircbotx.Channel getChannel() {
		return this.channel;
	}

	@Override
	public void onUserListChanged() {
		usersLiveData.postValue(new ArrayList<>(this.channel.getUsers()));
	}
}
