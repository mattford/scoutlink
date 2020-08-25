package uk.org.mattford.scoutlink.adapter;

import java.text.DateFormat;
import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.utils.MircColors;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {
    private LinkedList<Message> messages;

    public MessageListAdapter(LinkedList<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_list_item, parent, false);
        return new MessageListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context context = holder.mView.getContext();
        Message previousMessage = null;
        if (position > 0) {
            previousMessage = this.messages.get(position - 1);
        }
        Message message = this.messages.get(position);

        holder.mDivider.setVisibility(View.GONE);
        if (previousMessage == null ||
                message.getTimestamp().getTime() - previousMessage.getTimestamp().getTime() > 24 * 60 * 60 * 1000
        ) {
            holder.mDivider.setVisibility(View.VISIBLE);
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
            String dateString = dateFormat.format(message.getTimestamp());
            holder.mDate.setText(dateString);
        }
        TextView defaultTextView = new TextView(context);
        if (message.isType(Message.TYPE_ERROR)) {
            holder.mMessageView.setTextColor(context.getResources().getColor(R.color.red));
        } else {
            holder.mMessageView.setTextColor(defaultTextView.getTextColors());
        }

        String messageText = message.getText();
        if (message.isType(Message.TYPE_ACTION)) {
            messageText = context.getString(R.string.message_action, message.getText());
        }
        holder.mMessageView.setText(MircColors.toSpannable(messageText));
        holder.mMessageView.setTypeface(
            defaultTextView.getTypeface(),
            message.isType(Message.TYPE_ACTION) ? Typeface.ITALIC : Typeface.NORMAL
        );
        String sender = message.getSender();
        if (sender == null || message.isSenderType(Message.SENDER_TYPE_SERVER)) {
            holder.mSenderView.setVisibility(View.GONE);
        } else {
            holder.mSenderView.setVisibility(View.VISIBLE);
            if (message.isType(Message.TYPE_NOTICE)) {
                sender = context.getString(R.string.message_notice_sender, sender);
            }
            holder.mSenderView.setText(sender);
        }
        int defaultMarginPx = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            5,
            context.getResources().getDisplayMetrics()
        );
        int marginPx = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            100,
            context.getResources().getDisplayMetrics()
        );
        Drawable background = context.getResources().getDrawable(R.drawable.rounded_corners);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)holder.mMessageContainer.getLayoutParams();
        if (message.isSenderType(Message.SENDER_TYPE_SELF)) {
            holder.mMessageContainer.setGravity(Gravity.END);
            background.setColorFilter(context.getResources().getColor(R.color.outgoing_message_background), PorterDuff.Mode.SRC_IN);
        }
        layoutParams.leftMargin = message.isSenderType(Message.SENDER_TYPE_SELF) ? marginPx : defaultMarginPx;
        layoutParams.rightMargin = message.isSenderType(Message.SENDER_TYPE_OTHER) ? marginPx : defaultMarginPx;
        holder.mMessageView.setGravity(message.isSenderType(Message.SENDER_TYPE_SERVER) ? Gravity.CENTER : Gravity.START);
        holder.mMessageContainer.setBackground(background);

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
        final LinearLayout mDivider;
        final LinearLayout mMessageContainer;
        final TextView mDate;

        ViewHolder(View view) {
            super(view);
            mView = (LinearLayout) view;
            mSenderView = view.findViewById(R.id.sender);
            mTimestampView = view.findViewById(R.id.timestamp);
            mMessageContainer = view.findViewById(R.id.message_container);
            mMessageView = view.findViewById(R.id.message);
            mDivider = view.findViewById(R.id.date_divider);
            mDate = view.findViewById(R.id.date);
        }
    }
}
