package uk.org.mattford.scoutlink.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import uk.org.mattford.scoutlink.R;

public class ChannelListActivity extends ListActivity implements AdapterView.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_list);
        ArrayList<String> channels = getIntent().getStringArrayListExtra("channels");
        setListAdapter(new ArrayAdapter<>(this, R.layout.channel_list_item, channels));
        getListView().setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView tv = (TextView)view;
        String channel = tv.getText().toString();
        Intent intent = new Intent();
        intent.putExtra("target", channel);
        setResult(RESULT_OK, intent);
        finish();
    }


}
