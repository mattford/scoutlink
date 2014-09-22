package uk.org.mattford.scoutlink.receiver;

import uk.org.mattford.scoutlink.activity.ConversationsActivity;
import uk.org.mattford.scoutlink.model.Broadcast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConversationReceiver extends BroadcastReceiver {
	
	private final ConversationsActivity activity;
	
	public ConversationReceiver(ConversationsActivity act) {
		super();
		this.activity = act;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("ScoutLink", "Received broadcast action: " + intent.getAction());
		String action = intent.getAction();
		if (action.equals(Broadcast.NEW_CONVERSATION)) {
			this.activity.createNewConversation(intent.getStringExtra("target"));
		} else if (action.equals(Broadcast.NEW_MESSAGE)) {
			this.activity.newConversationMessage(intent.getStringExtra("target"));
		} else if (action.equals(Broadcast.REMOVE_CONVERSATION)){
			this.activity.removeConversation(intent.getStringExtra("target"));
		}
		
	}

}
