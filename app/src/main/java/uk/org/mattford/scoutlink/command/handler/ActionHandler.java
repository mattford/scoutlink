package uk.org.mattford.scoutlink.command.handler;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;

public class ActionHandler extends CommandHandler {

	public ActionHandler(Context context) {
		super(context);
	}

	@Override
	public void execute(String[] params, Conversation conversation, Handler backgroundHandler) {
		String action;
		String nick = server.getConnection().getNick();
		if (params.length > 2) {
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i < params.length; i++) {
				sb.append(params[i]);
                sb.append(" ");
			}
			action = sb.toString();
		} else {
			action = params[1];
		}
		backgroundHandler.post(() -> server.getConnection().sendIRC().action(conversation.getName(), action));
        Message msg = new Message(nick, context.getString(R.string.message_action, action));
        msg.setAlignment(Message.ALIGN_RIGHT);
		conversation.addMessage(msg);
		Intent intent = new Intent().setAction(Broadcast.NEW_MESSAGE).putExtra("target", conversation.getName());
		context.sendBroadcast(intent);
	}

	@Override
	public String getUsage() {
		return "/me <action>";
	}

	@Override
	public String getDescription() {
		return "Sends an action to a channel, e.g., Fordy goes to the shops";
	}

}
