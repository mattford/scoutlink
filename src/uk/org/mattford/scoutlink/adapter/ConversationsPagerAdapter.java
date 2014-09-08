package uk.org.mattford.scoutlink.adapter;

import java.util.LinkedList;

import uk.org.mattford.scoutlink.ConversationFragment;
import uk.org.mattford.scoutlink.model.Conversation;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ConversationsPagerAdapter extends FragmentStatePagerAdapter {
	
	private LinkedList<ConversationInfo> conversations;

	public ConversationsPagerAdapter(FragmentManager fm) {
        super(fm);
        conversations = new LinkedList<ConversationInfo>();
    }
	
	public class ConversationInfo {
		public Conversation conv;
		
		public ConversationInfo(Conversation conv) {
			this.conv = conv;
		}
	}

    @Override
    public Fragment getItem(int i) {
    	ConversationInfo cinfo = conversations.get(i);
    	Conversation conv = cinfo.conv;
    	
        Fragment fragment = new ConversationFragment(conv);
        Bundle args = new Bundle();
        args.putInt(ConversationFragment.CONVERSATION_ID, i);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return conversations.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
		ConversationInfo cinfo = conversations.get(position);
		return cinfo.conv.getName();
    }

}
