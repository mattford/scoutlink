package uk.org.mattford.scoutlink.receiver;

import uk.org.mattford.scoutlink.activity.ConversationsActivity;
import uk.org.mattford.scoutlink.model.Broadcast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ConversationReceiver extends BroadcastReceiver {
	
	private ConversationsActivity activity;
	
	public ConversationReceiver(ConversationsActivity act) {
		super();
		this.activity = act;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
        switch (action) {
            case Broadcast.NEW_CONVERSATION:
                activity.onNewConversation(intent.getStringExtra("target"), intent.getBooleanExtra("selected", false));
                break;
            case Broadcast.NEW_MESSAGE:
                activity.onConversationMessage(intent.getStringExtra("target"));
                break;
            case Broadcast.REMOVE_CONVERSATION:
                activity.removeConversation(intent.getStringExtra("target"));
                break;
            case Broadcast.INVITE:
                activity.onInvite(intent.getStringExtra("target"));
                break;
            case Broadcast.DISCONNECTED:
                activity.onDisconnect();
                break;
            case Broadcast.CONNECTED:
                activity.onConnect();
                break;
        }

	}

}
