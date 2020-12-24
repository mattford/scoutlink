package uk.org.mattford.scoutlink.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import uk.org.mattford.scoutlink.activity.ChannelListActivity;
import uk.org.mattford.scoutlink.model.Broadcast;

public class ChannelListReceiver extends BroadcastReceiver {

    private final ChannelListActivity activity;

    public ChannelListReceiver(ChannelListActivity activity) {
        super();
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Broadcast.CHANNEL_LIST_INFO.equals(action)) {
            activity.onChannelListInfo();
        }
    }
}
