package uk.org.mattford.scoutlink.command;

import java.util.HashMap;

import android.util.Log;
import uk.org.mattford.scoutlink.command.handler.JoinHandler;
import uk.org.mattford.scoutlink.command.handler.NickHandler;
import uk.org.mattford.scoutlink.command.handler.PartHandler;
import uk.org.mattford.scoutlink.command.handler.QuitHandler;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Conversation;

public class CommandParser {
	
	public static CommandParser instance;
	
	public HashMap<String, CommandHandler> commands;
	public HashMap<String, String> aliases;
	
	private final String logTag = "ScoutLink/CommandParser";

	private CommandParser() {
		commands = new HashMap<String, CommandHandler>();
		aliases = new HashMap<String, String>();
		
		commands.put("join", new JoinHandler());
		commands.put("part", new PartHandler());
		commands.put("nick", new NickHandler());
		commands.put("quit", new QuitHandler());
		
		aliases.put("j", "join");
		aliases.put("p", "part");
		aliases.put("n", "nick");
		aliases.put("q", "quit");
		aliases.put("disconnect", "quit");
	}
	
	public void parse(String command, Conversation conversation, IRCService service) {
		if (command.startsWith("/")) {
			command = command.replaceFirst("/", "");
			String[] params = command.split(" ");
			if (isClientCommand(params[0])) {
				handleClientCommand(params, conversation, service);
			} else {
				handleServerCommand(params, conversation, service);
			}
			
		} else {
			service.getConnection().sendMessage(conversation.getName(), command);
			service.getConnection().sendNewMessageBroadcast(conversation.getName());
			
		}
	}
	
	public CommandHandler getCommandHandler(String command) {
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
	
	public void handleClientCommand(String[] params, Conversation conversation, IRCService service) {
		CommandHandler handler = getCommandHandler(params[0]);
		handler.execute(params, conversation, service);
	}
	
	public void handleServerCommand(String[] params, Conversation conversation, IRCService service) {
		if (params.length > 1) {
			params[0] = params[0].toUpperCase();
			Log.d(logTag, "Server Command: " + mergeParams(params));
			service.getConnection().sendRawLineViaQueue(mergeParams(params));
		} else {
			service.getConnection().sendRawLineViaQueue(params[0].toUpperCase());
		}
	}
	
	public boolean isClientCommand(String command) {
		if (commands.containsKey(command) || aliases.containsKey(command)) {
			return true;
		}
		return false;
	}
	
	public String mergeParams(String[] params) {
		StringBuilder sb = new StringBuilder();
		sb.append(params[0]);
		for (int i = 1; i<params.length; i++) {
			sb.append(" ").append(params[i]);
		}
		return sb.toString();
	}
	
	public static CommandParser getInstance() {
		if (instance == null) {
			instance = new CommandParser();
		}
		return instance;
	}
	
	
}
