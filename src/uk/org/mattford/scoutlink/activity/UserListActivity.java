package uk.org.mattford.scoutlink.activity;

import java.util.ArrayList;

import uk.org.mattford.scoutlink.R;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;


public class UserListActivity extends ListActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userlist);
		ArrayList<String> users = getIntent().getStringArrayListExtra("users");
		this.setListAdapter(new ArrayAdapter<String>(this, R.layout.user_list_item, users));
	}
	
}
