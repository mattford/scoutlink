package uk.org.mattford.scoutlink.activity;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.ScoutlinkApplication;
import uk.org.mattford.scoutlink.irc.IRCBinder;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Settings;
import uk.org.mattford.scoutlink.utils.Validator;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ServiceConnection {
	
	private Settings settings;
	private IRCBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((ScoutlinkApplication) getApplication()).getTracker(ScoutlinkApplication.TrackerName.APP_TRACKER);
        this.settings = new Settings(this);

        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (NullPointerException e) {
            // Not a big deal, so let's just get on with our lives
        }
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onResume() {
    	super.onResume();

        EditText nick = findViewById(R.id.nickname);
        nick.setText(settings.getString("nickname", ""));
        nick.setOnEditorActionListener((v, actionId, event) -> {
            if (event == null) {
                connectClick(v);
                return true;
            }
            return false;
        });
        CheckBox showChannelList = findViewById(R.id.channel_list_on_connect);
        showChannelList.setChecked(settings.getBoolean("channel_list_on_connect", true));
        Intent service = new Intent(this, IRCService.class);
        startService(service);
        bindService(service, this, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        unbindService(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void connectClick(View v) {
    	String nick = ((EditText)findViewById(R.id.nickname)).getText().toString();
        boolean channelListOnConnect = ((CheckBox)findViewById(R.id.channel_list_on_connect)).isChecked();
    	if (nick == null || !Validator.isValidNickname(nick)) {
            Toast.makeText(this, getString(R.string.nickname_not_valid), Toast.LENGTH_LONG).show();
            return;
        }
        settings.putString("nickname", nick);
    	settings.putBoolean("channel_list_on_connect", channelListOnConnect);

    	Intent intent = new Intent(this, ConversationsActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(intent);
    	finish();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        switch(id) {
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_rules:
                intent = new Intent(this, RulesActivity.class);
                startActivity(intent);
                break;
        	
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		binder = (IRCBinder)service;
		if (binder.getService().getConnection() != null && binder.getService().getConnection().isConnected()) {
			Intent intent = new Intent(this, ConversationsActivity.class);
			startActivity(intent);
			finish();
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		this.binder = null;	
	}

}
