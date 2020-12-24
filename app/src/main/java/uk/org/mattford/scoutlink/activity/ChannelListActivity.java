package uk.org.mattford.scoutlink.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import org.pircbotx.ChannelListEntry;

import java.util.List;

import uk.org.mattford.scoutlink.adapter.ChannelListAdapter;
import uk.org.mattford.scoutlink.databinding.ActivityChannelListBinding;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Server;
import uk.org.mattford.scoutlink.receiver.ChannelListReceiver;

public class ChannelListActivity extends AppCompatActivity implements ChannelListAdapter.OnChannelListItemClickListener {

    private ChannelListReceiver receiver;
    private ChannelListAdapter adapter;
    private ActivityChannelListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChannelListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        adapter = new ChannelListAdapter(this);
        binding.channelList.setAdapter(adapter);
    }

    public void onResume() {
        super.onResume();
        receiver = new ChannelListReceiver(this);
        registerReceiver(receiver, new IntentFilter(Broadcast.CHANNEL_LIST_INFO));
        Intent listChannels = new Intent(this, IRCService.class);
        listChannels.setAction(IRCService.ACTION_LIST_CHANNELS);
        startService(listChannels);
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void onChannelListInfo() {
        Server server = Server.getInstance();
        List<ChannelListEntry> channelList = server.getChannelList();
        adapter.setChannels(channelList);
        binding.loadingPanel.setVisibility(View.GONE);
    }

    @Override
    public void onChannelListItemClick(String channel) {
        Intent intent = new Intent()
                .putExtra("target", channel);
        setResult(RESULT_OK, intent);
        finish();
    }
}
