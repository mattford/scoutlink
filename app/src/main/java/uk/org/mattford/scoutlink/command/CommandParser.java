package uk.org.mattford.scoutlink.command;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import java.util.HashMap;
import java.util.Locale;

import uk.org.mattford.scoutlink.command.handler.ActionHandler;
import uk.org.mattford.scoutlink.command.handler.JoinHandler;
import uk.org.mattford.scoutlink.command.handler.MessageHandler;
import uk.org.mattford.scoutlink.command.handler.NickHandler;
import uk.org.mattford.scoutlink.command.handler.NotifyHandler;
import uk.org.mattford.scoutlink.command.handler.PartHandler;
import uk.org.mattford.scoutlink.command.handler.QuitHandler;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Server;

public class CommandParser {
	
	private static CommandParser instance;
	private Server server;
	private Context context;
	
	private HashMap<String, CommandHandler> commands;
	private HashMap<String, String> aliases;
	
	private CommandParser(Context context) {
		commands = new HashMap<>();
		aliases = new HashMap<>();
		server = Server.getInstance();
		this.context = context;
		
		commands.put("join", new JoinHandler(context));
		commands.put("part", new PartHandler(context));
		commands.put("nick", new NickHandler(context));
		commands.put("quit", new QuitHandler(context));
		commands.put("me", new ActionHandler(context));
		commands.put("notify", new NotifyHandler(context));
		commands.put("msg", new MessageHandler(context));
		
		aliases.put("j", "join");
		aliases.put("p", "part");
		aliases.put("n", "nick");
		aliases.put("q", "quit");
		aliases.put("disconnect", "quit");
	}
	
	public void parse(String command, Conversation conversation, Handler backgroundHandler) {
		if (command.startsWith("/")) {
			command = command.replaceFirst("/", "");
			String[] params = command.split(" ");
			if (isClientCommand(params[0])) {
				handleClientCommand(params, conversation, backgroundHandler);
			} else {
				handleServerCommand(params, conversation, backgroundHandler);
			}
			
		} else {
			final String threadedCommand = command;
			backgroundHandler.post(() -> server.getConnection().sendIRC().message(conversation.getName(), threadedCommand));
			Intent intent = new Intent().setAction(Broadcast.NEW_MESSAGE).putExtra("target", conversation.getName());
			context.sendBroadcast(intent);
			
		}
	}
	
	private CommandHandler getCommandHandler(String command) {
		if (commands.containsKey(command)) {
			return commands.get(command);
		} else if (aliases.containsKey(command)) {
			command = aliases.get(command);
			if (commands.containsKey(command)) {
				return commands.get(command);
			}
		}
		return null;
	}
	
	private void handleClientCommand(String[] params, Conversation conversation, Handler backgroundHandler) {
		CommandHandler handler = getCommandHandler(params[0]);
		handler.execute(params, conversation, backgroundHandler);
	}
	
	private void handleServerCommand(String[] params, Conversation conversation, Handler backgroundHandler) {
		if (params.length > 1) {
			params[0] = params[0].toUpperCase(Locale.ENGLISH);
			backgroundHandler.post(() -> server.getConnection().sendRaw().rawLine(mergeParams(params)));
		} else {
			backgroundHandler.post(() -> server.getConnection().sendRaw().rawLine(params[0].toUpperCase(Locale.ENGLISH)));
		}
	}
	
	private boolean isClientCommand(String command) {
		return (commands.containsKey(command) || aliases.containsKey(command));
	}
	
	private String mergeParams(String[] params) {
		StringBuilder sb = new StringBuilder();
		sb.append(params[0]);
		for (int i = 1; i<params.length; i++) {
			sb.append(" ").append(params[i]);
		}
		return sb.toString();
	}
	
	public static CommandParser getInstance(Context context) {
		if (instance == null) {
			instance = new CommandParser(context);
		}
		return instance;
	}
}
