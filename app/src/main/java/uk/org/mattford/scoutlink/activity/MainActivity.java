package uk.org.mattford.scoutlink.activity;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.ScoutlinkApplication;
import uk.org.mattford.scoutlink.irc.IRCBinder;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Settings;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;

public class MainActivity extends ActionBarActivity implements ServiceConnection {
	
	private Settings settings;
	private IRCBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((ScoutlinkApplication) getApplication()).getTracker(ScoutlinkApplication.TrackerName.APP_TRACKER);
        this.settings = new Settings(this);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onResume() {
    	super.onResume();

        EditText nick = (EditText)findViewById(R.id.nickname);
        nick.setText(settings.getString("nickname", ""));
        nick.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event == null) {
                    connectClick(v);
                    return true;
                }
                return false;
            }
        });
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

    	if (nick == null || !nick.matches("\\A[a-z_\\-\\[\\]\\\\^{}|`][a-z0-9_\\-\\[\\]\\\\^{}|`]*\\z")) {
            Toast.makeText(this, getString(R.string.nickname_not_valid), Toast.LENGTH_LONG).show();
            return;
        }
        settings.putString("nickname", nick);
    	
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
        switch(id) {
        case R.id.action_settings:
        	Intent intent = new Intent(this, SettingsActivity.class);
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
