package uk.org.mattford.scoutlink.command.handler;

import android.widget.Toast;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.command.CommandHandler;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.utils.Validator;

public class NickHandler extends CommandHandler {

	@Override
	public void execute(String[] params, Conversation conversation,
			IRCService service) {
		
		if (params.length > 1) {
			String nickname = params[1];
			if (nickname != null && Validator.isValidNickname(nickname)) {
				service.getConnection().sendIRC().changeNick(nickname);
			} else {
				Toast.makeText(service, service.getString(R.string.nickname_not_valid), Toast.LENGTH_SHORT).show();
			}
		}
		
		
	}

	@Override
	public String getUsage() {
		return "/nick <newnick>";
	}

	@Override
	public String getDescription() {
		return "Changes your nickname.";
	}

}
