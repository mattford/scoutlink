package uk.org.mattford.scoutlink.activity;


import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.model.Conversation;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


public class MessageListFragment extends ListFragment {

	private Conversation conv;

	public MessageListFragment(Conversation conv) {
		super();
		this.conv = conv;


	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	Log.d("ScoutLink", "MessageListFragment.onCreateView() called.");
        View v = inflater.inflate(R.layout.message_list_view, container, false);
        ListView lv = (ListView)v.findViewById(android.R.id.list);
        lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        return v;
    }
	
	

}
