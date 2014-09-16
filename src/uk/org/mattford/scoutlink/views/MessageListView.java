package uk.org.mattford.scoutlink.views;

import android.content.Context;
import android.widget.ListView;

public class MessageListView extends ListView {

	public MessageListView(Context context) {
		super(context);
		setTranscriptMode(TRANSCRIPT_MODE_NORMAL);
	}

}
