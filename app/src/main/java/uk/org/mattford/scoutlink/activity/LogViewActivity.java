package uk.org.mattford.scoutlink.activity;

import android.content.Intent;
import android.os.Bundle;

import java.util.LinkedList;

import androidx.appcompat.app.AppCompatActivity;
import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.database.LogDatabase;
import uk.org.mattford.scoutlink.database.entities.LogMessage;
import uk.org.mattford.scoutlink.fragment.MessageListFragment;
import uk.org.mattford.scoutlink.model.Message;

public class LogViewActivity extends AppCompatActivity {
    private LogDatabase logDatabase;
    private String channelName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_view);
        Intent startingIntent = getIntent();
        channelName = startingIntent.getStringExtra("channelName");
    }

    @Override
    public void onResume() {
        super.onResume();
        logDatabase = LogDatabase.getInstance();
        loadMessagesFromChannel(channelName);
    }

    private void loadMessagesFromChannel(String channelName) {
        logDatabase.logMessageDao().findByConversation(channelName).observe(this, (logMessages) -> {
            LinkedList<Message> messages = new LinkedList<>();
            for (LogMessage msg : logMessages) {
                messages.add(msg.toMessage());
            }

            MessageListFragment messageListFragment = (MessageListFragment) getSupportFragmentManager().findFragmentById(R.id.conversation_view);
            if (messageListFragment != null) {
                messageListFragment.setDataSource(messages);
            }
        });
    }
}
