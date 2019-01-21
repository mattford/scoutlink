package uk.org.mattford.scoutlink.adapter;

import java.util.LinkedList;
import java.util.List;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;

import android.content.Context;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MessageListAdapter extends ArrayAdapter<LinearLayout> {
	
	private LinkedList<LinearLayout> messages;
	private Context context;
    private Message previousMessage;
		
	public MessageListAdapter(Context context, Conversation conv) {
		super(context, 0);

		this.context = context;
		this.messages = new LinkedList<>();
		
		for (Message msg : conv.getMessages()) {
			addMessage(msg);
		}
	}

	public MessageListAdapter(Context context, List<Message> messages) {
		super(context, 0);

		this.context = context;
		this.messages = new LinkedList<>();

		for (Message msg : messages) {
			addMessage(msg);
		}
	}
	
	public void addMessage(Message message) {
        if (message.getSender() != null &&
                previousMessage != null &&
                previousMessage.getSender() != null &&
                previousMessage.getSender().equalsIgnoreCase(message.getSender())) {

            SpannableString msg = Message.applySpans(message.getText());
            LinearLayout lastLayout = getItem(getCount()-1);
            TextView lastTextView = lastLayout.findViewById(R.id.message);
            lastTextView.append("\n");
            lastTextView.append(msg);
            Linkify.addLinks(lastTextView, Linkify.WEB_URLS);
        } else {
            LinearLayout msgView = message.renderTextView(context);
            messages.add(msgView);
        }
        previousMessage = message;
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
