package uk.org.mattford.scoutlink.command.handler;

import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Conversation;

public class NickHandler extends CommandHandler {

	@Override
	public void execute(String[] params, Conversation conversation,
			IRCService service) {
		
		if (params.length > 1) {
			String nickname = params[1];
			service.getConnection().sendIRC().changeNick(nickname);
		}
		
		
	}

	@Override
	public String getUsage() {
		return "/nick <newnick>";
	}

	@Override
	public String getDescription() {
		return "Changes your nickname.";
	}

}
