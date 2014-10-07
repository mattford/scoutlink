package uk.org.mattford.scoutlink.activity;

import java.util.ArrayList;
import java.util.Map;

import org.jibble.pircbot.User;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.Scoutlink;
import uk.org.mattford.scoutlink.adapter.ConversationsPagerAdapter;
import uk.org.mattford.scoutlink.adapter.MessageListAdapter;
import uk.org.mattford.scoutlink.command.CommandParser;
import uk.org.mattford.scoutlink.irc.IRCBinder;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.receiver.ConversationReceiver;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class ConversationsActivity extends FragmentActivity implements ServiceConnection {
	
	private ConversationsPagerAdapter pagerAdapter;
	private ViewPager pager;
	private ActionBar.TabListener tabListener;
	private ActionBar actionBar;
	private ConversationReceiver receiver;
	private IRCBinder binder;
	
	public static final String PRE_CONNECT = "uk.org.mattford.scoutlink.ACTION_PRE_CONNECT";
	public final int JOIN_CHANNEL_RESULT = 1;
	
	private final String logTag = "ScoutLink/ConversationsActivity";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);
                 
        pagerAdapter = new ConversationsPagerAdapter(getSupportFragmentManager(), this);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });
        pager.setAdapter(pagerAdapter);
        
        this.actionBar = getActionBar();
        this.actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        this.tabListener = new ActionBar.TabListener() {

			@Override
			public void onTabSelected(Tab tab,
					android.app.FragmentTransaction ft) {
				pager.setCurrentItem(tab.getPosition());	
			}
			
			@Override
			public void onTabUnselected(Tab tab,
					android.app.FragmentTransaction ft) {
				// TODO Do nothing?	
			}
			
			@Override
			public void onTabReselected(Tab tab,
					android.app.FragmentTransaction ft) {
				// TODO Do nothing?
			}
        };           
            

    }
	
	public void onResume() {
		super.onResume();
		
		this.receiver = new ConversationReceiver(this);
		registerReceiver(this.receiver, new IntentFilter(Broadcast.NEW_CONVERSATION));
		registerReceiver(this.receiver, new IntentFilter(Broadcast.NEW_MESSAGE));
		registerReceiver(this.receiver, new IntentFilter(Broadcast.REMOVE_CONVERSATION));
		
		Intent serviceIntent = new Intent(this, IRCService.class);
		startService(serviceIntent);
		bindService(serviceIntent, this, 0);
		
		for (Map.Entry<String, Conversation> conv : Scoutlink.getInstance().getServer().getConversations().entrySet()) {
			if (pagerAdapter.getItemByName(conv.getKey()) == -1) {
				createNewConversation(conv.getKey());
			}
			newConversationMessage(conv.getKey());
		}
		
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
			String nickname = this.binder.getService().getConnection().getNick();
			conv.addMessage(new Message("<"+nickname+"> "+message));
			newConversationMessage(conv.getName());
			this.binder.getService().getConnection().sendMessage(conv.getName(), message);
		}
		
		et.setText("");
				
	}
	
	
	
	public void createNewConversation(String name) {
		actionBar.addTab(
				actionBar.newTab()
				.setText(name)
				.setTabListener(tabListener));
		Conversation conv = Scoutlink.getInstance().getServer().getConversation(name);
		pagerAdapter.addConversation(conv);
		newConversationMessage(conv.getName());
			
	}
	
	public void removeConversation(String name) {
		int i = pagerAdapter.getItemByName(name);
		pagerAdapter.removeConversation(i);
		actionBar.removeTabAt(i);
	}
	
	public void newConversationMessage(String name) {
		Conversation conv = Scoutlink.getInstance().getServer().getConversation(name);
		Log.d(logTag, "Message received for: "+name);
		int i = pagerAdapter.getItemByName(name);
		MessageListAdapter adapter = pagerAdapter.getItemAdapter(i);
		if (adapter == null) {
			Log.d(logTag, "Adapter for "+ name + " is null.");
			return;
		}
		while (conv.hasBuffer()) {
			Message msg = conv.pollBuffer();
			if (i != -1) {
				Log.d(logTag, "Adding "+ msg.getText()+" to " + name);
				adapter.addMessage(msg);
			}
			
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.binder = (IRCBinder)service;
        Intent intent = getIntent();
        String action = intent.getAction();
        if (action != null && action.equals(ConversationsActivity.PRE_CONNECT)) {
        	binder.getService().connect();
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
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {
        case R.id.action_settings:
        	
        	break;
        case R.id.action_close:
        	// TODO make this work for queries
        	String channel = pagerAdapter.getItemInfo(pager.getCurrentItem()).conv.getName();
        	this.binder.getService().getConnection().partChannel(channel);
        	
        	break;
        case R.id.action_disconnect:
        	this.binder.getService().getConnection().quitServer("ScoutLink for Android!");
        	Scoutlink.getInstance().getServer().clearConversations();
        	pagerAdapter.clearConversations();
        	setResult(RESULT_OK);
        	finish();
        	break;
        case R.id.action_userlist:
        	String chan = pagerAdapter.getItemInfo(pager.getCurrentItem()).conv.getName();
        	ArrayList<String> users = binder.getService().getConnection().getUsersAsStringArray(chan);
        	Intent intent = new Intent(this, UserListActivity.class);
        	intent.putStringArrayListExtra("users", users);
        	startActivity(intent);
        	break;
        case R.id.action_join:
        	Intent joinIntent = new Intent(this, JoinActivity.class);
        	startActivityForResult(joinIntent, JOIN_CHANNEL_RESULT);
        	break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == JOIN_CHANNEL_RESULT) {
            if (resultCode == RESULT_OK) {
                String channel = data.getStringExtra("target");
                binder.getService().getConnection().joinChannel(channel);
            }
        }
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	this.binder.getService().getConnection().quitServer("ScoutLink for Android! (User killed application)");
    	Intent service = new Intent(this, IRCService.class);
    	/*if (this.binder != null) { // Service should already be unbound at this point
    		unbindService(this);
    	}*/
    	stopService(service);
    }
        
}
	
	


