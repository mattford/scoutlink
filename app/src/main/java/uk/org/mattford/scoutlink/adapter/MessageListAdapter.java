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
    private Context context;

    public MessageListAdapter(Context context, LinkedList<Message> messages) {
        this.context = context;
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
        Message message = this.messages.get(position);

        String messageText = message.getText();
        if (message.isType(Message.TYPE_ACTION)) {
            messageText = context.getString(R.string.message_action, message.getText());
        } else if (message.isType(Message.TYPE_ERROR)) {
            holder.mMessageView.setTextColor(context.getResources().getColor(R.color.red));
        }
        holder.mMessageView.setText(MircColors.toSpannable(messageText));

        String sender = message.getSender();
        if (sender == null || !message.isSenderType(Message.SENDER_TYPE_OTHER)) {
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
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams)holder.mView.getLayoutParams();
        if (message.isSenderType(Message.SENDER_TYPE_SELF)) {
            holder.mView.setGravity(Gravity.END);
            background.setColorFilter(holder.mView.getResources().getColor(R.color.light_green), PorterDuff.Mode.MULTIPLY);
            layoutParams.leftMargin = marginPx;
            layoutParams.rightMargin = defaultMarginPx;
            holder.mView.setLayoutParams(layoutParams);
        } else if (message.isSenderType(Message.SENDER_TYPE_OTHER)) {
            layoutParams.leftMargin = defaultMarginPx;
            layoutParams.rightMargin = marginPx;
            holder.mMessageView.setGravity(Gravity.START);
        } else if (message.isSenderType(Message.SENDER_TYPE_SERVER)) {
            layoutParams.leftMargin = defaultMarginPx;
            layoutParams.rightMargin = defaultMarginPx;
            holder.mMessageView.setGravity(Gravity.CENTER);
        }
        holder.mView.setBackgroundDrawable(background);
        holder.mView.setLayoutParams(layoutParams);

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
