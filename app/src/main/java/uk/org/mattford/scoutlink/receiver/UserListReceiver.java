package uk.org.mattford.scoutlink.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import uk.org.mattford.scoutlink.activity.UserListActivity;
import uk.org.mattford.scoutlink.model.Broadcast;

public class UserListReceiver extends BroadcastReceiver {

    private UserListActivity activity;
    private String channel;

    public UserListReceiver(UserListActivity activity, String channel) {
        super();
        this.activity = activity;
        this.channel = channel;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Broadcast.USER_LIST_CHANGED:
                String targetChannel = intent.getStringExtra("target");
                if (targetChannel == null || channel.equals(targetChannel)) {
                    activity.refreshUserList();
                }
        }
    }
}
