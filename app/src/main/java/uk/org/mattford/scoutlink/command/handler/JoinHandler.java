package uk.org.mattford.scoutlink.command.handler;

import android.content.Context;
import android.os.Handler;

import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Conversation;

public class JoinHandler extends CommandHandler {

	public JoinHandler(Context context) {
		super(context);
	}

	@Override
	public void execute(String[] params, Conversation conversation, Handler backgroundHandler) {

		String channelToJoin = params[1];
		if (params.length > 2) {
			String key = params[2];
			backgroundHandler.post(() -> server.getConnection().sendIRC().joinChannel(channelToJoin, key));
		} else {
			backgroundHandler.post(() -> server.getConnection().sendIRC().joinChannel(channelToJoin));
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
