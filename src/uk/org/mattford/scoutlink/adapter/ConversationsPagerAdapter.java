package uk.org.mattford.scoutlink.adapter;

import java.util.ArrayList;

import uk.org.mattford.scoutlink.activity.ConversationFragment;
import uk.org.mattford.scoutlink.model.Conversation;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ConversationsPagerAdapter extends FragmentStatePagerAdapter {
	
	private ArrayList<ConversationInfo> conversations;

	public ConversationsPagerAdapter(FragmentManager fm) {
        super(fm);
        conversations = new ArrayList<ConversationInfo>();
    }
	
	public class ConversationInfo {
		public String name;
		
		public ConversationInfo(Conversation conv) {
			this.name = conv.getName();
		}

	}

    @Override
    public Fragment getItem(int i) {
    	ConversationInfo cinfo = conversations.get(i);
        Fragment fragment = new ConversationFragment();
        Bundle args = new Bundle();
        args.putInt(ConversationFragment.CONVERSATION_ID, i);
        args.putString("name", cinfo.name);
        fragment.setArguments(args);
        return fragment;
    }
    
    public ConversationInfo getItemInfo(int i) {
    	return conversations.get(i);
    }

    @Override
    public int getCount() {
        return conversations.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
		ConversationInfo cinfo = conversations.get(position);
		return cinfo.name;
    }
    
    public void addConversation(Conversation conv) {
    	conversations.add(new ConversationInfo(conv));
    	notifyDataSetChanged();
    }
    
    public void removeConversation(int position) {
    	conversations.remove(position);
    	notifyDataSetChanged();
    }
    

}
