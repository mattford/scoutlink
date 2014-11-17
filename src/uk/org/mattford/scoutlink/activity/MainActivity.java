package uk.org.mattford.scoutlink.activity;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.Scoutlink;
import uk.org.mattford.scoutlink.irc.IRCBinder;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Settings;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


public class MainActivity extends Activity implements ServiceConnection {
	
	private Settings settings;
	private IRCBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.settings = new Settings(this);
        
   
        
        setContentView(R.layout.activity_main);
        EditText nick = (EditText)findViewById(R.id.nickname);
        nick.setText(settings.getString("nickname", ""));
        
    }
    
    @Override
    public void onResume() {
        Intent service = new Intent(this, IRCService.class);
        bindService(service, this, 0);
    }
    
    
    public void connectClick(View v) {
    	EditText nick = (EditText)findViewById(R.id.nickname);
    	settings.putString("nickname", nick.getText().toString());
    	
    	Intent intent = new Intent(this, ConversationsActivity.class);
    	intent.setAction(ConversationsActivity.PRE_CONNECT);
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
        	
        	break;
        	
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.binder = (IRCBinder)service;
		if (this.binder.getService().getConnection().isConnected()) {
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
