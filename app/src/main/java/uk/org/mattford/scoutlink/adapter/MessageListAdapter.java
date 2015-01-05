package uk.org.mattford.scoutlink.adapter;

import java.util.LinkedList;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MessageListAdapter extends ArrayAdapter<LinearLayout> {
	
	private LinkedList<LinearLayout> messages;
	private Context context;
	private Conversation conversation;
		
	public MessageListAdapter(Context context, Conversation conv) {
		super(context, 0);

		this.context = context;
		this.conversation = conv;
		this.messages = new LinkedList<LinearLayout>();
		
		for (Message msg : conversation.getMessages()) {
			addMessage(msg);
		}
		
		
		
	}
	
	public void addMessage(Message message) {

        if (message.getSender() != null) {
            Message lastMessage = conversation.getMessages().get(conversation.getMessages().size()-2);

            if (lastMessage.getSender() != null && lastMessage.getSender().equalsIgnoreCase(message.getSender())) {
                LinearLayout lastLayout = getItem(getCount()-1);
                TextView lastTextView = (TextView)lastLayout.findViewById(R.id.message);
                lastTextView.setText(lastTextView.getText().toString() + '\n' + message.getText());
            } else {
                LinearLayout msgView = message.renderTextView(context);
                messages.add(msgView);
            }
        } else {
            LinearLayout msgView = message.renderTextView(context);
            messages.add(msgView);
        }
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return messages.size();
	}

	@Override
	public LinearLayout getItem(int position) {
		return messages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = getItem(position);
		return convertView;
	}



}
