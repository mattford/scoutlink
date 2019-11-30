package uk.org.mattford.scoutlink.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.pircbotx.Channel;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.irc.IRCBinder;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Server;

public class ChannelSettingsActivity extends AppCompatActivity implements ServiceConnection {

    private IRCBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_settings);
    }

    public void onResume() {
        super.onResume();
        Intent intent = new Intent(this, IRCService.class);
        startService(intent);
        bindService(intent, this, 0);
    }

    public void onPause() {
        super.onPause();
        unbindService(this);
    }

    protected void populateValues() {
        String channelName = getIntent().getStringExtra("channelName");
        final Channel channel = Server.getInstance().getConnection().getUserChannelDao().getChannel(channelName);

        Handler backgroundHandler = binder.getService().getBackgroundHandler();
        final EditText et = findViewById(R.id.settings_topic);
        et.setText(channel.getTopic());

        Button changeTopic = findViewById(R.id.settings_topic_change);
        changeTopic.setOnClickListener(view -> {
            if (!channel.getTopic().equals(et.getText().toString())) {
                backgroundHandler.post(() -> channel.send().setTopic(et.getText().toString()));
            }
        });

        CheckBox cb = findViewById(R.id.settings_no_external);
        cb.setChecked(channel.isNoExternalMessages());
        cb.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && !channel.isNoExternalMessages()) {
                backgroundHandler.post(() -> channel.send().setNoExternalMessages());
            } else if (!b && channel.isNoExternalMessages()) {
                backgroundHandler.post(() -> channel.send().removeNoExternalMessages());
            }
        });

        cb = findViewById(R.id.settings_topic_protection);
        cb.setChecked(channel.hasTopicProtection());
        cb.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && !channel.hasTopicProtection()) {
                backgroundHandler.post(() -> channel.send().setTopicProtection());
            } else if (!b && channel.hasTopicProtection()) {
                backgroundHandler.post(() -> channel.send().removeTopicProtection());
            }
        });

        cb = findViewById(R.id.settings_private);
        cb.setChecked(channel.isChannelPrivate());
        cb.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && !channel.isChannelPrivate()) {
                backgroundHandler.post(() -> channel.send().setChannelPrivate());
            } else if (!b && channel.isNoExternalMessages()) {
                backgroundHandler.post(() -> channel.send().removeChannelPrivate());
            }
        });

        cb = findViewById(R.id.settings_secret);
        cb.setChecked(channel.isSecret());
        cb.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && !channel.isSecret()) {
                backgroundHandler.post(() ->  channel.send().setSecret());
            } else if (!b && channel.isNoExternalMessages()) {
                backgroundHandler.post(() -> channel.send().removeSecret());
            }
        });

        cb = findViewById(R.id.settings_moderated);
        cb.setChecked(channel.isModerated());
        cb.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && !channel.isModerated()) {
                backgroundHandler.post(() -> channel.send().setModerated());
            } else if (!b && channel.isNoExternalMessages()) {
                backgroundHandler.post(() -> channel.send().removeModerated());
            }
        });

        cb = findViewById(R.id.settings_invite_only);
        cb.setChecked(channel.isInviteOnly());
        cb.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && !channel.isInviteOnly()) {
                backgroundHandler.post(() -> channel.send().setInviteOnly());
            } else if (!b && channel.isNoExternalMessages()) {
                backgroundHandler.post(() -> channel.send().removeInviteOnly());
            }
        });

        final EditText limit = findViewById(R.id.channel_limit);
        if (channel.getChannelLimit() != -1) {
            limit.setText(Integer.toString(channel.getChannelLimit()));
        }

        Button setLimitButton = findViewById(R.id.channel_limit_set);
        setLimitButton.setOnClickListener(view -> {
            if (!limit.getText().toString().equals("")) {
                backgroundHandler.post(() -> channel.send().setMode("+l " + limit.getText().toString()));
            }
        });
        Button removeLimitButton = findViewById(R.id.channel_limit_remove);
        removeLimitButton.setOnClickListener(view -> {
            backgroundHandler.post(() -> channel.send().removeChannelLimit());
            limit.setText("");
        });

        final EditText key = findViewById(R.id.channel_key);
        key.setText(channel.getChannelKey());
        Button setKeyButton = findViewById(R.id.channel_key_set);
        setKeyButton.setOnClickListener(view -> backgroundHandler.post(() -> channel.send().setMode("+k " + key.getText().toString())));
        Button removeKeyButton = findViewById(R.id.channel_key_remove);
        removeKeyButton.setOnClickListener(view -> {
            if (channel.getChannelKey() != null) {
                backgroundHandler.post(() -> channel.send().setMode("-k " + channel.getChannelKey()));
                key.setText("");
            }
        });
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        this.binder = (IRCBinder)iBinder;
        populateValues();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        this.binder = null;
    }
}
