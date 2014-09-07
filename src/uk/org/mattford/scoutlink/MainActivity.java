package uk.org.mattford.scoutlink;

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
        
    	Intent serviceIntent = new Intent(this, IRCService.class);
    	startService(serviceIntent);
    	bindService(serviceIntent, this, 0);
    }
    
    public void connectClick(View v) {
    	EditText nick = (EditText)findViewById(R.id.nickname);
    	settings.putString("nickname", nick.getText().toString());
    	
    	
    	this.binder.getService().connect();
    	Intent intent = new Intent(this, ConversationsActivity.class);
    	startActivity(intent);
    }
    
   public void onServiceConnected(ComponentName name, IBinder service)
   {
       this.binder = (IRCBinder) service;
   }

   public void onServiceDisconnected(ComponentName name)
   {
       this.binder = null;
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
