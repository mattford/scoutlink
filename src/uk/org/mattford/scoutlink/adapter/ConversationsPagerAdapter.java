package uk.org.mattford.scoutlink.adapter;

import java.util.ArrayList;

import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.views.MessageListView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class ConversationsPagerAdapter extends PagerAdapter {
	
	private ArrayList<ConversationInfo> conversations;

	public ConversationsPagerAdapter() {
        super();
        conversations = new ArrayList<ConversationInfo>();
    }
	
	public class ConversationInfo {
		public Conversation conv;
		public MessageListAdapter adapter;
		public MessageListView view;
		
		public ConversationInfo(Conversation conv) {
			this.conv = conv;
		}

	}

    public Conversation getItem(int i) {
    	return conversations.get(i).conv;
    }
    
    public int getItemByName(String name) {
    	for (int i = 0; i < conversations.size(); i++) {
    		if (conversations.get(i).conv.getName() == name) {
    			return i;
    		}
    	}
		return 0;
    	
    }
    
    public ConversationInfo getItemInfo(int i) {
    	return conversations.get(i);
    }

    
    public void addConversation(Conversation conv) {
    	conversations.add(new ConversationInfo(conv));
    	notifyDataSetChanged();
    }
    
    public void removeConversation(int position) {
    	conversations.remove(position);
    	notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		return conversations.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return false;
	}
	
    /**
     * Create a view object for the conversation at the given position.
     */
    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
    	Log.d("ScoutLink", "Instantiating item: " + position);
        ConversationInfo convInfo = conversations.get(position);
        View view;

        if (convInfo.view != null) {
            view = convInfo.view;
        } else {
            view = createView(convInfo, collection);
        }

        //views.put(position, view);
        ((ViewPager) collection).addView(view);

        return view;
    }
    
    public MessageListView createView(ConversationInfo info, View parent) {
        MessageListView list = new MessageListView(parent.getContext());
        info.view = list;
        //list.setOnItemClickListener(MessageClickListener.getInstance());

        MessageListAdapter adapter = info.adapter;

        if (adapter == null) {
            adapter = new MessageListAdapter(info.conv, parent.getContext());
            info.adapter = adapter;
        }


        list.setAdapter(adapter);
        list.setSelection(adapter.getCount() - 1); // scroll to bottom

        return list;

    }
    

}
