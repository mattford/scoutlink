package uk.org.mattford.scoutlink.command.handler;

import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Conversation;

public class JoinHandler extends CommandHandler {

	@Override
	public void execute(String[] params, 
			Conversation conversation, IRCService service) {

		String channelToJoin = params[1];
		if (params.length > 2) {
			String key = params[2];
			new Thread(() -> service.getConnection().sendIRC().joinChannel(channelToJoin, key)).start();
		} else {
			new Thread(() -> service.getConnection().sendIRC().joinChannel(channelToJoin)).start();
		}
		
	}

	@Override
	public String getUsage() {
		return "/join #channel channelkey(optional)";
	}

	@Override
	public String getDescription() {
		return "Joins a new channel.";
	}

}
