package uk.org.mattford.scoutlink.command.handler;

import android.content.Context;
import android.os.Handler;

import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.command.CommandParser;
import uk.org.mattford.scoutlink.model.Conversation;

public class UserDefinedCommandHandler extends CommandHandler {
    private final String[] commands;
    private final CommandParser parser;
    private final String argPattern = "\\$([0-9]+)(-?)";

    public UserDefinedCommandHandler(Context context, CommandParser parser, String command) {
        super(context);
        this.commands = command.split("\\r?\\n");
        this.parser = parser;
    }

    public boolean validate(String[] params, Conversation conversation) {
        for (String command : commands) {
            Pattern p = Pattern.compile(argPattern);
            Matcher m = p.matcher(command);
            while (m.find()) {
                // Groups: 1 - param number, 2 - "greedy" dash
                try {
                    int paramNumber = Integer.parseInt(m.group(1));
                    if (paramNumber < 0 || paramNumber > params.length - 1) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    // Failed.
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void execute(String[] params, Conversation conversation, Handler backgroundHandler) {
        /*
         * Supported args:
         * $1 - replaced with relevant matching group
         * $1- - replaced with relevant matching group and all later matching groups
         * # - replaced with current channel name (only if without text immediately before/after)
         */
        for (String command : commands) {
            if (conversation.getType() == Conversation.TYPE_CHANNEL) {
                command = command.replaceAll("(^| )#($| )", "$1" + conversation.getName() + "$2");
            }
            Pattern p = Pattern.compile(argPattern);
            Matcher m = p.matcher(command);
            StringBuffer finalCommand = new StringBuffer(command.length());
            while (m.find()) {
                // Groups: 1 - param number, 2 - "greedy" dash
                try {
                    int paramNumber = Integer.parseInt(m.group(1));
                    String paramValue = "";
                    if (paramNumber >= 0 && params.length > paramNumber) {
                        StringJoiner sj = new StringJoiner(" ");
                        if ("-".equalsIgnoreCase(m.group(2))) {
                            for (int i = paramNumber; i < params.length; i++) {
                                sj.add(params[i]);
                            }
                            paramValue = sj.toString();
                        } else {
                            paramValue = params[paramNumber];
                        }
                    }
                    m.appendReplacement(finalCommand, paramValue);
                } catch (NumberFormatException e) {
                    // Failed.
                    return;
                }
            }
            m.appendTail(finalCommand);
            parser.parse(finalCommand.toString(), conversation, backgroundHandler);
        }
    }


    @Override
    public String getUsage() {
        return "Incorrect usage, refer to Settings -> Aliases for correct usage.";
    }

    @Override
    public String getDescription() {
        return null;
    }
}
