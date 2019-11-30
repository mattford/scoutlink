package uk.org.mattford.scoutlink.activity;

import java.util.ArrayList;
import java.util.Map;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;
import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.adapter.ConversationsPagerAdapter;
import uk.org.mattford.scoutlink.adapter.MessageListAdapter;
import uk.org.mattford.scoutlink.command.CommandParser;
import uk.org.mattford.scoutlink.database.LogDatabase;
import uk.org.mattford.scoutlink.database.entities.LogMessage;
import uk.org.mattford.scoutlink.database.migrations.LogDatabaseMigrations;
import uk.org.mattford.scoutlink.fragment.ConversationListFragment;
import uk.org.mattford.scoutlink.fragment.UserListFragment;
import uk.org.mattford.scoutlink.irc.IRCBinder;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.model.Server;
import uk.org.mattford.scoutlink.model.Settings;
import uk.org.mattford.scoutlink.receiver.ConversationReceiver;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import uk.org.mattford.scoutlink.views.NonSwipeableViewPager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ConversationsActivity extends AppCompatActivity implements ServiceConnection, ConversationListFragment.OnConversationListFragmentInteractionListener, UserListFragment.OnUserListFragmentInteractionListener {
	
	private ConversationsPagerAdapter pagerAdapter;
	private NonSwipeableViewPager pager;
	private ConversationReceiver receiver;
	private IRCBinder binder;
    private Settings settings;
    private LogDatabase db;
    private DrawerLayout drawerLayout;
    private Server server;

	private final int JOIN_CHANNEL_RESULT = 0;

    /**
     * Required to work around NPE when screen is rotated immediately after selecting a channel, causing the reference to IRCService to be lost briefly.
     */
    private ArrayList<String> joinChannelBuffer = new ArrayList<>();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        settings = new Settings(this);

        drawerLayout = findViewById(R.id.conversations_container);

        pagerAdapter = new ConversationsPagerAdapter(getSupportFragmentManager(), this);
        pager = findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                ConversationsPagerAdapter.ConversationInfo info = pagerAdapter.getItemInfo(position);
                pagerAdapter.setActiveItem(position);
                drawerLayout.setDrawerLockMode(
                    info != null && info.conv.getType() == Conversation.TYPE_CHANNEL ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                    GravityCompat.END
                );
            }

            @Override
            public void onPageSelected(int position) {
                ConversationsPagerAdapter.ConversationInfo info = pagerAdapter.getItemInfo(position);
                pagerAdapter.setActiveItem(position);
                drawerLayout.setDrawerLockMode(
                    info != null && info.conv.getType() == Conversation.TYPE_CHANNEL ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                    GravityCompat.END
                );            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        if (savedInstanceState == null) {
            Fragment newFragment = new ConversationListFragment(pager);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.conversation_list_fragment, newFragment).commit();

            Fragment userListFragment = new UserListFragment(pager);
            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
            ft2.add(R.id.user_list_fragment, userListFragment).commit();
        }
    }

	/**
	 * If this is not overridden, then ConversationsPagerAdapter
     * retains old fragments when the activity is recreated.
     *
     * TODO: Find a better way.
	 */
	@Override
	protected void onSaveInstanceState(final Bundle outState) {
	    // super.onSaveInstanceState(outState);
	}

	public void onResume() {
		super.onResume();

        server = Server.getInstance();

        db = Room.databaseBuilder(getApplicationContext(), LogDatabase.class, "logs")
                .addMigrations(LogDatabaseMigrations.MIGRATION_0_1)
                .build();

		this.receiver = new ConversationReceiver(this);
		registerReceiver(this.receiver, new IntentFilter(Broadcast.NEW_CONVERSATION));
		registerReceiver(this.receiver, new IntentFilter(Broadcast.NEW_MESSAGE));
		registerReceiver(this.receiver, new IntentFilter(Broadcast.REMOVE_CONVERSATION));
		registerReceiver(this.receiver, new IntentFilter(Broadcast.INVITE));
		registerReceiver(this.receiver, new IntentFilter(Broadcast.DISCONNECTED));
        registerReceiver(this.receiver, new IntentFilter(Broadcast.CONNECTED));

		Intent serviceIntent = new Intent(this, IRCService.class);
		startService(serviceIntent);
		bindService(serviceIntent, this, 0);

        EditText newMessage = findViewById(R.id.input);
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
	}
	
	public void onPause() {
		super.onPause();

		if (db != null) {
            db.close();
        }

		unregisterReceiver(this.receiver);
		unbindService(this);
	}
	
	public void onSendButtonClick(View v) {
		EditText et = findViewById(R.id.input);
		String message = et.getText().toString();
		Conversation conv = pagerAdapter.getItemInfo(pager.getCurrentItem()).conv;
		if (message.isEmpty()) {
			return;
		}
		if (message.startsWith("/")) {
			CommandParser.getInstance().parse(message, conv, this.binder.getService());
		} else {
            if (conv.getType() == (Conversation.TYPE_SERVER)) {
                Message msg = new Message(getString(R.string.send_message_in_server_window));
                msg.setColour(Color.RED);
                conv.addMessage(msg);
            } else {
                String nickname = server.getConnection().getNick();
                Message msg = new Message(nickname, message);
                msg.setAlignment(Message.ALIGN_RIGHT);
                conv.addMessage(msg);

                binder.getService().getBackgroundHandler().post(() -> server.getConnection().sendIRC().message(conv.getName(), message));
            }
            onConversationMessage(conv.getName());
		}
		et.setText("");
	}
	
	public void onInvite(final String channel) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(getString(R.string.activity_invite_title));
		adb.setMessage(getString(R.string.invited_to_channel, channel));
		adb.setPositiveButton("Yes", (dialog, which) -> binder.getService().getBackgroundHandler().post(() -> server.getConnection().sendIRC().joinChannel(channel)));
		adb.setNegativeButton("No", (dialog, which) -> {});
		adb.show();
	}

    public void onConnect() {
        if (settings.getBoolean("channel_list_on_connect", false)) {
            Intent channelListIntent = new Intent(this, ChannelListActivity.class);
            ArrayList<String> channels = server.getChannelList();
            channelListIntent.putStringArrayListExtra("channels", channels);
            startActivityForResult(channelListIntent, JOIN_CHANNEL_RESULT);
        }
        ((TextView)findViewById(R.id.connection_status)).setText(server.getConnection().getNick());
    }

	public void onDisconnect() {
        server.clearConversations();
		pagerAdapter.clearConversations();
		Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        ((TextView)findViewById(R.id.connection_status)).setText(R.string.not_connected);
	}
	
	public void onNewConversation(String name, boolean selected) {
		Conversation conv = Server.getInstance().getConversation(name);
        // Only add the new conversation if the conversation does not already exist.
        int i = pagerAdapter.getItemByName(name);
        if (i == -1) {
            i = pagerAdapter.addConversation(conv);
        }
        if (selected) {
            pager.setCurrentItem(i);
        }
		onConversationMessage(conv.getName());
	}

    public void onNewConversation(String name) {
        onNewConversation(name, false);
    }
	
	public void removeConversation(String name) {
		int i = pagerAdapter.getItemByName(name);
		pagerAdapter.removeConversation(i);
	}
	
	public void onConversationMessage(String name) {
		Conversation conv = Server.getInstance().getConversation(name);
        if (conv == null) {
            return;
        }
		int i = pagerAdapter.getItemByName(name);
		if (i == -1) {
			onNewConversation(name);
			i = pagerAdapter.getItemByName(name);
		}
        ConversationsPagerAdapter.ConversationInfo info = pagerAdapter.getItemInfo(i);
		if (!info.active) {
		    info.incrementUnreadMessages();
        }
		MessageListAdapter adapter = pagerAdapter.getItemAdapter(i);

		if (adapter == null) {
			return;
		}

		while (conv.hasBuffer()) {
			Message msg = conv.pollBuffer();
            // Don't log server window messages
			if (conv.getType() != Conversation.TYPE_SERVER) {
                binder.getService().getBackgroundHandler().post(() -> {
                    LogMessage logMessage = new LogMessage(
                        name,
                        conv.getType(),
                        msg.getSender(),
                        msg.getText()
                    );
                    db.logMessageDao().insert(logMessage);
                });
            }
			adapter.addMessage(msg);
		}

        binder.getService().updateNotification();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.binder = (IRCBinder)service;
        if (server.getConnection() == null || !server.getConnection().isConnected()) {
            ((TextView)findViewById(R.id.connection_status)).setText(R.string.connect_message);
        	binder.getService().connect();
        } else {
        	/*
        	 * The activity has resumed and the service has been bound, get all the messages we missed...
        	 */
    		for (Map.Entry<String, Conversation> conv : binder.getService().getServer().getConversations().entrySet()) {
    			int i = pagerAdapter.getItemByName(conv.getKey());
    			if (i == -1) {
    				onNewConversation(conv.getKey());
    			}
    			onConversationMessage(conv.getKey());
    		}
            // Join any channels we want to join...
            if (!joinChannelBuffer.isEmpty()) {
    		    binder.getService().getBackgroundHandler().post(() -> {
                    for (String channel : joinChannelBuffer) {
                        server.getConnection().sendIRC().joinChannel(channel);
                    }
                    joinChannelBuffer.clear();
                });
            }
            binder.getService().updateNotification();
        }
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		this.binder = null;
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.conversations, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
    	Conversation conversation = pagerAdapter.getItemInfo(pager.getCurrentItem()).conv;
        int id = item.getItemId();
        Intent intent;
        switch(id) {
            case R.id.action_close:
                switch (conversation.getType()) {
                    case Conversation.TYPE_CHANNEL:
                        binder.getService().getBackgroundHandler().post(() -> server.getConnection().getUserChannelDao().getChannel(conversation.getName()).send().part());
                        break;
                    case Conversation.TYPE_QUERY:
                        server.removeConversation(conversation.getName());
                        removeConversation(conversation.getName());
                        break;
                    default:
                        Toast.makeText(this, getResources().getString(R.string.close_server_window), Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            case R.id.action_disconnect:
                binder.getService().getBackgroundHandler().post(() -> server.getConnection().sendIRC().quitServer(settings.getString("quit_message", getString(R.string.default_quit_message))));
                break;
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.action_userlist:
                if (conversation.getType() == Conversation.TYPE_CHANNEL) {
                    drawerLayout.openDrawer(GravityCompat.END);
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
                } else if (server.getConnection().getUserChannelDao().getChannel(conversation.getName()).isOp(binder.getService().getConnection().getUserBot())) {
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

    @Override
    public void onConversationSelected(ConversationsPagerAdapter.ConversationInfo item) {
	    int i = pagerAdapter.getItemByName(item.conv.getName());
	    if (i != -1) {
	        pager.setCurrentItem(i);
        }
	    drawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onUserListItemClicked(String username) {
        Toast.makeText(this, username, Toast.LENGTH_LONG).show();
    }
}
