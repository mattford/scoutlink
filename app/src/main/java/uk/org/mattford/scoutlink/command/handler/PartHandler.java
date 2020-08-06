package uk.org.mattford.scoutlink.command.handler;

import android.content.Context;
import android.os.Handler;

import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Server;

public class PartHandler extends CommandHandler {

	public PartHandler(Context context) {
		super(context);
	}

	@Override
	public void execute(String[] params, Conversation conversation, Handler backgroundHandler) {
		String channelToPart;
		if (params.length >= 2) {
			channelToPart = params[1];
		} else {
			channelToPart = conversation.getName();
		}
		Server server = Server.getInstance();
		Conversation target = server.getConversation(channelToPart);
		if (target == null || target.getType() != Conversation.TYPE_CHANNEL) {
			return;
		}
		backgroundHandler.post(() -> server.getConnection().getUserChannelDao().getChannel(channelToPart).send().part());
	}

	@Override
	public String getUsage() {
		return "/part #channel";
	}

	@Override
	public String getDescription() {
		return "Leaves a channel, if no channel is specified will default to the current active channel.";
	}

}
