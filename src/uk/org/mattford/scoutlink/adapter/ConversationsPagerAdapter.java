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
			this.adapter = null;
			this.frag = null;
		}

	}
	
	@Override
    public Fragment getItem(int i) {
    	Log.d("ScoutLink", "ConversationsPagerAdapter.getItem("+i+") called.");
        return getView(i);
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
    	Log.d("ScoutLink", "getItemInfo("+Integer.toString(i)+")");
    	ConversationInfo info = null;
    	try {
    		info = conversations.get(i);
    	} catch (IndexOutOfBoundsException e) {
    		Log.d("ScoutLink", Integer.toString(i)+" is out of bounds.");
    		
    	}
    	return info;
    }

    
    public void addConversation(Conversation conv) {
    	conversations.add(new ConversationInfo(conv));
    	notifyDataSetChanged();
    }
    
    public void removeConversation(int position) {
    	conversations.remove(position);
    	notifyDataSetChanged();
    }
    
    public void clearConversations() {
    	conversations.clear();
    }
    
	@Override
	public int getCount() {
		return conversations.size();
	}
	

	

    
    public MessageListFragment getView(int i) {
    	ConversationInfo info = conversations.get(i);
    	Log.d("ScoutLink", "Creating new MessageListFragment for " + info.conv.getName());
    	MessageListFragment frag;
    	
    	if (info.frag == null) {
    		frag = new MessageListFragment(info.conv);
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
