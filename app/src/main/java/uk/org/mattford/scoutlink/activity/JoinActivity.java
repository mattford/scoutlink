package uk.org.mattford.scoutlink.activity;

import uk.org.mattford.scoutlink.databinding.ActivityJoinBinding;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class JoinActivity extends Activity implements OnClickListener {
	private ActivityJoinBinding binding;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityJoinBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		binding.joinButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		String channel = "#" + binding.channel.getText().toString();
		Intent intent = new Intent();
		intent.putExtra("target", channel);
		setResult(RESULT_OK, intent);
		finish();
	}
}
