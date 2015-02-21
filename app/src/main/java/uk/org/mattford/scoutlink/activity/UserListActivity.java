package uk.org.mattford.scoutlink.activity;

import java.util.ArrayList;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.model.User;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;


public class UserListActivity extends ListActivity {

    private boolean isChanOp;
    private boolean isIrcOp;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userlist);
		ArrayList<String> users = getIntent().getStringArrayListExtra("users");
        this.isChanOp = getIntent().getBooleanExtra("isChanOp", false);
        this.isIrcOp = getIntent().getBooleanExtra("isIrcOp", false);
		setListAdapter(new ArrayAdapter<>(this, R.layout.user_list_item, users));
        registerForContextMenu(getListView());
	}

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.userlist_context_menu, menu);
        if (isChanOp) {
            inflater.inflate(R.menu.userlist_context_menu_chanop, menu);
        }
        if (isIrcOp) {
            inflater.inflate(R.menu.userlist_context_menu_ircop, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String nick = getListAdapter().getItem(info.position).toString();
        Intent intent = new Intent();
        intent.putExtra("target", nick);
        switch (item.getItemId()) {
            case R.id.action_userlist_query:
                intent.putExtra("action", User.ACTION_QUERY);
                break;
            case R.id.action_userlist_notice:
                intent.putExtra("action", User.ACTION_NOTICE);
                break;
            case R.id.action_userlist_kick:
                intent.putExtra("action", User.ACTION_KICK);
                break;
            case R.id.action_userlist_kill:
                intent.putExtra("action", User.ACTION_KILL);
                break;
            case R.id.action_userlist_op:
                intent.putExtra("action", User.ACTION_OP);
                break;
            case R.id.action_userlist_deop:
                intent.putExtra("action", User.ACTION_DEOP);
                break;
            case R.id.action_userlist_hop:
                intent.putExtra("action", User.ACTION_HOP);
                break;
            case R.id.action_userlist_dehop:
                intent.putExtra("action", User.ACTION_DEHOP);
                break;
            case R.id.action_userlist_owner:
                intent.putExtra("action", User.ACTION_OWNER);
                break;
            case R.id.action_userlist_deowner:
                intent.putExtra("action", User.ACTION_DEOWNER);
                break;
            case R.id.action_userlist_admin:
                intent.putExtra("action", User.ACTION_ADMIN);
                break;
            case R.id.action_userlist_deadmin:
                intent.putExtra("action", User.ACTION_DEADMIN);
                break;
            case R.id.action_userlist_voice:
                intent.putExtra("action", User.ACTION_VOICE);
                break;
            case R.id.action_userlist_devoice:
                intent.putExtra("action", User.ACTION_DEVOICE);
                break;


        }
        setResult(RESULT_OK, intent);
        finish();
        return false;
    }
	
}
