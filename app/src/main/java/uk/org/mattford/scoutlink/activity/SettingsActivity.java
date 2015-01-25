package uk.org.mattford.scoutlink.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.model.Settings;

public class SettingsActivity extends Activity {

    private Settings settings;

    private final int AUTOJOIN_REQUEST_CODE = 0;
    private final int CONNECT_COMMANDS_REQUEST_CODE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings = new Settings(this);

        EditText et;

        et = (EditText)findViewById(R.id.settings_nickname);
        et.setText(settings.getString("nickname", ""));

        et = (EditText)findViewById(R.id.settings_ident);
        et.setText(settings.getString("ident", "androidirc"));

        et = (EditText)findViewById(R.id.settings_gecos);
        et.setText(settings.getString("gecos", "ScoutLink IRC for Android!"));

        et = (EditText)findViewById(R.id.settings_nickserv_user);
        et.setText(settings.getString("nickserv_user", ""));

        et = (EditText)findViewById(R.id.settings_nickserv_password);
        et.setText(settings.getString("nickserv_password", ""));


    }

    public void openAutojoinSettings(View v) {
        Intent intent = new Intent(this, ListViewEditActivity.class);
        Log.d("SL", settings.getString("autojoin_channels"));
        String[] strs = settings.getStringArray("autojoin_channels");
        for (String str : strs) {
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        }
        intent.putStringArrayListExtra("items", new ArrayList<String>(Arrays.asList(settings.getStringArray("autojoin_channels"))));
        startActivityForResult(intent, AUTOJOIN_REQUEST_CODE);
    }

    public void openCommandOnConnectSettings(View v) {
        Intent intent = new Intent(this, ListViewEditActivity.class);

        intent.putStringArrayListExtra("items", new ArrayList<String>(Arrays.asList(settings.getStringArray("command_on_connect"))));
        startActivityForResult(intent, CONNECT_COMMANDS_REQUEST_CODE);
    }

    @Override
    public void onPause() {
        super.onPause();
        EditText et;
        et = (EditText)findViewById(R.id.settings_nickname);
        settings.putString("nickname", et.getText().toString());

        et = (EditText)findViewById(R.id.settings_ident);
        settings.putString("ident", et.getText().toString());

        et = (EditText)findViewById(R.id.settings_gecos);
        settings.putString("gecos", et.getText().toString());

        et = (EditText)findViewById(R.id.settings_nickserv_user);
        settings.putString("nickserv_user", et.getText().toString());

        et = (EditText)findViewById(R.id.settings_nickserv_password);
        settings.putString("nickserv_password", et.getText().toString());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AUTOJOIN_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    for (String str : data.getStringArrayListExtra("items")) {
                        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
                    }
                    settings.putStringArrayList("autojoin_channels", data.getStringArrayListExtra("items"));
                }
                break;
            case CONNECT_COMMANDS_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    settings.putStringArrayList("command_on_connect", data.getStringArrayListExtra("items"));
                }
                break;


        }
    }

}
