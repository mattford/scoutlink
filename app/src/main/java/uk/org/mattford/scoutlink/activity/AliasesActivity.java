package uk.org.mattford.scoutlink.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.adapter.AliasesAdapter;
import uk.org.mattford.scoutlink.database.SettingsDatabase;
import uk.org.mattford.scoutlink.databinding.ActivityAliasesBinding;

public class AliasesActivity extends AppCompatActivity implements AliasesAdapter.OnAliasClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAliasesBinding binding = ActivityAliasesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AliasesAdapter adapter = new AliasesAdapter(this);
        binding.aliasList.setAdapter(adapter);

        SettingsDatabase.getInstance(getApplicationContext()).aliasesDao().getAliases().observe(this, adapter::setAliases);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.aliases, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_new) {
            Intent intent = new Intent(this, EditAliasActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAliasClick(String commandName) {
        Intent intent = new Intent(this, EditAliasActivity.class);
        intent.putExtra("commandName", commandName);
        startActivity(intent);
    }
}
