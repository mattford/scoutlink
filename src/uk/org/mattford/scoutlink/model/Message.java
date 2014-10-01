package uk.org.mattford.scoutlink.model;

import android.content.Context;
import android.widget.TextView;

public class Message {

	private String text;
	
	public Message (String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public TextView renderTextView(Context context) {
		TextView view = new TextView(context);
		// TODO: Use a parser and SpannableStrings here to show colours.
		view.setText(this.getText());

		return view;
	}
}
