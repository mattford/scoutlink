package uk.org.mattford.scoutlink.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import uk.org.mattford.scoutlink.R;

public class ListEditActivity extends ListActivity {

    private ArrayList<String>items;
    private ArrayAdapter<String> adapter;

    private String title = "List Edit";
    private String firstChar = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_edit);

        Intent callingIntent = getIntent();
        items = callingIntent.getStringArrayListExtra("items");
        if (items.size() < 2 && items.get(0).equals("")) {
            items = new ArrayList<String>();
        }
        adapter = new ArrayAdapter<String>(this, R.layout.list_view_edit_item, items);
        setListAdapter(adapter);

        if (callingIntent.getStringExtra("firstChar") != null) {
            firstChar = callingIntent.getStringExtra("firstChar");
        }
        if (callingIntent.getStringExtra("title") != null) {
            title = callingIntent.getStringExtra("title");
        }
        setTitle(title);
        EditText et = (EditText)findViewById(R.id.new_item);
        et.setText(firstChar);
        et.setSelection(et.getText().length());
    }

    public void onPause() {
        super.onPause();
        Intent intent = new Intent();
        intent.putStringArrayListExtra("items", items);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onFinishClick(View v) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra("items", items);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onNewItemButtonClick(View v) {
        EditText et = (EditText)v.getRootView().findViewById(R.id.new_item);
        String newItem = et.getText().toString();
        items.add(newItem);
        adapter.notifyDataSetChanged();
        et.setText(firstChar);
        et.setSelection(et.getText().length());
    }

    public void onItemClick(View v) {
        final String s = ((TextView)v).getText().toString();
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(getString(R.string.edit_list_remove_item_confirm_title));
        adb.setMessage(getString(R.string.edit_list_remove_item_confirm_text));
        adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                items.remove(s);
                adapter.notifyDataSetChanged();
            }
        });
        adb.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing.
            }
        });
        adb.show();

    }

}
