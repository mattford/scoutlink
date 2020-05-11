package uk.org.mattford.scoutlink.activity;

import android.content.Intent;
import android.os.Bundle;

import java.util.LinkedList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;
import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.adapter.MessageListAdapter;
import uk.org.mattford.scoutlink.database.LogDatabase;
import uk.org.mattford.scoutlink.database.entities.LogMessage;
import uk.org.mattford.scoutlink.database.migrations.LogDatabaseMigrations;
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
        logDatabase = Room.databaseBuilder(getApplicationContext(), LogDatabase.class, "logs")
                .addMigrations(LogDatabaseMigrations.MIGRATION_0_1)
                .build();
        loadMessagesFromChannel(channelName);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (logDatabase != null) {
            logDatabase.close();
        }
    }

    private void loadMessagesFromChannel(String channelName) {
        logDatabase.logMessageDao().findByConversation(channelName).observe(this, (logMessages) -> {
            LinkedList<Message> messages = new LinkedList<>();
            for (LogMessage msg : logMessages) {
                messages.add(msg.toMessage());
            }

            MessageListAdapter adapter = new MessageListAdapter(this, messages);
//            MessageListFragment fragment = new MessageListFragment();
//            fragment.setListAdapter(adapter);
//            replaceFragment(fragment);
        });
    }

    private void replaceFragment(Fragment destFragment)
    {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_placeholder, destFragment);
        fragmentTransaction.commit();
    }
}
