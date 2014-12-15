package uk.org.mattford.scoutlink.command.handler;

import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Conversation;

public class QuitHandler extends CommandHandler {

	@Override
	public void execute(String[] params, Conversation conversation,
			IRCService service) {
		
		if (params.length > 2) {
			String reason = params[2];
			service.getConnection().sendIRC().quitServer(reason);
		} else {
			service.getConnection().sendIRC().quitServer();
		}
		
		
	}

	@Override
	public String getUsage() {
		return "/quit <message>";
	}

	@Override
	public String getDescription() {
		return "Disconnects from ScoutLink with an optional quit message.";
	}

}
