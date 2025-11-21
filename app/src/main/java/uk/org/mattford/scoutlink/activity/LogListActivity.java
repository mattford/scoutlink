package uk.org.mattford.scoutlink.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.database.LogDatabase;
import uk.org.mattford.scoutlink.database.entities.LogMessage;
import uk.org.mattford.scoutlink.tasks.ExportLogFileTask;

public class LogListActivity extends EdgeToEdgeActivity {
    private LogDatabase logDatabase;
    private ArrayList<String> conversationNames;
    private String queuedExport;
    private ActivityResultLauncher<Intent> createDocumentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_list);
        
        createDocumentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        doExport(uri);
                    }
                }
            }
        );
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
        if (info == null) return true;
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
            launchExportFilePicker();
        }
        return true;
    }

    private void launchExportFilePicker() {
        String conversationName = this.queuedExport;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String fileName = "logs-" + conversationName + "-" + dateFormat.format(new Date()) + ".txt";
        
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        createDocumentLauncher.launch(intent);
    }

    private void doExport(@NonNull Uri uri) {
        String conversationName = this.queuedExport;
        new Thread(() -> {
            List<LogMessage> logMessages = logDatabase.logMessageDao().findByConversationSync(conversationName);
            (new ExportLogFileTask(logMessages, this, uri)).run();
        }).start();
    }
}
