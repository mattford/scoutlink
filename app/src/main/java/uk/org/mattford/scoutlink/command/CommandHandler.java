package uk.org.mattford.scoutlink.command;

import android.content.Context;
import android.os.Handler;

import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Server;

public abstract class CommandHandler {
	protected Server server;
	protected Context context;

	public CommandHandler(Context context) {
		server = Server.getInstance();
		this.context = context;
	}
	
	public abstract void execute(String[] params, Conversation conversation, Handler backgroundHandler);
	
	public abstract String getUsage();
	
	public abstract String getDescription();
}
