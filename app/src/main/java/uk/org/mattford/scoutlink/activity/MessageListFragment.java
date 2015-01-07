package uk.org.mattford.scoutlink.activity;


import uk.org.mattford.scoutlink.R;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


public class MessageListFragment extends ListFragment {

	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.message_list_view, container, false);
        ListView lv = (ListView)v.findViewById(android.R.id.list);

        lv.setDivider(null);
        lv.setDividerHeight(0);
        lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        lv.setStackFromBottom(true);
        
        return v;
    }
	
	

}
