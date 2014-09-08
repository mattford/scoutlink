package uk.org.mattford.scoutlink;

import java.util.ArrayList;

import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ConversationFragment extends Fragment {
	
	private Conversation conv;
    public static String CONVERSATION_ID = "conversation_id";
    
    public ConversationFragment(Conversation conv) {
    	this.conv = conv;
    }

	@Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.fragment_conversation, container, false);
        TextView messages = (TextView) rootView.findViewById(R.id.messages);
        ArrayList<Message> msgs = this.conv.getMessages();
        for (Message msg : msgs) {
        	messages.append(msg.sender + ": " + msg.text + '\n');
        }
      
        return rootView;
    }

}
