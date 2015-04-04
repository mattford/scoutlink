package uk.org.mattford.scoutlink.command;

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
import uk.org.mattford.scoutlink.model.Conversation;

public class CommandParser {
	
	public static CommandParser instance;
	
	public HashMap<String, CommandHandler> commands;
	public HashMap<String, String> aliases;
	
	private CommandParser() {
		commands = new HashMap<>();
		aliases = new HashMap<>();
		
		commands.put("join", new JoinHandler());
		commands.put("part", new PartHandler());
		commands.put("nick", new NickHandler());
		commands.put("quit", new QuitHandler());
		commands.put("me", new ActionHandler());
		commands.put("notify", new NotifyHandler());
		commands.put("msg", new MessageHandler());
		
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
			
			service.getConnection().sendIRC().message(conversation.getName(), command);
			service.onNewMessage(conversation.getName());
			
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
			params[0] = params[0].toUpperCase(Locale.ENGLISH);
			service.getConnection().sendRaw().rawLine(mergeParams(params));
		} else {
			service.getConnection().sendRaw().rawLine(params[0].toUpperCase(Locale.ENGLISH));
		}
	}
	
	public boolean isClientCommand(String command) {
		return (commands.containsKey(command) || aliases.containsKey(command));
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
