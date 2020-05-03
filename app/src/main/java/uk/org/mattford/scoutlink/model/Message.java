package uk.org.mattford.scoutlink.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.utils.MircColors;

public class Message {

	private String text;
    private String sender;
    private Date timestamp;
    private Integer colour;
    private Integer backgroundColour;
    private DateFormat dateFormat;

    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Set the timestamp for this message
     *
     * @param timestamp
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getAlignment() {
        return alignment;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    private int alignment;

    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_RIGHT = 1;
    public static final int ALIGN_CENTER = 2;
	
	public Message (String text) {
		this.text = text;
        this.timestamp = new Date();
        setAlignment(ALIGN_CENTER);
	}

    public Message (String sender, String text) {
	    this(text);
        this.sender = sender;
        this.alignment = ALIGN_LEFT;
        this.colour = Color.BLACK;
    }

    public Message (String sender, String text, Date timestamp, DateFormat dateFormat) {
	    this(sender, text);
	    this.timestamp = timestamp;
	    this.dateFormat = dateFormat;
    }

	public String getText() {
		return text;
	}

	public SpannableString getFormattedText() { return applySpans(getText()); }

	public void setText(String text) {
		this.text = text;
	}

    public Integer getColour() {
        return this.colour;
    }

    public void setColour(Integer colour) {
        this.colour = colour;
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

    private static SpannableString applySpans(String text) {
        return MircColors.toSpannable(text);
    }
}
