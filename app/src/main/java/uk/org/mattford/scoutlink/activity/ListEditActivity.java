package uk.org.mattford.scoutlink.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.databinding.ActivityListViewEditBinding;

public class ListEditActivity extends ListActivity {
    private ActivityListViewEditBinding binding;
    private ArrayList<String>items;
    private ArrayAdapter<String> adapter;
    private final ArrayList<String> newItems = new ArrayList<>();
    private final ArrayList<String> removedItems = new ArrayList<>();

    protected String title = "List Edit";
    protected String firstChar = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListViewEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent callingIntent = getIntent();
        items = callingIntent.getStringArrayListExtra("items");
        adapter = new ArrayAdapter<>(this, R.layout.list_view_edit_item, items);
        setListAdapter(adapter);

        if (callingIntent.getStringExtra("firstChar") != null) {
            firstChar = callingIntent.getStringExtra("firstChar");
            TextView tv = binding.firstChar;
            tv.setText(firstChar);
            tv.setVisibility(View.VISIBLE);
        }
        if (callingIntent.getStringExtra("title") != null) {
            title = callingIntent.getStringExtra("title");
        }
        setTitle(title);
    }

    public void onFinishClick(View v) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra("items", items);
        intent.putStringArrayListExtra("newItems", newItems);
        intent.putStringArrayListExtra("removedItems", removedItems);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onNewItemButtonClick(View v) {
        EditText et = binding.newItem;
        String newItem = firstChar + et.getText().toString();
        items.add(newItem);
        newItems.add(newItem);
        adapter.notifyDataSetChanged();
        et.setText("");
    }

    public void onItemClick(View v) {
        final String s = ((TextView)v).getText().toString();
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(getString(R.string.edit_list_remove_item_confirm_title));
        adb.setMessage(getString(R.string.edit_list_remove_item_confirm_text));
        adb.setPositiveButton("Yes", (dialog, which) -> {
            items.remove(s);
            adapter.notifyDataSetChanged();
            onItemRemoved(s);
        });
        adb.setNegativeButton("No", (dialog, which) -> {
            // Do nothing.
        });
        adb.show();
    }

    public void onItemRemoved(String item) {
        removedItems.add(item);
    }
}
