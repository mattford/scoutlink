package uk.org.mattford.scoutlink.adapter;


import java.util.LinkedList;

import uk.org.mattford.scoutlink.activity.MessageListFragment;
import uk.org.mattford.scoutlink.model.Conversation;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

public class ConversationsPagerAdapter extends FragmentStatePagerAdapter {
	
	private LinkedList<ConversationInfo> conversations;
	private Context context;
	
	private final String logTag = "ScoutLink/ConversationsPagerAdapter";

	public ConversationsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        conversations = new LinkedList<ConversationInfo>();
        this.context = context;
    }
	
	public class ConversationInfo {
		public Conversation conv;
		public MessageListAdapter adapter;
		public MessageListFragment frag;
		
		public ConversationInfo(Conversation conv) {
			this.conv = conv;
			this.adapter = new MessageListAdapter(conv, context);
			this.frag = null;
		}

	}
	
	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}
	
	@Override
    public Fragment getItem(int i) {
    	Log.d(logTag, "ConversationsPagerAdapter.getItem("+i+") called.");
        return getView(i);
    }
	
	public MessageListAdapter getItemAdapter(int position) {
		ConversationInfo info = getItemInfo(position);
		if (info == null) {
			return null;
		}
		return info.adapter;
	}
    
    public int getItemByName(String name) {
    	for (int i = 0; i < conversations.size(); i++) {
    		if (conversations.get(i).conv.getName().equalsIgnoreCase(name)) {
    			return i;
    		}
    	}
		return -1;	// TODO: Should this return null if not found?
    }
    
    public ConversationInfo getItemInfo(int i) {
    	Log.d(logTag, "getItemInfo("+Integer.toString(i)+")");
    	ConversationInfo info = null;
    	try {
    		info = conversations.get(i);
    	} catch (IndexOutOfBoundsException e) {
    		Log.d(logTag, Integer.toString(i)+" is out of bounds.");
    		return null;
    	}
    	return info;
    }

    
    public void addConversation(Conversation conv) {
    	conversations.add(new ConversationInfo(conv));
    	notifyDataSetChanged();
    }
    
    public void removeConversation(int position) {
    	Log.d(logTag, "Removing conversation at "+position);
    	conversations.remove(position);
    	for (int i = 0; i<conversations.size();i++) {
    		Log.d(logTag, i +": "+conversations.get(i).conv.getName());
    	}
    	notifyDataSetChanged();
    }
    
    public void clearConversations() {
    	conversations.clear();
    	notifyDataSetChanged();
    }
    
	@Override
	public int getCount() {
		return conversations.size();
	}
	
    public MessageListFragment getView(int i) {
    	ConversationInfo info = conversations.get(i);

    	MessageListFragment frag;
    	
    	if (info.frag == null) {
    		frag = new MessageListFragment();
    	} else {
    		frag = info.frag;
    	}

        MessageListAdapter adapter = info.adapter;

        if (adapter == null) {
            adapter = new MessageListAdapter(info.conv, context);
            info.adapter = adapter;
        }


        frag.setListAdapter(adapter);

        return frag;

    }
    

}
