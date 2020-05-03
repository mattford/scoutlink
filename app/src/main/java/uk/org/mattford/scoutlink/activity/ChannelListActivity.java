package uk.org.mattford.scoutlink.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.databinding.ActivityChannelListBinding;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.receiver.ChannelListReceiver;

public class ChannelListActivity extends ListActivity implements AdapterView.OnItemClickListener {

    private ChannelListReceiver receiver;
    private ArrayAdapter<String> adapter;
    private ActivityChannelListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChannelListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        adapter = new ArrayAdapter<>(this, R.layout.channel_list_item, new ArrayList<>());
        setListAdapter(adapter);

        getListView().setOnItemClickListener(this);
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

    public void onChannelListInfo(String channel) {
        if (adapter.getPosition(channel) == -1) {
            adapter.add(channel);
            adapter.sort((lhs, rhs) -> {
                int res = String.CASE_INSENSITIVE_ORDER.compare(lhs, rhs);
                if (res == 0) {
                    res = lhs.compareTo(rhs);
                }
                return res;
            });
            adapter.notifyDataSetChanged();
        }

        binding.loadingPanel.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView tv = (TextView)view;
        String channel = tv.getText().toString();
        Intent intent = new Intent();
        intent.putExtra("target", channel);
        setResult(RESULT_OK, intent);
        finish();
    }
}
