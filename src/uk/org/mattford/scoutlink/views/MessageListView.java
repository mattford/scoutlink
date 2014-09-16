package uk.org.mattford.scoutlink.views;

import android.content.Context;
import android.widget.ListView;

public class MessageListView extends ListView {

	public MessageListView(Context context) {
		super(context);


        setDivider(null);
        
        setCacheColorHint(0x000000);
        setVerticalFadingEdgeEnabled(false);
        setScrollBarStyle(SCROLLBARS_OUTSIDE_INSET);

        // Scale padding by screen density
        float density = context.getResources().getDisplayMetrics().density;
        int padding = (int) (5 * density);
        setPadding(padding, padding, padding, padding);
		
		setTranscriptMode(TRANSCRIPT_MODE_NORMAL);
		
		

	}
	

}
