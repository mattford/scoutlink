package uk.org.mattford.scoutlink.command.handler;

import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Conversation;

public class PartHandler extends CommandHandler {

	@Override
	public void execute(String[] params, 
			Conversation conversation, IRCService service) {

		if (params.length < 2) {
			return;
		}

		String channelToPart = params[1];
		//String channelPartReason = params[2];
		new Thread(() -> service.getConnection().getUserChannelDao().getChannel(channelToPart).send().part()).start();
		
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
