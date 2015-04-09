package uk.org.mattford.scoutlink.activity;

import java.util.ArrayList;
import java.util.Map;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.adapter.ConversationsPagerAdapter;
import uk.org.mattford.scoutlink.adapter.MessageListAdapter;
import uk.org.mattford.scoutlink.command.CommandParser;
import uk.org.mattford.scoutlink.irc.IRCBinder;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.model.Query;
import uk.org.mattford.scoutlink.model.Settings;
import uk.org.mattford.scoutlink.model.User;
import uk.org.mattford.scoutlink.receiver.ConversationReceiver;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;
import com.viewpagerindicator.TitlePageIndicator;

import org.pircbotx.Channel;

public class ConversationsActivity extends ActionBarActivity implements ServiceConnection {
	
	private ConversationsPagerAdapter pagerAdapter;
	private ViewPager pager;
	private ConversationReceiver receiver;
	private IRCBinder binder;
    private Settings settings;
    private Tracker tracker;
    private TitlePageIndicator indicator;

    private final int USER_LIST_RESULT = 0;
	private final int JOIN_CHANNEL_RESULT = 1;
    private final int NOTICE_RESULT = 2;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        settings = new Settings(this);
        //tracker = ((ScoutlinkApplication) getApplication()).getTracker(ScoutlinkApplication.TrackerName.APP_TRACKER);

        pagerAdapter = new ConversationsPagerAdapter(getSupportFragmentManager(), this);

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);



        indicator = (TitlePageIndicator)findViewById(R.id.nav_titles);
        indicator.setViewPager(pager);

        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            private int currentPage = -1;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                binder.getService().updateNotification();
                if (currentPage != -1 && pagerAdapter.getItemInfo(currentPage) != null) {
                    pagerAdapter.getItemInfo(currentPage).conv.setSelected(false);
                }
                currentPage = position;
                pagerAdapter.getItemInfo(position).conv.setSelected(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        
    }

	/**
	 * If this is not overridden, then ConversationsPagerAdapter retains old fragments when the activity is recreated.
	 */
	@Override
	protected void onSaveInstanceState(final Bundle outState) {
	    // super.onSaveInstanceState(outState);
	}
	
	public void onResume() {
		super.onResume();
		
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

        EditText newMessage = (EditText)findViewById(R.id.input);
        newMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event == null) {
                    onSendButtonClick(v);
                    return true;
                }
                return false;
            }
        });
	}
	
	public void onPause() {
		super.onPause();

		unregisterReceiver(this.receiver);
		unbindService(this);
	}
	
	public void onSendButtonClick(View v) {
		EditText et = (EditText)findViewById(R.id.input);
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
                String nickname = binder.getService().getConnection().getNick();
                Message msg = new Message(nickname, message);
                msg.setBackgroundColour(Color.parseColor("#0F1B5F"));
                msg.setColour(Color.WHITE);
                msg.setAlignment(Message.ALIGN_RIGHT);
                conv.addMessage(msg);

                binder.getService().getConnection().sendIRC().message(conv.getName(), message);
            }
            onConversationMessage(conv.getName());
		}
		et.setText("");

       /*tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Messages")
                .setAction("Send")
                .build()
        );*/

	}
	
	public void onInvite(final String channel) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(getString(R.string.activity_invite_title));
		adb.setMessage(getString(R.string.invited_to_channel, channel));
		adb.setPositiveButton("Yes", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				binder.getService().getConnection().sendIRC().joinChannel(channel);
			}
		});
		adb.setNegativeButton("No", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Do nothing.
			}
		});
		adb.show();
	}

    public void onConnect() {
        if (settings.getBoolean("channel_list_on_connect", false)) {
            Intent channelListIntent = new Intent(this, ChannelListActivity.class);
            ArrayList<String> channels = binder.getService().getServer().getChannelList();
            channelListIntent.putStringArrayListExtra("channels", channels);
            startActivityForResult(channelListIntent, JOIN_CHANNEL_RESULT);
        }
    }

	public void onDisconnect() {
		binder.getService().getServer().clearConversations();
		pagerAdapter.clearConversations();
		Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
	}
	
	public void onNewConversation(String name, boolean selected) {
		Conversation conv = binder.getService().getServer().getConversation(name);
		pagerAdapter.addConversation(conv);
		if (conv.isSelected() || selected) {
            indicator.setCurrentItem(pagerAdapter.getItemByName(conv.getName()));
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
		Conversation conv = binder.getService().getServer().getConversation(name);
		int i = pagerAdapter.getItemByName(name);
		if (i == -1) {
			onNewConversation(name);
			i = pagerAdapter.getItemByName(name);
		}
		MessageListAdapter adapter = pagerAdapter.getItemAdapter(i);

		if (adapter == null) {
			return;
		}

		while (conv.hasBuffer()) {
			Message msg = conv.pollBuffer();
			adapter.addMessage(msg);
		}

        binder.getService().updateNotification();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.binder = (IRCBinder)service;
        if (binder.getService().getConnection() == null || !binder.getService().getConnection().isConnected()) {
        	binder.getService().connect();
        } else {
        	/**
        	 * The activity has resumed and the service has been bound, get all the messages we missed...
        	 */
    		for (Map.Entry<String, Conversation> conv : binder.getService().getServer().getConversations().entrySet()) {
    			int i = pagerAdapter.getItemByName(conv.getKey());
    			if (i == -1) {
    				onNewConversation(conv.getKey());
    			}
    			onConversationMessage(conv.getKey());
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
        switch(id) {
        case R.id.action_settings:
        	startActivity(new Intent(this, SettingsActivity.class));
        	break;
        case R.id.action_close:
            switch (conversation.getType()) {
                case Conversation.TYPE_CHANNEL:
                    binder.getService().getConnection().getUserChannelDao().getChannel(conversation.getName()).send().part();
                    break;
                case Conversation.TYPE_QUERY:
                    binder.getService().getServer().removeConversation(conversation.getName());
                    removeConversation(conversation.getName());
                    break;
                default:
                    Toast.makeText(this, getResources().getString(R.string.close_server_window), Toast.LENGTH_SHORT).show();
                    break;
            }
        	break;
        case R.id.action_disconnect:
        	binder.getService().getConnection().sendIRC().quitServer(settings.getString("quit_message", getString(R.string.default_quit_message)));
        	break;
        case R.id.action_userlist:
            switch (conversation.getType()) {
                case Conversation.TYPE_CHANNEL:
                    String chan = conversation.getName();
                    ArrayList<String> users = new ArrayList<>();
                    users.addAll(binder.getService().getConnection().getUserChannelDao().getChannel(chan).getUsersNicks());
                    Intent intent = new Intent(this, UserListActivity.class);
                    intent.putStringArrayListExtra("users", users);

                    boolean isChanOp = binder.getService().getConnection().getUserChannelDao().getChannel(chan).isOp(binder.getService().getConnection().getUserBot());
                    intent.putExtra("isChanOp", isChanOp);
                    boolean isIrcOp = binder.getService().getConnection().getUserBot().isIrcop();
                    intent.putExtra("isIrcOp", isIrcOp);

                    startActivityForResult(intent, USER_LIST_RESULT);
                    break;
                default:
                    Toast.makeText(this, getResources().getString(R.string.userlist_not_on_channel), Toast.LENGTH_SHORT).show();
                    break;
            }
        	break;
        case R.id.action_join:
        	Intent joinIntent = new Intent(this, JoinActivity.class);
        	startActivityForResult(joinIntent, JOIN_CHANNEL_RESULT);
        	break;
        case R.id.action_channel_list:
            Intent channelListIntent = new Intent(this, ChannelListActivity.class);
            startActivityForResult(channelListIntent, JOIN_CHANNEL_RESULT);
            break;
        case R.id.action_channel_settings:
            if (conversation.getType() != Conversation.TYPE_CHANNEL) {
                Toast.makeText(this, getString(R.string.channel_settings_not_channel), Toast.LENGTH_SHORT).show();
            } else if (binder.getService().getConnection().getUserChannelDao().getChannel(conversation.getName()).isOp(binder.getService().getConnection().getUserBot())) {
                Intent intent = new Intent(this, ChannelSettingsActivity.class);
                intent.putExtra("channelName", conversation.getName());
                startActivity(intent);
            } else {
                Toast.makeText(this, getString(R.string.channel_settings_need_op), Toast.LENGTH_SHORT).show();
            }
            break;

        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case JOIN_CHANNEL_RESULT:
                if (resultCode == RESULT_OK) {
                    String channel = data.getStringExtra("target");
                    binder.getService().getConnection().sendIRC().joinChannel(channel);
                }
                break;
            case USER_LIST_RESULT:
                if (resultCode == RESULT_OK) {
                    String target = data.getStringExtra("target");
                    String chanStr = pagerAdapter.getItemInfo(pager.getCurrentItem()).conv.getName();
                    final Channel channel = binder.getService().getConnection().getUserChannelDao().getChannel(chanStr);
                    final org.pircbotx.User user = binder.getService().getConnection().getUserChannelDao().getUser(target);
                    switch (data.getIntExtra("action", -1)) {
                        case User.ACTION_QUERY:
                            Query query = new Query(target);
                            binder.getService().getServer().addConversation(query);
                            onNewConversation(query.getName());
                            break;
                        case User.ACTION_NOTICE:
                            Intent intent = new Intent(this, NoticeActivity.class);
                            intent.putExtra("target", target);
                            startActivityForResult(intent, NOTICE_RESULT);
                            break;
                        case User.ACTION_KICK:
                            final EditText input = new EditText(this);
                            new AlertDialog.Builder(this)
                                    .setTitle(R.string.action_kick_dialog_title)
                                    .setView(input)
                                    .setPositiveButton("Kick", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            channel.send().kick(user, input.getText().toString());
                                        }
                                    })
                                    .setNegativeButton("Cancel", new OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            // Nada.
                                        }
                                    })
                                    .show();
                            break;
                        case User.ACTION_KILL:
                            final EditText inputKill = new EditText(this);
                            new AlertDialog.Builder(this)
                                    .setTitle(R.string.action_kick_dialog_title)
                                    .setView(inputKill)
                                    .setPositiveButton("Kill", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            binder.getService().getConnection().sendRaw().rawLineNow("KILL " + user.getNick() + " " + inputKill.getText().toString());
                                        }
                                    })
                                    .setNegativeButton("Cancel", new OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            // Nada.
                                        }
                                    })
                                    .show();
                            break;
                        case User.ACTION_OP:
                            channel.send().op(user);
                            break;
                        case User.ACTION_DEOP:
                            channel.send().deOp(user);
                            break;
                        case User.ACTION_HOP:
                            channel.send().halfOp(user);
                            break;
                        case User.ACTION_DEHOP:
                            channel.send().deHalfOp(user);
                            break;
                        case User.ACTION_ADMIN:
                            channel.send().superOp(user);
                            break;
                        case User.ACTION_DEADMIN:
                            channel.send().deSuperOp(user);
                            break;
                        case User.ACTION_OWNER:
                            channel.send().owner(user);
                            break;
                        case User.ACTION_DEOWNER:
                            channel.send().deOwner(user);
                            break;
                        case User.ACTION_VOICE:
                            channel.send().voice(user);
                            break;
                        case User.ACTION_DEVOICE:
                            channel.send().deVoice(user);
                            break;
                    }
                }
                break;
            case NOTICE_RESULT:
                if (resultCode == RESULT_OK) {
                    String text = data.getStringExtra("message");
                    String target = data.getStringExtra("target");
                    Message msg = new Message("-> -"+target+"-", text);
                    binder.getService().getConnection().sendIRC().notice(target, text);
                    ConversationsPagerAdapter.ConversationInfo info = pagerAdapter.getItemInfo(pager.getCurrentItem());
                    info.conv.addMessage(msg);
                    onConversationMessage(info.conv.getName());
                }
                break;
        }
    }
}
