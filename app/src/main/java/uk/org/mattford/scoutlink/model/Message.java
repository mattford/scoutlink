package uk.org.mattford.scoutlink.model;

import android.content.Context;
import android.text.SpannableString;
import android.widget.TextView;

import uk.org.mattford.scoutlink.utils.MircColors;

public class Message {

	private String text;
    private Integer colour;
	
	public Message (String text) {
		this.text = text;
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
		TextView view = new TextView(context);
        SpannableString text = MircColors.toSpannable(getText());
		view.setText(text);
        if (colour != null) {
            view.setTextColor(colour);
        }

		return view;
	}
}
