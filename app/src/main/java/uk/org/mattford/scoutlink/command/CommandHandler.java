package uk.org.mattford.scoutlink.command;

import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Conversation;

public abstract class CommandHandler {

	public CommandHandler() {

	}
	
	public abstract void execute(String[] params, Conversation conversation, IRCService service);
	
	public abstract String getUsage();
	
	public abstract String getDescription();
	

}
