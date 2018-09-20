package uk.org.mattford.scoutlink.command.handler;

import android.graphics.Color;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;

public class ActionHandler extends CommandHandler {

	@Override
	public void execute(String[] params, Conversation conversation,
			IRCService service) {
		String action;
		String nick = service.getConnection().getNick();
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
		new Thread(() -> service.getConnection().sendIRC().action(conversation.getName(), action)).start();
        Message msg = new Message(nick, service.getString(R.string.message_action, action));
        msg.setBackgroundColour(service.getResources().getColor(R.color.scoutlink_blue));
        msg.setColour(Color.WHITE);
        msg.setAlignment(Message.ALIGN_RIGHT);
		conversation.addMessage(msg);
		service.onNewMessage(conversation.getName());
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
