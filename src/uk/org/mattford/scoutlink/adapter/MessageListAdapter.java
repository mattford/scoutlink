package uk.org.mattford.scoutlink.adapter;

import java.util.LinkedList;

import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MessageListAdapter extends ArrayAdapter<TextView> {
	
	private LinkedList<TextView> messages;
	private Conversation conversation;
	private Context context;
	
	private final String logTag = "ScoutLink/MessageListAdapter";
	
	public MessageListAdapter(Conversation conversation, Context context) {
		super(context, 0);
		this.conversation = conversation;
		this.context = context;
		this.messages = new LinkedList<TextView>();
		Log.d(logTag, "Creating MessageListAdapter for "+conversation.getName());
	}
	
	public void addMessage(Message message) {
		TextView tv = message.renderTextView(context);
		messages.add(tv);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		//Log.d(logTag, "MessageListAdapter.getCount() ("+this.conversation.getName()+") called, returning: " + messages.size());
		return messages.size();
	}

	@Override
	public TextView getItem(int position) {
		//Log.d(logTag, "MessageListAdapter.getItem() ("+this.conversation.getName()+") called: " + position);
		return messages.get(position);
	}

	@Override
	public long getItemId(int position) {
		//Log.d(logTag, "MessageListAdapter.getItemId() ("+this.conversation.getName()+") called: "+position);
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//Log.d(logTag, "MessageListAdapter.getView() called: " + position);
		convertView = getItem(position);
		return convertView;
	}



}
