package uk.org.mattford.scoutlink.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.utils.MircColors;

public class Message {

	private String text;
    private String sender;
    private int timestamp;
    private Integer colour;
    private Integer backgroundColour;

    public int getAlignment() {
        return alignment;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    private int alignment;

    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_RIGHT = 1;
	
	public Message (String text) {
		this.text = text;
	}

    public Message (String sender, String text) {
        this.text = text;
        this.sender = sender;
        this.alignment = ALIGN_LEFT;
        this.backgroundColour = Color.LTGRAY;
        this.colour = Color.BLACK;
    }

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

    public Integer getColour() {
        return this.colour;
    }

    public void setColour(Integer colour) {
        this.colour = colour;
    }

	public LinearLayout renderTextView(Context context) {
        LayoutInflater li = LayoutInflater.from(context);
		LinearLayout view;

        SpannableString text = MircColors.toSpannable(getText());

        if (getSender() != null) {
            view = (LinearLayout) li.inflate(R.layout.message_list_item, null);
            TextView senderView = (TextView)view.findViewById(R.id.sender);
            senderView.setText(getSender());
        } else {
            view = (LinearLayout) li.inflate(R.layout.message_list_item_no_sender, null);
        }

        if (getAlignment() == ALIGN_RIGHT) {
            view.setGravity(Gravity.RIGHT);
        }

        TextView messageView = (TextView)view.findViewById(R.id.message);
		messageView.setText(text);

        if (colour != null) {
            messageView.setTextColor(colour);
        }

        if (backgroundColour != null) {
            GradientDrawable bg = (GradientDrawable) messageView.getBackground();
            bg.setColor(backgroundColour);
        }

		return view;
	}

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Integer getBackgroundColour() {
        return backgroundColour;
    }

    public void setBackgroundColour(Integer backgroundColour) {
        this.backgroundColour = backgroundColour;
    }
}
