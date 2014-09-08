package uk.org.mattford.scoutlink;

import uk.org.mattford.scoutlink.adapter.ConversationsPagerAdapter;
import uk.org.mattford.scoutlink.model.Conversation;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

public class ConversationsActivity extends FragmentActivity {
	
	private ConversationsPagerAdapter pagerAdapter;
	private ViewPager pager;
	private ActionBar.TabListener tabListener;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        pagerAdapter = new ConversationsPagerAdapter((FragmentManager) getSupportFragmentManager());
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
        
        final ActionBar actionBar = getActionBar();

        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
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

        // Add Server Tab
            actionBar.addTab(
                    actionBar.newTab()
                            .setText("Server")
                            .setTabListener(tabListener));
            
            Conversation test = new Conversation("#test");
            pagerAdapter.addConversation(test);
        }
	
	
	
	public void updateTabs() {
		int size = this.pagerAdapter.getCount();
		ActionBar act = getActionBar();
		for (int i = 0; i < size; i++) {
			act.addTab(act.newTab()
					.setText(this.pagerAdapter.getItemInfo(i).conv.getName())
					.setTabListener(tabListener)
					);
		}

	}
        
}
	
	


