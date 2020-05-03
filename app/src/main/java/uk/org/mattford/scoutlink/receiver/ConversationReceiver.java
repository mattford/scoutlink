package uk.org.mattford.scoutlink.receiver;

import androidx.lifecycle.ViewModelProvider;
import uk.org.mattford.scoutlink.activity.ConversationsActivity;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Server;
import uk.org.mattford.scoutlink.viewmodel.ConversationListViewModel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConversationReceiver extends BroadcastReceiver {
	
	private ConversationsActivity activity;
	private ConversationListViewModel viewModel;
	
	public ConversationReceiver(ConversationsActivity act) {
		super();
		this.activity = act;
		viewModel = new ViewModelProvider(act).get(ConversationListViewModel.class);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action == null) {
		    return;
        }
        switch (action) {
            case Broadcast.INVITE:
                activity.onInvite(intent.getStringExtra("target"));
                break;
            case Broadcast.DISCONNECTED:
                activity.onDisconnect();
                break;
            case Broadcast.CONNECTED:
                activity.onConnect(true);
                break;
        }
	}
}
