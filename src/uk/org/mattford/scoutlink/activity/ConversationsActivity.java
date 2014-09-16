package uk.org.mattford.scoutlink.activity;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.Scoutlink;
import uk.org.mattford.scoutlink.adapter.ConversationsPagerAdapter;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.receiver.ConversationReceiver;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

public class ConversationsActivity extends FragmentActivity {
	
	private ConversationsPagerAdapter pagerAdapter;
	private ViewPager pager;
	private ActionBar.TabListener tabListener;
	private ActionBar actionBar;
	private ConversationReceiver receiver;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        pagerAdapter = new ConversationsPagerAdapter();
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
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTabReselected(Tab tab,
					android.app.FragmentTransaction ft) {
				// TODO Auto-generated method stub
				
			}
        };           
            

        }
	
	public void onResume() {
		super.onResume();
		
		this.receiver = new ConversationReceiver(this);
		registerReceiver(this.receiver, new IntentFilter(Broadcast.NEW_CONVERSATION));
		registerReceiver(this.receiver, new IntentFilter(Broadcast.NEW_MESSAGE));
		
	}
	
	public void onPause() {
		super.onPause();
		unregisterReceiver(this.receiver);
	}
	
	
	public void createNewConversation(String name) {
		actionBar.addTab(
				actionBar.newTab()
				.setText(name)
				.setTabListener(tabListener));
		Conversation conv = Scoutlink.getInstance().getServer().getConversation(name);
		pagerAdapter.addConversation(conv);
		
		
	}
	
	public void newConversationMessage(String name) {
		Conversation conv = Scoutlink.getInstance().getServer().getConversation(name);
		for (Message msg : conv.getBuffer()) {
			pagerAdapter.getItemInfo(pagerAdapter.getItemByName(name)).adapter.addMessage(msg);
		}
		conv.flushBuffer();
	}

        
}
	
	


