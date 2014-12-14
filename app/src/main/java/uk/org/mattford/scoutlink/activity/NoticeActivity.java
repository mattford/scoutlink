package uk.org.mattford.scoutlink.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import uk.org.mattford.scoutlink.R;

public class NoticeActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);
        ((Button)findViewById(R.id.send_button)).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String message = ((EditText)findViewById(R.id.message)).getText().toString();
        Intent intent = new Intent();
        intent.putExtra("target", getIntent().getStringExtra("target"));
        intent.putExtra("message", message);
        setResult(RESULT_OK, intent);
        finish();
    }
}
