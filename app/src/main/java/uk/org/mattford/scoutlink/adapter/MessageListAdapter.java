package uk.org.mattford.scoutlink.adapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.room.Room;
import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.database.LogDatabase;
import uk.org.mattford.scoutlink.database.entities.LogMessage;
import uk.org.mattford.scoutlink.database.migrations.LogDatabaseMigrations;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.model.Settings;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MessageListAdapter extends ArrayAdapter<LinearLayout> {
	
	private ArrayList<LinearLayout> messages;
	private Context context;
    private Message previousMessage;
    private Conversation conversation;
		
	public MessageListAdapter(Context context, Conversation conv) {
		super(context, 0);

		this.context = context;
		this.messages = new ArrayList<>();
		this.conversation = conv;

		LinkedList<Message> messagesSnapshot = new LinkedList<>();
		messagesSnapshot.addAll(conv.getMessages());

		loadLoggedMessages(messagesSnapshot);
	}

	public MessageListAdapter(Context context, List<Message> messages) {
		super(context, 0);

		this.context = context;
		this.messages = new ArrayList<>();

		loadLoggedMessages(messages);
	}

	private void loadLoggedMessages(List<Message> messages) {
		Settings settings = new Settings(context);
		if (conversation == null ||
				conversation.getType() == Conversation.TYPE_SERVER ||
				!settings.getBoolean("logging_enabled", true) ||
				!settings.getBoolean("load_previous_messages_on_join", true)) {

            for (Message msg : messages) {
                addMessage(msg);
            }

			return;
		}

		int messagesToLoad = settings.getInteger("previous_messages_to_load", 10);

		LogDatabase logDatabase = Room.databaseBuilder(context.getApplicationContext(), LogDatabase.class, "logs")
				.addMigrations(LogDatabaseMigrations.MIGRATION_0_1)
				.build();

		new Thread(() -> {
			List<LogMessage> logMessages = logDatabase.logMessageDao().findConversationMessagesWithLimit(conversation.getName(), messagesToLoad);
			(new Handler(Looper.getMainLooper())).post(() -> {
				addMessage(new Message(context.getString(R.string.current_session_header)), true);
				for (LogMessage msg : logMessages) {
					Message message = new Message(msg.sender, msg.message, msg.date, null);
					addMessage(message, true);
				}
				addMessage(new Message(context.getString(R.string.previous_session_header)), true);
				for (Message msg : messages) {
					addMessage(msg);
				}
			});
		}).start();
	}
	public void addMessage(Message message) {
		addMessage(message, false);
	}
	public void addMessage(Message message, boolean prepend) {
        if (!prepend && message.getSender() != null &&
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
            if (prepend) {
            	messages.add(0, msgView);
			} else {
				messages.add(msgView);
			}
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
