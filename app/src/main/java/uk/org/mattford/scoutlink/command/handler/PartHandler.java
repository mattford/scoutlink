package uk.org.mattford.scoutlink.command.handler;

import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Conversation;

public class PartHandler extends CommandHandler {

	@Override
	public void execute(String[] params, 
			Conversation conversation, IRCService service) {

		String channelToPart = params[1];
		//String channelPartReason = params[2];
		service.getConnection().partChannel(channelToPart);
		
	}

	@Override
	public String getUsage() {
		return "/part #channel";
	}

	@Override
	public String getDescription() {
		return "Leaves a channel";
	}

}
