package uk.org.mattford.scoutlink.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.widget.TextView;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.utils.MircColors;

public class Message {

	private String text;
    private String sender;
    private int timestamp;
    private Integer colour;
    private Integer backgroundColour;
	
	public Message (String text) {
		this.text = text;
	}

    public Message (String sender, String text) {
        this.text = text;
        this.sender = sender;
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

	public TextView renderTextView(Context context) {
        LayoutInflater li = LayoutInflater.from(context);
		TextView view = (TextView) li.inflate(R.layout.message_list_item, null);

        SpannableString text = MircColors.toSpannable(getText());
        if (getSender() != null) {
            SpannableStringBuilder sbb = new SpannableStringBuilder();
            sbb.append(getSender());
            sbb.append('\n');
            sbb.append(text);
            text = new SpannableString(sbb);
        }
		view.setText(text);
        if (colour != null) {
            view.setTextColor(colour);
        }
        if (backgroundColour != null) {
            GradientDrawable bg = (GradientDrawable) view.getBackground();
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
