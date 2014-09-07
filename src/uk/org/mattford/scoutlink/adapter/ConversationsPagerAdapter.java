package uk.org.mattford.scoutlink.adapter;

import java.util.LinkedList;

import uk.org.mattford.scoutlink.ConversationFragment;
import uk.org.mattford.scoutlink.model.Conversation;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ConversationsPagerAdapter extends FragmentStatePagerAdapter {
	
	private LinkedList<Conversation> conversations;

	public ConversationsPagerAdapter(FragmentManager fm) {
        super(fm);
        conversations = new LinkedList<Conversation>();
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new ConversationFragment();
        Bundle args = new Bundle();
        args.putInt(ConversationFragment.CONVERSATION_ID, i);
        // make cmmit
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return conversations.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
		return null;
    }

}
