package uk.org.mattford.scoutlink.command.handler;

import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Conversation;

public class NotifyHandler extends CommandHandler {
    @Override
    public void execute(String[] params, Conversation conversation, IRCService service) {
        if (params.length == 2) {
            if (params[1].equals("-l")) {
                // List
                new Thread(() -> service.getConnection().sendRaw().rawLineNow("WATCH")).start();
            } else {
                // Add
                new Thread(() -> service.getConnection().sendRaw().rawLineNow("WATCH "+service.getConnection().getNick()+" +"+params[1])).start();
            }
        } else if (params.length == 3) {
            if (params[1].equals("-r")) {
                // Remove
                new Thread(() -> service.getConnection().sendRaw().rawLineNow("WATCH "+service.getConnection().getNick()+" -"+params[2])).start();
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
