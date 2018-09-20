package uk.org.mattford.scoutlink.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.pircbotx.Channel;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.irc.IRCBinder;
import uk.org.mattford.scoutlink.irc.IRCService;

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
        final Channel channel = binder.getService().getConnection().getUserChannelDao().getChannel(channelName);

        final EditText et = findViewById(R.id.settings_topic);
        et.setText(channel.getTopic());

        Button changeTopic = findViewById(R.id.settings_topic_change);
        changeTopic.setOnClickListener(view -> {
            if (!channel.getTopic().equals(et.getText().toString())) {
                new Thread(() -> channel.send().setTopic(et.getText().toString())).start();
            }
        });

        CheckBox cb = findViewById(R.id.settings_no_external);
        cb.setChecked(channel.isNoExternalMessages());
        cb.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && !channel.isNoExternalMessages()) {
                new Thread(() -> channel.send().setNoExternalMessages()).start();
            } else if (!b && channel.isNoExternalMessages()) {
                new Thread(() -> channel.send().removeNoExternalMessages()).start();
            }
        });

        cb = findViewById(R.id.settings_topic_protection);
        cb.setChecked(channel.hasTopicProtection());
        cb.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && !channel.hasTopicProtection()) {
                new Thread(() -> channel.send().setTopicProtection()).start();
            } else if (!b && channel.hasTopicProtection()) {
                new Thread(() -> channel.send().removeTopicProtection()).start();
            }
        });

        cb = findViewById(R.id.settings_private);
        cb.setChecked(channel.isChannelPrivate());
        cb.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && !channel.isChannelPrivate()) {
                new Thread(() -> channel.send().setChannelPrivate()).start();
            } else if (!b && channel.isNoExternalMessages()) {
                new Thread(() -> channel.send().removeChannelPrivate()).start();
            }
        });

        cb = findViewById(R.id.settings_secret);
        cb.setChecked(channel.isSecret());
        cb.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && !channel.isSecret()) {
                new Thread(() ->  channel.send().setSecret()).start();
            } else if (!b && channel.isNoExternalMessages()) {
                new Thread(() -> channel.send().removeSecret()).start();
            }
        });

        cb = findViewById(R.id.settings_moderated);
        cb.setChecked(channel.isModerated());
        cb.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && !channel.isModerated()) {
                new Thread(() -> channel.send().setModerated()).start();
            } else if (!b && channel.isNoExternalMessages()) {
                new Thread(() -> channel.send().removeModerated()).start();
            }
        });

        cb = findViewById(R.id.settings_invite_only);
        cb.setChecked(channel.isInviteOnly());
        cb.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && !channel.isInviteOnly()) {
                new Thread(() -> channel.send().setInviteOnly()).start();
            } else if (!b && channel.isNoExternalMessages()) {
                new Thread(() -> channel.send().removeInviteOnly()).start();
            }
        });

        final EditText limit = findViewById(R.id.channel_limit);
        if (channel.getChannelLimit() != -1) {
            limit.setText(Integer.toString(channel.getChannelLimit()));
        }

        Button setLimitButton = findViewById(R.id.channel_limit_set);
        setLimitButton.setOnClickListener(view -> {
            if (!limit.getText().toString().equals("")) {
                new Thread(() -> channel.send().setMode("+l " + limit.getText().toString())).start();
            }
        });
        Button removeLimitButton = findViewById(R.id.channel_limit_remove);
        removeLimitButton.setOnClickListener(view -> {
            new Thread(() -> channel.send().removeChannelLimit()).start();
            limit.setText("");
        });

        final EditText key = findViewById(R.id.channel_key);
        key.setText(channel.getChannelKey());
        Button setKeyButton = findViewById(R.id.channel_key_set);
        setKeyButton.setOnClickListener(view -> new Thread(() -> channel.send().setMode("+k " + key.getText().toString())).start());
        Button removeKeyButton = findViewById(R.id.channel_key_remove);
        removeKeyButton.setOnClickListener(view -> {
            if (channel.getChannelKey() != null) {
                new Thread(() -> channel.send().setMode("-k " + channel.getChannelKey())).start();
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
