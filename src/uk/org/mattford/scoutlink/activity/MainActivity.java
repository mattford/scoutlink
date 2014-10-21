package uk.org.mattford.scoutlink.activity;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.Scoutlink;
import uk.org.mattford.scoutlink.model.Settings;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


public class MainActivity extends Activity {
	
	private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.settings = new Settings(this);
        
        setContentView(R.layout.activity_main);
        EditText nick = (EditText)findViewById(R.id.nickname);
        nick.setText(settings.getString("nickname", ""));
        
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

}
