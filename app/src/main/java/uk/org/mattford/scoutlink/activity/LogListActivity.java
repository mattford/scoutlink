package uk.org.mattford.scoutlink.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.database.LogDatabase;
import uk.org.mattford.scoutlink.database.entities.LogMessage;
import uk.org.mattford.scoutlink.database.migrations.LogDatabaseMigrations;

public class LogListActivity extends AppCompatActivity {
    private LogDatabase logDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_list);
    }

    @Override
    public void onResume() {
        super.onResume();
        logDatabase = Room.databaseBuilder(getApplicationContext(), LogDatabase.class, "logs")
                .addMigrations(LogDatabaseMigrations.MIGRATION_0_1)
                .build();

        populateLogChannels();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (logDatabase != null) {
            logDatabase.close();
        }
    }

    private void populateLogChannels() {
        logDatabase.logMessageDao().findConversationNamesExcludingServerWindow().observe(this, logMessages -> {
            ArrayList<String> conversationNames = new ArrayList<>();
            for (LogMessage msg : logMessages) {
                conversationNames.add(msg.conversationName);
            }
            ListView lv = findViewById(R.id.conversation_list);
            lv.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, conversationNames));
            lv.setOnItemClickListener((parent, view, position, id) -> {
                String name = conversationNames.get(position);
                if (name != null) {
                    Intent intent = new Intent(this, LogViewActivity.class);
                    intent.putExtra("channelName", name);
                    startActivity(intent);
                }
            });
        });
    }
}
