package uk.org.mattford.scoutlink.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import uk.org.mattford.scoutlink.activity.ChannelListActivity;
import uk.org.mattford.scoutlink.model.Broadcast;

public class ChannelListReceiver extends BroadcastReceiver {

    private ChannelListActivity activity;

    public ChannelListReceiver(ChannelListActivity activity) {
        super();
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch(intent.getAction()) {
            case Broadcast.CHANNEL_LIST_INFO:
                String channel = intent.getStringExtra("value");
                activity.onChannelListInfo(channel);
                break;
        }
    }
}
