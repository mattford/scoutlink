package uk.org.mattford.scoutlink.command.handler;

import android.content.Context;
import android.os.Handler;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;

public class SayHandler extends CommandHandler {

	public SayHandler(Context context) {
		super(context);
	}

	@Override
	public boolean validate(String[] params, Conversation conversation) {
		return params.length > 1;
	}

	@Override
	public void execute(String[] params, Conversation conversation, Handler backgroundHandler) {
		String message = StringUtils.join(Arrays.copyOfRange(params, 1, params.length), " ");
		backgroundHandler.post(() -> server.getConnection().sendIRC().message(conversation.getName(), message));
		Message msg = new Message(
				server.getConnection().getNick(),
				message,
				Message.SENDER_TYPE_SELF,
				Message.TYPE_MESSAGE
		);
		conversation.addMessage(msg);
	}

	@Override
	public String getUsage() {
		return "/say Message";
	}

	@Override
	public String getDescription() {
		return "Sends a message to the current channel";
	}

}
