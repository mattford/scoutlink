package uk.org.mattford.scoutlink.adapter;


import java.util.LinkedList;

import uk.org.mattford.scoutlink.activity.MessageListFragment;
import uk.org.mattford.scoutlink.model.Conversation;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ConversationsPagerAdapter extends FragmentStatePagerAdapter {
	
	private LinkedList<ConversationInfo> conversations;
	private Context context;

	public ConversationsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        conversations = new LinkedList<>();
        this.context = context;
    }
	

	
	public class ConversationInfo {
		public Conversation conv;
		public MessageListAdapter adapter;
		public MessageListFragment frag;
		
		public ConversationInfo(Conversation conv) {
			this.conv = conv;
			this.adapter = new MessageListAdapter(context, conv);
			this.frag = null;
		}

	}
	
	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}
	
	@Override
    public Fragment getItem(int i) {
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
		return -1;
    }

    public String getPageTitle(int position) {
        return conversations.get(position).conv.getName();
    }
    
    public ConversationInfo getItemInfo(int i) {
    	ConversationInfo info;
    	try {
    		info = conversations.get(i);
    	} catch (IndexOutOfBoundsException e) {
    		return null;
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
    	notifyDataSetChanged();
    }
    
	@Override
	public int getCount() {
		return conversations.size();
	}
	
    protected MessageListFragment getView(int i) {
    	ConversationInfo info = conversations.get(i);

    	MessageListFragment frag;
    	
    	if (info.frag == null) {
    		frag = new MessageListFragment();
    	} else {
    		frag = info.frag;
    	}

        MessageListAdapter adapter = info.adapter;

        if (adapter == null) {
            adapter = new MessageListAdapter(context, info.conv);
            info.adapter = adapter;
        }


        frag.setListAdapter(adapter);
        

        return frag;

    }

    

}
