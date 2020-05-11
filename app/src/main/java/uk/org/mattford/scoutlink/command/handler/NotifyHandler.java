package uk.org.mattford.scoutlink.command.handler;

import android.content.Context;
import android.os.Handler;

import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.model.Conversation;

public class NotifyHandler extends CommandHandler {
    public NotifyHandler(Context context) {
        super(context);
    }

    @Override
    public void execute(String[] params, Conversation conversation, Handler backgroundHandler) {
        if (params.length == 2) {
            if (params[1].equals("-l")) {
                // List
                backgroundHandler.post(() -> server.getConnection().sendRaw().rawLineNow("WATCH"));
            } else {
                // Add
                backgroundHandler.post(() -> server.getConnection().sendRaw().rawLineNow("WATCH "+server.getConnection().getNick()+" +"+params[1]));
            }
        } else if (params.length == 3) {
            if (params[1].equals("-r")) {
                // Remove
                backgroundHandler.post(() -> server.getConnection().sendRaw().rawLineNow("WATCH "+server.getConnection().getNick()+" -"+params[2]));
            }
        }
    }

    @Override
    public String getUsage() {
        return "/notify [-rl] <nickname> (-r removes from notify list, -l shows the list)";
    }

    @Override
    public String getDescription() {
        return "Notifies you when a user connects or disconnects, or marks themselves away.";
    }
}
