package uk.org.mattford.scoutlink.activity;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.ScoutlinkApplication;
import uk.org.mattford.scoutlink.databinding.ActivityMainBinding;
import uk.org.mattford.scoutlink.model.Server;
import uk.org.mattford.scoutlink.model.Settings;
import uk.org.mattford.scoutlink.utils.Validator;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
	private ActivityMainBinding binding;
	private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        ((ScoutlinkApplication) getApplication()).getTracker(ScoutlinkApplication.TrackerName.APP_TRACKER);
        this.settings = new Settings(this);

        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (NullPointerException e) {
            // Not a big deal, so let's just get on with our lives
        }
        setContentView(binding.getRoot());
    }

    @Override
    public void onResume() {
    	super.onResume();

        /*
         * If we are already connected, send the user to ConversationsActivity
         */
    	if (Server.getInstance().isConnected()) {
            Intent intent = new Intent(this, ConversationsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        EditText nick = binding.nickname;
        nick.setText(settings.getString("nickname", ""));
        nick.setOnEditorActionListener((v, actionId, event) -> {
            if (event == null) {
                connectClick(v);
                return true;
            }
            return false;
        });
        CheckBox showChannelList = binding.channelListOnConnect;
        showChannelList.setChecked(settings.getBoolean("channel_list_on_connect", true));
    }

    public void connectClick(View v) {
    	String nick = binding.nickname.getText().toString();
        boolean channelListOnConnect = binding.channelListOnConnect.isChecked();
    	if ("".equals(nick) || !Validator.isValidNickname(nick)) {
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
            case R.id.action_logs:
                intent = new Intent(this, LogListActivity.class);
                startActivity(intent);
                break;
        	
        }
        return super.onOptionsItemSelected(item);
    }
}
