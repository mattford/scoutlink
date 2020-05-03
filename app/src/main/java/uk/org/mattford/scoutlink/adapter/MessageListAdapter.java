package uk.org.mattford.scoutlink.adapter;

import java.text.DateFormat;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.model.Message;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import static uk.org.mattford.scoutlink.model.Message.ALIGN_CENTER;
import static uk.org.mattford.scoutlink.model.Message.ALIGN_RIGHT;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private LinkedList<Message> messages;
    private Context context;

    public MessageListAdapter(Context context, LinkedList<Message> messages) {
        this.context = context;
        this.messages = messages;
//        loadLoggedMessages(messagesSnapshot);
    }

    private void loadLoggedMessages(List<Message> messages) {
//        Settings settings = new Settings(context);
//        if (conversation == null ||
//                conversation.getType() == Conversation.TYPE_SERVER ||
//                !settings.getBoolean("logging_enabled", true) ||
//                !settings.getBoolean("load_previous_messages_on_join", true)
//        ) {
//            for (Message msg : messages) {
//                addMessage(msg);
//            }
//            initialised = true;
//            processBuffer();
//            return;
//        }
//
//        int messagesToLoad = settings.getInteger("previous_messages_to_load", 10);
//
//        LogDatabase logDatabase = Room.databaseBuilder(context.getApplicationContext(), LogDatabase.class, "logs")
//                .addMigrations(LogDatabaseMigrations.MIGRATION_0_1)
//                .build();
//
//        new Thread(() -> {
//            List<LogMessage> logMessages = logDatabase.logMessageDao().findConversationMessagesWithLimit(conversation.getName(), messagesToLoad);
//            logDatabase.close();
//            (new Handler(Looper.getMainLooper())).post(() -> {
//                for (int i = logMessages.size() - 1; i >= 0; i--) {
//                    LogMessage msg = logMessages.get(i);
//                    Message message = new Message(msg.sender, msg.message, msg.date, null);
//                    addMessage(message);
//                }
//                if (!messages.isEmpty()) {
//                    for (Message msg : messages) {
//                        addMessage(msg);
//                    }
//                }
//                initialised = true;
//                processBuffer();
//            });
//        }).start();
    }

//    private boolean showMessageMetadata(Message message, Message previousMessage) {
//        if (previousMessage == null ||
//                (message.getSender() != null &&
//                        (previousMessage.getSender() == null ||
//                                !previousMessage.getSender().equalsIgnoreCase(message.getSender()))) ||
//                (message.getTimestamp() != null && previousMessage.getTimestamp() == null)
//        ) {
//            return true;
//        }
//
//        // If more that 30 mins has passed, show the meta regardless
//        Date previousTimestamp = previousMessage.getTimestamp();
//        Date currentTimestamp = message.getTimestamp();
//        return (currentTimestamp.getTime() - previousTimestamp.getTime() > (1000 * 60 * 30));
//    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_list_item, parent, false);
        return new MessageListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = this.messages.get(position);
        holder.mSenderView.setText(message.getSender());
        if (message.getSender() != null) {
            holder.mSenderView.setText(message.getSender());
        } else {
            holder.mSenderView.setVisibility(View.GONE);
        }

        holder.mMessageView.setText(message.getFormattedText());

        if (message.getAlignment() == ALIGN_RIGHT) {
            holder.mView.setGravity(Gravity.END);
            holder.mMessageView.setGravity(Gravity.END);
        } else if (message.getAlignment() == ALIGN_CENTER) {
            ViewGroup.LayoutParams params = holder.mMessageView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            holder.mMessageView.setLayoutParams(params);
            holder.mView.setGravity(Gravity.CENTER);
            holder.mMessageView.setGravity(Gravity.CENTER);
        }

        if (message.getColour() != null) {
            holder.mMessageView.setTextColor(message.getColour());
        }

        if (message.getBackgroundColour() != null) {
            holder.mView.setBackgroundColor(message.getBackgroundColour());
        }

        if (message.getTimestamp() != null) {
            DateFormat dateFormat = android.text.format.DateFormat.getTimeFormat(context);
            String dateString = dateFormat.format(message.getTimestamp());
            holder.mTimestampView.setText(dateString);
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout mView;
        final TextView mSenderView;
        final TextView mTimestampView;
        final TextView mMessageView;

        ViewHolder(View view) {
            super(view);
            mView = (LinearLayout) view;
            mSenderView = view.findViewById(R.id.sender);
            mTimestampView = view.findViewById(R.id.timestamp);
            mMessageView = view.findViewById(R.id.message);
        }
    }
}
