package uk.org.mattford.scoutlink.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.model.Settings;

public class SettingsActivity extends Activity {

    private Settings settings;

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

}
