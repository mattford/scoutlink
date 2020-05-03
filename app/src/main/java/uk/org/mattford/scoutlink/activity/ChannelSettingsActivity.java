package uk.org.mattford.scoutlink.activity;

import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;

import org.pircbotx.Channel;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.ScoutlinkApplication;
import uk.org.mattford.scoutlink.databinding.ActivityChannelSettingsBinding;
import uk.org.mattford.scoutlink.model.Server;

public class ChannelSettingsActivity extends AppCompatActivity {
    private ActivityChannelSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChannelSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        populateValues();
    }

    protected void populateValues() {
        String channelName = getIntent().getStringExtra("channelName");
        final Channel channel = Server.getInstance().getConnection().getUserChannelDao().getChannel(channelName);

        Handler backgroundHandler = ((ScoutlinkApplication)getApplication()).getBackgroundHandler();
        binding.settingsTopic.setText(channel.getTopic());

        Button changeTopic = findViewById(R.id.settings_topic_change);
        changeTopic.setOnClickListener(view -> {
            String topic = binding.settingsTopic.getText().toString();
            if (!channel.getTopic().equals(topic)) {
                backgroundHandler.post(() -> channel.send().setTopic(topic));
            }
        });

        binding.settingsNoExternal.setChecked(channel.isNoExternalMessages());
        binding.settingsNoExternal.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && !channel.isNoExternalMessages()) {
                backgroundHandler.post(() -> channel.send().setNoExternalMessages());
            } else if (!b && channel.isNoExternalMessages()) {
                backgroundHandler.post(() -> channel.send().removeNoExternalMessages());
            }
        });

        binding.settingsTopicProtection.setChecked(channel.hasTopicProtection());
        binding.settingsTopicProtection.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && !channel.hasTopicProtection()) {
                backgroundHandler.post(() -> channel.send().setTopicProtection());
            } else if (!b && channel.hasTopicProtection()) {
                backgroundHandler.post(() -> channel.send().removeTopicProtection());
            }
        });

        binding.settingsPrivate.setChecked(channel.isChannelPrivate());
        binding.settingsPrivate.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && !channel.isChannelPrivate()) {
                backgroundHandler.post(() -> channel.send().setChannelPrivate());
            } else if (!b && channel.isNoExternalMessages()) {
                backgroundHandler.post(() -> channel.send().removeChannelPrivate());
            }
        });

        binding.settingsSecret.setChecked(channel.isSecret());
        binding.settingsSecret.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && !channel.isSecret()) {
                backgroundHandler.post(() ->  channel.send().setSecret());
            } else if (!b && channel.isNoExternalMessages()) {
                backgroundHandler.post(() -> channel.send().removeSecret());
            }
        });

        binding.settingsModerated.setChecked(channel.isModerated());
        binding.settingsModerated.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && !channel.isModerated()) {
                backgroundHandler.post(() -> channel.send().setModerated());
            } else if (!b && channel.isNoExternalMessages()) {
                backgroundHandler.post(() -> channel.send().removeModerated());
            }
        });

        binding.settingsInviteOnly.setChecked(channel.isInviteOnly());
        binding.settingsInviteOnly.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && !channel.isInviteOnly()) {
                backgroundHandler.post(() -> channel.send().setInviteOnly());
            } else if (!b && channel.isNoExternalMessages()) {
                backgroundHandler.post(() -> channel.send().removeInviteOnly());
            }
        });

        if (channel.getChannelLimit() != -1) {
            binding.channelLimit.setText(Integer.toString(channel.getChannelLimit()));
        }

        binding.channelLimitSet.setOnClickListener(view -> {
            if (!binding.channelLimit.getText().toString().equals("")) {
                backgroundHandler.post(() -> channel.send().setMode("+l " + binding.channelLimit.getText().toString()));
            }
        });
        binding.channelLimitRemove.setOnClickListener(view -> {
            backgroundHandler.post(() -> channel.send().removeChannelLimit());
            binding.channelLimit.setText("");
        });

        binding.channelKey.setText(channel.getChannelKey());
        binding.channelKeySet.setOnClickListener(view -> backgroundHandler.post(() -> channel.send().setMode("+k " + binding.channelKey.getText().toString())));
        binding.channelKeyRemove.setOnClickListener(view -> {
            if (channel.getChannelKey() != null) {
                backgroundHandler.post(() -> channel.send().setMode("-k " + channel.getChannelKey()));
                binding.channelKey.setText("");
            }
        });
    }
}
