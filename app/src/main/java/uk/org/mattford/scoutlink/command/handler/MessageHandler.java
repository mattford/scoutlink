package uk.org.mattford.scoutlink.command.handler;

import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Conversation;

/**
 * Handles /msg command
 *
 * Created by Matt Ford on 04/04/2015.
 */
public class MessageHandler extends CommandHandler {
    @Override
    public void execute(String[] params, Conversation conversation, IRCService service) {
        if (params.length >= 3) {
            String target = params[1];
            StringBuilder messageBuilder = new StringBuilder();
            for (int i = 2; i < params.length; i++) {
                messageBuilder.append(params[i]);
                messageBuilder.append(" ");
            }
            new Thread(() -> service.getConnection().sendIRC().message(target, messageBuilder.toString())).start();
        }
    }

    @Override
    public String getUsage() {
        return "/msg <nickname> <message>";
    }

    @Override
    public String getDescription() {
        return "Sends a private message to a user";
    }
}
