package uk.org.mattford.scoutlink.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.database.LogDatabase;
import uk.org.mattford.scoutlink.database.entities.LogMessage;
import uk.org.mattford.scoutlink.database.migrations.LogDatabaseMigrations;

public class LogListActivity extends AppCompatActivity {
    private LogDatabase logDatabase;
    private ArrayList<String> conversationNames;

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
            conversationNames = new ArrayList<>();
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
            registerForContextMenu(lv);
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.conversation_list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle(conversationNames.get(info.position));
            String[] menuItems = getResources().getStringArray(R.array.log_list_context_menu);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Context context = this;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        if (menuItemIndex == 0) {
            String conversationName = conversationNames.get(info.position);
            new Thread(() -> {
                int affectedRows = logDatabase.logMessageDao().deleteByConversation(conversationName);
                if (affectedRows > 0) {
                    ((LogListActivity) context).runOnUiThread(() -> {
                        Toast.makeText(context, getString(R.string.logs_deleted, conversationName), Toast.LENGTH_LONG).show();
                    });
                }

            }).start();
        }
        return true;
    }
}
