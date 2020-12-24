package uk.org.mattford.scoutlink.command;

import android.os.Handler;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Locale;

import uk.org.mattford.scoutlink.activity.ConversationsActivity;
import uk.org.mattford.scoutlink.command.handler.ActionHandler;
import uk.org.mattford.scoutlink.command.handler.JoinHandler;
import uk.org.mattford.scoutlink.command.handler.MessageHandler;
import uk.org.mattford.scoutlink.command.handler.NickHandler;
import uk.org.mattford.scoutlink.command.handler.NotifyHandler;
import uk.org.mattford.scoutlink.command.handler.PartHandler;
import uk.org.mattford.scoutlink.command.handler.QuitHandler;
import uk.org.mattford.scoutlink.command.handler.SayHandler;
import uk.org.mattford.scoutlink.command.handler.UserDefinedCommandHandler;
import uk.org.mattford.scoutlink.database.SettingsDatabase;
import uk.org.mattford.scoutlink.database.entities.Alias;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.model.Server;

public class CommandParser {
	
	private static CommandParser instance;
	private final Server server;

	private final HashMap<String, CommandHandler> commands = new HashMap<>();
	private final HashMap<String, String> aliases = new HashMap<>();
	private final HashMap<String, CommandHandler> userDefinedAliases = new HashMap<>();
	
	private CommandParser(ConversationsActivity activity) {
		server = Server.getInstance();

		commands.put("join", new JoinHandler(activity.getApplicationContext()));
		commands.put("part", new PartHandler(activity.getApplicationContext()));
		commands.put("nick", new NickHandler(activity.getApplicationContext()));
		commands.put("quit", new QuitHandler(activity.getApplicationContext()));
		commands.put("me", new ActionHandler(activity.getApplicationContext()));
		commands.put("notify", new NotifyHandler(activity.getApplicationContext()));
		commands.put("msg", new MessageHandler(activity.getApplicationContext()));
		commands.put("say", new SayHandler(activity.getApplicationContext()));

		aliases.put("j", "join");
		aliases.put("p", "part");
		aliases.put("n", "nick");
		aliases.put("q", "quit");
		aliases.put("disconnect", "quit");

		SettingsDatabase.getInstance(activity.getApplicationContext()).aliasesDao().getAliases().observe(activity, dbAliases -> {
			userDefinedAliases.clear();
			for (Alias alias : dbAliases) {
				userDefinedAliases.put(alias.commandName, new UserDefinedCommandHandler(activity.getApplicationContext(), this, alias.commandText));
			}
		});
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
		} else if (userDefinedAliases.containsKey(command)) {
			return userDefinedAliases.get(command);
		}
		return null;
	}
	
	private void handleClientCommand(String[] params, Conversation conversation, Handler backgroundHandler) {
		CommandHandler handler = getCommandHandler(params[0]);
		if (handler == null) {
			conversation.addMessage(new Message("Command not found", Message.SENDER_TYPE_OTHER, Message.TYPE_ERROR));
		} else if (!handler.validate(params, conversation)) {
			conversation.addMessage(new Message(handler.getUsage(), Message.SENDER_TYPE_OTHER, Message.TYPE_ERROR));
		} else {
			handler.execute(params, conversation, backgroundHandler);
		}
	}
	
	private void handleServerCommand(String[] params, Conversation conversation, Handler backgroundHandler) {
		if (params.length > 1) {
			params[0] = params[0].toUpperCase(Locale.ENGLISH);
			backgroundHandler.post(() -> server.getConnection().sendRaw().rawLine(StringUtils.join(params, " ")));
		} else {
			backgroundHandler.post(() -> server.getConnection().sendRaw().rawLine(params[0].toUpperCase(Locale.ENGLISH)));
		}
	}
	
	private boolean isClientCommand(String command) {
		return commands.containsKey(command) || aliases.containsKey(command) || userDefinedAliases.containsKey(command);
	}
	
	public static CommandParser getInstance(ConversationsActivity activity) {
		if (instance == null) {
			instance = new CommandParser(activity);
		}
		return instance;
	}
}
