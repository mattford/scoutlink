package uk.org.mattford.scoutlink.command.handler;

import android.content.Context;
import android.os.Handler;

import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Conversation;

public class PartHandler extends CommandHandler {

	public PartHandler(Context context) {
		super(context);
	}

	@Override
	public void execute(String[] params, Conversation conversation, Handler backgroundHandler) {

		if (params.length < 2) {
			return;
		}

		String channelToPart = params[1];
		//String channelPartReason = params[2];
		backgroundHandler.post(() -> server.getConnection().getUserChannelDao().getChannel(channelToPart).send().part());
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
