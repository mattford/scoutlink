package uk.org.mattford.scoutlink.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Settings;

public class SettingsActivity extends AppCompatActivity {

    private Settings settings;

    private final int AUTOJOIN_REQUEST_CODE = 0;
    private final int CONNECT_COMMANDS_REQUEST_CODE = 1;
    private final int NOTIFY_LIST_REQUEST_CODE = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings = new Settings(this);

        EditText et;

        et = findViewById(R.id.settings_nickname);
        et.setText(settings.getString("nickname", ""));

        et = findViewById(R.id.settings_ident);
        et.setText(settings.getString("ident", "androidirc"));

        et = findViewById(R.id.settings_gecos);
        et.setText(settings.getString("gecos", "ScoutLink IRC for Android!"));

        et = findViewById(R.id.settings_nickserv_user);
        et.setText(settings.getString("nickserv_user", ""));

        et = findViewById(R.id.settings_nickserv_password);
        et.setText(settings.getString("nickserv_password", ""));

        et = findViewById(R.id.settings_quit_message);
        et.setText(settings.getString("quit_message", getString(R.string.default_quit_message)));
    }

    public void openAutojoinSettings(View v) {
        Intent intent = new Intent(this, ListEditActivity.class);

        intent.putExtra("title", getString(R.string.settings_autojoin_channels_label));
        intent.putExtra("firstChar", "#");
        intent.putStringArrayListExtra("items", settings.getStringArrayList("autojoin_channels"));
        startActivityForResult(intent, AUTOJOIN_REQUEST_CODE);
    }

    public void openCommandOnConnectSettings(View v) {
        Intent intent = new Intent(this, ListEditActivity.class);

        intent.putStringArrayListExtra("items", settings.getStringArrayList("command_on_connect"));
        intent.putExtra("title", getString(R.string.settings_command_on_connect_label));
        intent.putExtra("firstChar", "/");
        startActivityForResult(intent, CONNECT_COMMANDS_REQUEST_CODE);
    }

    public void openNotifyListSettings(View v) {
        Intent intent = new Intent(this, ListEditActivity.class);

        intent.putStringArrayListExtra("items", settings.getStringArrayList("notify_list"));
        intent.putExtra("title", getString(R.string.settings_notify_list_label));
        intent.putExtra("firstChar", "");
        startActivityForResult(intent, NOTIFY_LIST_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AUTOJOIN_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    settings.putStringArrayList("autojoin_channels", data.getStringArrayListExtra("items"));
                }
                break;
            case CONNECT_COMMANDS_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    settings.putStringArrayList("command_on_connect", data.getStringArrayListExtra("items"));
                }
                break;
            case NOTIFY_LIST_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    settings.putStringArrayList("notify_list", data.getStringArrayListExtra("items"));
                    ArrayList<String> newItems = data.getStringArrayListExtra("newItems");
                    ArrayList<String> removedItems = data.getStringArrayListExtra("removedItems");
                    Intent addNotify = new Intent(this, IRCService.class);
                    addNotify.setAction(IRCService.ACTION_ADD_NOTIFY);
                    addNotify.putStringArrayListExtra("items", newItems);
                    startService(addNotify);
                    Intent removeNotify = new Intent(this, IRCService.class);
                    removeNotify.setAction(IRCService.ACTION_REMOVE_NOTIFY);
                    removeNotify.putStringArrayListExtra("items", removedItems);
                    startService(removeNotify);
                }
                break;
        }
    }
}
