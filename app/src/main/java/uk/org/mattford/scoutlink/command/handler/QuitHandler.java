package uk.org.mattford.scoutlink.command.handler;

import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Settings;

public class QuitHandler extends CommandHandler {

	@Override
	public void execute(String[] params, Conversation conversation,
			IRCService service) {
		
		if (params.length > 1) {
			String reason = params[1];
			new Thread(() -> service.getConnection().sendIRC().quitServer(reason)).start();
		} else {
			Settings settings = new Settings(service);
			String quitMessage = settings.getString("quit_message");
			new Thread(() -> service.getConnection().sendIRC().quitServer(quitMessage)).start();
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
