package uk.org.mattford.scoutlink.activity;

import uk.org.mattford.scoutlink.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class JoinActivity extends Activity implements OnClickListener {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_join);
		findViewById(R.id.join_button).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		String channel = ((EditText)findViewById(R.id.channel)).getText().toString();
		Intent intent = new Intent();
		intent.putExtra("target", channel);
		setResult(RESULT_OK, intent);
		finish();
	}
}
