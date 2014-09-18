package uk.org.mattford.scoutlink.views;

import android.content.Context;
import android.util.Log;
import android.widget.ListView;

public class MessageListView extends ListView {

	public MessageListView(Context context) {
		super(context);
		Log.d("ScoutLink", "Created new MessageListView");
		setBackgroundColor(getResources().getColor(android.R.color.background_dark));
		setTranscriptMode(TRANSCRIPT_MODE_NORMAL);
		
	}
	

}
