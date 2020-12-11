package uk.org.mattford.scoutlink.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.database.SettingsDatabase;
import uk.org.mattford.scoutlink.database.entities.Alias;
import uk.org.mattford.scoutlink.databinding.ActivityEditAliasBinding;

public class EditAliasActivity extends AppCompatActivity {
    Alias alias;
    ActivityEditAliasBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditAliasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(null);

        binding.saveButton.setOnClickListener(view -> onSaveButtonClick());

        Intent intent = getIntent();
        if (intent.hasExtra("commandName")) {
            loadAlias(intent.getStringExtra("commandName"));
        }
    }

    private void loadAlias(String commandName) {
        LiveData<Alias> aliasLiveData = SettingsDatabase.getInstance(getApplicationContext()).aliasesDao().getAlias(commandName);
        aliasLiveData.observe(this, new Observer<Alias>() {
            @Override
            public void onChanged(Alias dbAlias) {
                alias = dbAlias;
                binding.commandName.setText(alias.commandName);
                binding.commandText.setText(alias.commandText);
                aliasLiveData.removeObserver(this);
            }
        });
    }

    public void onSaveButtonClick() {
        SettingsDatabase db = SettingsDatabase.getInstance(getApplicationContext());
        String newCommandName = binding.commandName.getText().toString();
        String newCommandText = binding.commandText.getText().toString();

        new Thread(() -> {
            if (alias != null && alias.commandName.equalsIgnoreCase(newCommandName)) {
                alias.commandText = newCommandText;
                db.aliasesDao().updateAlias(alias);
            } else {
                // Check if exists
                Alias existingAlias = db.aliasesDao().getAliasSync(newCommandName);
                if (existingAlias != null) {
                    runOnUiThread(() -> Toast.makeText(this, getString(R.string.alias_already_exists), Toast.LENGTH_LONG).show());
                    return;
                }
                db.aliasesDao().insertAlias(new Alias(newCommandName, newCommandText));
            }
            runOnUiThread(this::finish);
        }).start();
    }
}
