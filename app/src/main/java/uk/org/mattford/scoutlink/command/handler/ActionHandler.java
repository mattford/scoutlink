package uk.org.mattford.scoutlink.command.handler;

import android.content.Context;
import android.os.Handler;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;

public class ActionHandler extends CommandHandler {

	public ActionHandler(Context context) {
		super(context);
	}

	@Override
	public boolean validate(String[] params, Conversation conversation) {
		return params.length > 1;
	}

	@Override
	public void execute(String[] params, Conversation conversation, Handler backgroundHandler) {
		String action = StringUtils.join(Arrays.copyOfRange(params, 1, params.length), " ");
		String nick = server.getConnection().getNick();
		backgroundHandler.post(() -> server.getConnection().sendIRC().action(conversation.getName(), action));
        Message msg = new Message(nick, action, Message.SENDER_TYPE_SELF, Message.TYPE_ACTION);
		conversation.addMessage(msg);
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
