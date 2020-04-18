package uk.org.mattford.scoutlink.command.handler;

import android.content.Context;
import android.os.Handler;

import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Settings;

public class QuitHandler extends CommandHandler {
	public QuitHandler(Context context) {
		super(context);
	}

	@Override
	public void execute(String[] params, Conversation conversation, Handler backgroundHandler) {
		
		if (params.length > 1) {
			String reason = params[1];
			backgroundHandler.post(() -> server.getConnection().sendIRC().quitServer(reason));
		} else {
			Settings settings = new Settings(context);
			String quitMessage = settings.getString("quit_message");
			backgroundHandler.post(() -> server.getConnection().sendIRC().quitServer(quitMessage));
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
