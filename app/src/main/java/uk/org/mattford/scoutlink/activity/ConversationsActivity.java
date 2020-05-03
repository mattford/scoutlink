package uk.org.mattford.scoutlink.activity;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;
import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.ScoutlinkApplication;
import uk.org.mattford.scoutlink.command.CommandParser;
import uk.org.mattford.scoutlink.database.LogDatabase;
import uk.org.mattford.scoutlink.database.entities.LogMessage;
import uk.org.mattford.scoutlink.database.migrations.LogDatabaseMigrations;
import uk.org.mattford.scoutlink.databinding.ActivityConversationsBinding;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.model.Server;
import uk.org.mattford.scoutlink.model.Settings;
import uk.org.mattford.scoutlink.receiver.ConversationReceiver;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import uk.org.mattford.scoutlink.viewmodel.ConversationListViewModel;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ConversationsActivity extends AppCompatActivity {
    private ActivityConversationsBinding binding;
	private ConversationListViewModel viewModel;
	private ConversationReceiver receiver;
    private Settings settings;
    private LogDatabase db;
    private Server server;
    private boolean hasDrawerLayout;
    private Handler backgroundHandler;

	private final int JOIN_CHANNEL_RESULT = 0;

    /**
     * Required to work around NPE when screen is rotated immediately after selecting a channel, causing the reference to IRCService to be lost briefly.
     */
    private ArrayList<String> joinChannelBuffer = new ArrayList<>();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityConversationsBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(ConversationListViewModel.class);
        setContentView(binding.getRoot());

        settings = new Settings(this);

        hasDrawerLayout = binding.conversationsDrawerContainer != null;

        binding.toolbar.setTitle("");
        setSupportActionBar(binding.toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(hasDrawerLayout);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

//        pagerAdapter = new ConversationsPagerAdapter(getSupportFragmentManager(), this);
//        pager = binding.pager;
//        pager.setAdapter(pagerAdapter);

        if (hasDrawerLayout) {
            binding.conversationsDrawerContainer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        }
//        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                this.handlePageChange(position);
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                this.handlePageChange(position);
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {}
//
//            private void handlePageChange(int position) {
//                ConversationsPagerAdapter.ConversationInfo info = pagerAdapter.getItemInfo(position);
//                pagerAdapter.setActiveItem(position);
//                binding.toolbar.setTitle(info.conv.getName());
//                if (hasDrawerLayout) {
//                    binding.conversationsDrawerContainer.setDrawerLockMode(
//                            info.conv.getType() == Conversation.TYPE_CHANNEL ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
//                            GravityCompat.END
//                    );
//                }
//            }
//        });
    }

	public void onResume() {
		super.onResume();

        server = Server.getInstance();
        backgroundHandler = ((ScoutlinkApplication)getApplication()).getBackgroundHandler();

        db = Room.databaseBuilder(getApplicationContext(), LogDatabase.class, "logs")
                .addMigrations(LogDatabaseMigrations.MIGRATION_0_1)
                .build();

		this.receiver = new ConversationReceiver(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Broadcast.INVITE);
		intentFilter.addAction(Broadcast.DISCONNECTED);
		intentFilter.addAction(Broadcast.CONNECTED);
        registerReceiver(this.receiver, intentFilter);

        EditText newMessage = binding.input;
        newMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (event == null) {
                onSendButtonClick(v);
                return true;
            }
            return false;
        });

        if (!settings.getBoolean("rules_viewed", false)) {
            final Context context = this;
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.view_rules_dialog_title))
                    .setMessage(getString(R.string.view_rules_dialog_message))
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, which) -> {
                        Intent intent = new Intent(context, RulesActivity.class);
                        context.startActivity(intent);
                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
            settings.putBoolean("rules_viewed", true);
        }

        if (server.getConnection() != null && server.getConnection().isConnected()) {
            onConnect(false);
            return;
        }
        binding.connectionStatus.setText(R.string.connect_message);
        Intent connectIntent = new Intent(getApplicationContext(), IRCService.class);
        connectIntent.setAction(Broadcast.CONNECT);
        startService(connectIntent);
	}
	
	public void onPause() {
		super.onPause();

		if (db != null) {
            db.close();
        }

		unregisterReceiver(this.receiver);
	}
	
	public void onSendButtonClick(View v) {
		EditText et = binding.input;
		String message = et.getText().toString();
		Conversation conversation = viewModel.getActiveConversation().getValue();
		if (message.isEmpty() || conversation == null) {
			return;
		}
		if (message.startsWith("/")) {
			CommandParser.getInstance(getApplicationContext()).parse(message, conversation, backgroundHandler);
		} else {
            if (conversation.getType() == (Conversation.TYPE_SERVER)) {
                Message msg = new Message(getString(R.string.send_message_in_server_window));
                msg.setColour(Color.RED);
                conversation.addMessage(msg);
            } else {
                String nickname = server.getConnection().getNick();
                Message msg = new Message(nickname, message);
                msg.setAlignment(Message.ALIGN_RIGHT);
                conversation.addMessage(msg);

                backgroundHandler.post(() -> server.getConnection().sendIRC().message(conversation.getName(), message));
            }
		}
		et.setText("");
	}
	
	public void onInvite(final String channel) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(getString(R.string.activity_invite_title));
		adb.setMessage(getString(R.string.invited_to_channel, channel));
		adb.setPositiveButton("Yes", (dialog, which) -> backgroundHandler.post(() -> server.getConnection().sendIRC().joinChannel(channel)));
		adb.setNegativeButton("No", (dialog, which) -> {});
		adb.show();
	}

    public void onConnect(boolean initialConnection) {
	    if (initialConnection) {
            if (settings.getBoolean("channel_list_on_connect", false)) {
                Intent channelListIntent = new Intent(this, ChannelListActivity.class);
                ArrayList<String> channels = server.getChannelList();
                channelListIntent.putStringArrayListExtra("channels", channels);
                startActivityForResult(channelListIntent, JOIN_CHANNEL_RESULT);
            }
            binding.connectionStatus.setText(server.getConnection().getNick());
        }

        /*
         * The activity has resumed and the service has been bound, get all the messages we missed...
         */
//        for (Map.Entry<String, Conversation> conv : server.getConversations().entrySet()) {
//            int i = pagerAdapter.getItemByName(conv.getKey());
//            if (i == -1) {
//                onNewConversation(conv.getKey());
//            }
//            onConversationMessage(conv.getKey());
//        }
        // Join any channels we want to join...
        if (!joinChannelBuffer.isEmpty()) {
            backgroundHandler.post(() -> {
                for (String channel : joinChannelBuffer) {
                    server.getConnection().sendIRC().joinChannel(channel);
                }
                joinChannelBuffer.clear();
            });
        }
        Intent updateNotificationIntent = new Intent();
        updateNotificationIntent.setAction(Broadcast.UPDATE_NOTIFICATION);
        sendBroadcast(updateNotificationIntent);
    }

	public void onDisconnect() {
        server.clearConversations();
		Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        binding.connectionStatus.setText(R.string.not_connected);
	}
	
//	public void onConversationMessage(String name) {
//		Conversation conversation = Server.getInstance().getConversation(name);
//        if (conversation == null) {
//            return;
//        }
//
//		while (conversation.hasBuffer()) {
//			Message msg = conversation.pollBuffer();
//            // Don't log server window messages
//			if (conversation.getType() != Conversation.TYPE_SERVER) {
//                backgroundHandler.post(() -> {
//                    LogMessage logMessage = new LogMessage(
//                        name,
//                        conversation.getType(),
//                        msg.getSender(),
//                        msg.getText()
//                    );
//                    db.logMessageDao().insert(logMessage);
//                });
//            }
////			adapter.addMessage(msg);
//		}
//
//        Intent updateNotificationIntent = new Intent();
//        updateNotificationIntent.setAction(Broadcast.UPDATE_NOTIFICATION);
//        sendBroadcast(updateNotificationIntent);
//	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.conversations, menu);
        if (!hasDrawerLayout) {
            menu.findItem(R.id.action_userlist).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        ConversationListViewModel viewModel = new ViewModelProvider(this).get(ConversationListViewModel.class);
    	Conversation conversation = viewModel.getActiveConversation().getValue();
    	if (conversation == null) {
    	    return super.onOptionsItemSelected(item);
        }
        int id = item.getItemId();
        Intent intent;
        switch(id) {
            case R.id.action_close:
                switch (conversation.getType()) {
                    case Conversation.TYPE_CHANNEL:
                        backgroundHandler.post(() -> conversation.getChannel().send().part());
                        break;
                    case Conversation.TYPE_QUERY:
                        server.removeConversation(conversation.getName());
                        break;
                    default:
                        Toast.makeText(this, getResources().getString(R.string.close_server_window), Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            case R.id.action_disconnect:
                backgroundHandler.post(() -> server.getConnection().sendIRC().quitServer(settings.getString("quit_message", getString(R.string.default_quit_message))));
                break;
            case android.R.id.home:
                if (hasDrawerLayout && binding.conversationsDrawerContainer != null) {
                    binding.conversationsDrawerContainer.openDrawer(GravityCompat.START);
                }
                break;
            case R.id.action_userlist:
                if (conversation.getType() == Conversation.TYPE_CHANNEL) {
                    if (hasDrawerLayout && binding.conversationsDrawerContainer != null) {
                        binding.conversationsDrawerContainer.openDrawer(GravityCompat.END);
                    }
                } else {
                    Toast.makeText(this, getResources().getString(R.string.userlist_not_on_channel), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_join:
                intent = new Intent(this, JoinActivity.class);
                startActivityForResult(intent, JOIN_CHANNEL_RESULT);
                break;
            case R.id.action_channel_list:
                intent = new Intent(this, ChannelListActivity.class);
                startActivityForResult(intent, JOIN_CHANNEL_RESULT);
                break;
            case R.id.action_channel_settings:
                if (conversation.getType() != Conversation.TYPE_CHANNEL) {
                    Toast.makeText(this, getString(R.string.channel_settings_not_channel), Toast.LENGTH_SHORT).show();
                } else if (conversation.getChannel().isOp(server.getConnection().getUserBot())) {
                    intent = new Intent(this, ChannelSettingsActivity.class);
                    intent.putExtra("channelName", conversation.getName());
                    startActivity(intent);
                } else {
                    Toast.makeText(this, getString(R.string.channel_settings_need_op), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_rules:
                intent = new Intent(this, RulesActivity.class);
                startActivity(intent);
                break;
            case R.id.action_logs:
                intent = new Intent(this, LogListActivity.class);
                startActivity(intent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void onSettingsButtonClick(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == JOIN_CHANNEL_RESULT && resultCode == RESULT_OK) {
            String channel = data.getStringExtra("target");
            joinChannelBuffer.add(channel);
        }
    }
}
