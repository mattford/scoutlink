package uk.org.mattford.scoutlink.adapter;

import java.util.LinkedList;

import uk.org.mattford.scoutlink.activity.MessageListFragment;
import uk.org.mattford.scoutlink.model.Conversation;
import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

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
		public int unreadMessages;
		public boolean active;
		private OnUnreadMessagesChangedListener listener;
		
		public ConversationInfo(Conversation conv) {
			this.conv = conv;
			this.adapter = new MessageListAdapter(context, conv);
			this.frag = null;
			this.active = false;
			this.unreadMessages = 0;
		}

		public void setActive(boolean active) {
			if (!active) {
				this.resetUnreadMessages();
			}
			this.active = active;
		}

		public void resetUnreadMessages() {
			this.unreadMessages = 0;
			onUnreadMessagesChanged();
		}

		public void incrementUnreadMessages() {
			this.unreadMessages++;
			onUnreadMessagesChanged();
		}

		public void setOnUnreadMessagesChangedListener(OnUnreadMessagesChangedListener listener) {
			this.listener = listener;
		}

		public void onUnreadMessagesChanged() {
			if (this.listener != null) {
				listener.onUnreadMessagesChanged(this.unreadMessages);
			}
		}
	}

	public interface OnUnreadMessagesChangedListener {
		public void onUnreadMessagesChanged(int unreadMessagesCount);
	}

	public LinkedList<ConversationInfo> getConversations() {
		return this.conversations;
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	public void setActiveItem(int position) {
		for (int i = 0; i < conversations.size(); i++) {
			ConversationInfo conversationInfo = conversations.get(i);
			if (conversationInfo != null) {
				conversationInfo.setActive(i == position);
			}
		}
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

    public int addConversation(Conversation conv) {
    	conversations.add(new ConversationInfo(conv));
    	notifyDataSetChanged();
    	return conversations.size() - 1;
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
	
    private MessageListFragment getView(int i) {
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
