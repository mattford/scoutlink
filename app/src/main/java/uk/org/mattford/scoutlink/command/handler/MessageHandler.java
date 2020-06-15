package uk.org.mattford.scoutlink.command.handler;

import android.content.Context;
import android.os.Handler;

import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.model.Query;
import uk.org.mattford.scoutlink.model.Server;

/**
 * Handles /msg command
 *
 * Created by Matt Ford on 04/04/2015.
 */
public class MessageHandler extends CommandHandler {

    public MessageHandler(Context context) {
        super(context);
    }

    @Override
    public void execute(String[] params, Conversation conversation, Handler backgroundHandler) {
        if (params.length >= 3) {
            String target = params[1];
            StringBuilder messageBuilder = new StringBuilder();
            for (int i = 2; i < params.length; i++) {
                messageBuilder.append(params[i]);
                messageBuilder.append(" ");
            }
            String messageText = messageBuilder.toString();
            Message message = new Message(
                messageBuilder.toString(),
                Message.SENDER_TYPE_SELF,
                Message.TYPE_MESSAGE
            );
            Server server = Server.getInstance();
            Conversation targetConversation = server.getConversation(target);
            if (targetConversation == null) {
                targetConversation = new Query(target);
                server.addConversation(targetConversation, true);
            }
            targetConversation.addMessage(message);
            backgroundHandler.post(() -> server.getConnection().sendIRC().message(target, messageText));
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
