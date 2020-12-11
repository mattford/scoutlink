package uk.org.mattford.scoutlink.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.database.LogDatabase;
import uk.org.mattford.scoutlink.database.entities.LogMessage;
import uk.org.mattford.scoutlink.tasks.ExportLogFileTask;

public class LogListActivity extends AppCompatActivity {
    private LogDatabase logDatabase;
    private ArrayList<String> conversationNames;
    private String queuedExport;

    final int EXPORT_PERMISSION_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_list);
    }

    @Override
    public void onResume() {
        super.onResume();
        logDatabase = LogDatabase.getInstance();
        populateLogChannels();
    }

    private void populateLogChannels() {
        logDatabase.logMessageDao().findConversationNamesExcludingServerWindow().observe(this, logMessages -> {
            conversationNames = new ArrayList<>();
            for (LogMessage msg : logMessages) {
                conversationNames.add(msg.conversationName);
            }
            ListView lv = findViewById(R.id.conversation_list);
            lv.setAdapter(new ArrayAdapter<>(LogListActivity.this, android.R.layout.simple_list_item_1, conversationNames));
            lv.setOnItemClickListener((parent, view, position, id) -> {
                String name = conversationNames.get(position);
                if (name != null) {
                    Intent intent = new Intent(this, LogViewActivity.class);
                    intent.putExtra("channelName", name);
                    startActivity(intent);
                }
            });
            lv.setEmptyView(findViewById(android.R.id.empty));
            registerForContextMenu(lv);
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.log_list_item_context_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        LogListActivity context = this;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        String conversationName = conversationNames.get(info.position);
        int itemId = item.getItemId();
        if (itemId == R.id.log_action_delete) {
            new Thread(() -> {
                int affectedRows = logDatabase.logMessageDao().deleteByConversation(conversationName);
                if (affectedRows > 0) {
                    context.runOnUiThread(() -> Toast.makeText(context, getString(R.string.logs_deleted, conversationName), Toast.LENGTH_LONG).show());
                }

            }).start();
        } else if (itemId == R.id.log_action_export) {
            queuedExport = conversationName;
            checkExportPermission();
        }
        return true;
    }

    private void doExport() {
        String conversationName = this.queuedExport;
        new Thread(() -> {
            List<LogMessage> logMessages = logDatabase.logMessageDao().findByConversationSync(conversationName);
            (new ExportLogFileTask(logMessages, conversationName, this)).run();
        }).start();
    }

    private void checkExportPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    EXPORT_PERMISSION_REQUEST);
        } else {
            doExport();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == EXPORT_PERMISSION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            doExport();
        }
    }
}
