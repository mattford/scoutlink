package uk.org.mattford.scoutlink.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import org.pircbotx.Channel;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.irc.IRCBinder;
import uk.org.mattford.scoutlink.irc.IRCService;

public class ChannelSettingsActivity extends ActionBarActivity implements ServiceConnection {

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

        final EditText et = (EditText)findViewById(R.id.settings_topic);
        et.setText(channel.getTopic());

        Button changeTopic = (Button)findViewById(R.id.settings_topic_change);
        changeTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!channel.getTopic().equals(et.getText().toString())) {
                    channel.send().setTopic(et.getText().toString());
                }
            }
        });

        CheckBox cb = (CheckBox)findViewById(R.id.settings_no_external);
        cb.setChecked(channel.isNoExternalMessages());
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b && !channel.isNoExternalMessages()) {
                    channel.send().setNoExternalMessages();
                } else if (!b && channel.isNoExternalMessages()) {
                    channel.send().removeNoExternalMessages();
                }
                //compoundButton.setChecked(channel.isNoExternalMessages());
            }
        });

        cb = (CheckBox)findViewById(R.id.settings_topic_protection);
        cb.setChecked(channel.hasTopicProtection());
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b && !channel.hasTopicProtection()) {
                    channel.send().setTopicProtection();
                } else if (!b && channel.hasTopicProtection()) {
                    channel.send().removeTopicProtection();
                }
                //compoundButton.setChecked(channel.hasTopicProtection());
            }
        });

        cb = (CheckBox)findViewById(R.id.settings_private);
        cb.setChecked(channel.isChannelPrivate());
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b && !channel.isChannelPrivate()) {
                    channel.send().setChannelPrivate();
                } else if (!b && channel.isNoExternalMessages()) {
                    channel.send().removeChannelPrivate();
                }
               // compoundButton.setChecked(channel.isChannelPrivate());
            }
        });

        cb = (CheckBox)findViewById(R.id.settings_secret);
        cb.setChecked(channel.isSecret());
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b && !channel.isSecret()) {
                    channel.send().setSecret();
                } else if (!b && channel.isNoExternalMessages()) {
                    channel.send().removeSecret();
                }
               // compoundButton.setChecked(channel.isSecret());
            }
        });

        cb = (CheckBox)findViewById(R.id.settings_moderated);
        cb.setChecked(channel.isModerated());
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b && !channel.isModerated()) {
                    channel.send().setModerated();
                } else if (!b && channel.isNoExternalMessages()) {
                    channel.send().removeModerated();
                }
               // compoundButton.setChecked(channel.isModerated());
            }
        });

        cb = (CheckBox)findViewById(R.id.settings_invite_only);
        cb.setChecked(channel.isInviteOnly());
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b && !channel.isInviteOnly()) {
                    channel.send().setInviteOnly();
                } else if (!b && channel.isNoExternalMessages()) {
                    channel.send().removeInviteOnly();
                }
               // compoundButton.setChecked(channel.isInviteOnly());
            }
        });

        final EditText limit = (EditText)findViewById(R.id.channel_limit);
        if (channel.getChannelLimit() != -1) {
            limit.setText(Integer.toString(channel.getChannelLimit()));
        }

        Button setLimitButton = (Button)findViewById(R.id.channel_limit_set);
        setLimitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!limit.getText().toString().equals("")) {
                    channel.send().setMode("+l " + limit.getText().toString());
                }
            }
        });
        Button removeLimitButton = (Button)findViewById(R.id.channel_limit_remove);
        removeLimitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                channel.send().removeChannelLimit();
                limit.setText("");
            }
        });

        final EditText key = (EditText)findViewById(R.id.channel_key);
        key.setText(channel.getChannelKey());
        Button setKeyButton = (Button)findViewById(R.id.channel_key_set);
        setKeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                channel.send().setMode("+k " + key.getText().toString());
            }
        });
        Button removeKeyButton = (Button)findViewById(R.id.channel_key_remove);
        removeKeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (channel.getChannelKey() != null) {
                    channel.send().setMode("-k " + channel.getChannelKey());
                    key.setText("");
                }
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
