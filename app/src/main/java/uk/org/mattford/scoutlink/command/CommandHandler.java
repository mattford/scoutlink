package uk.org.mattford.scoutlink.command;

import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Conversation;

public abstract class CommandHandler {
	
	public abstract void execute(String[] params, Conversation conversation, IRCService service);
	
	public abstract String getUsage();
	
	public abstract String getDescription();
	
	public String mergeParams(String[] params) {
		StringBuilder sb = new StringBuilder();
		sb.append(params[0]);
		for (int i = 1; i<params.length; i++) {
			sb.append(" ").append(params[i]);
		}
		return sb.toString();
	}

}
