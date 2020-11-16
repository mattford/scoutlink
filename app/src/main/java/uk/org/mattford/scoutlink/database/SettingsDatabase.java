package uk.org.mattford.scoutlink.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import uk.org.mattford.scoutlink.database.converters.Converters;
import uk.org.mattford.scoutlink.database.dao.AliasesDao;
import uk.org.mattford.scoutlink.database.entities.Alias;
import uk.org.mattford.scoutlink.database.migrations.SettingsDatabaseMigrations;

@Database(entities = {Alias.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class SettingsDatabase extends RoomDatabase {
    public abstract AliasesDao aliasesDao();
    private static SettingsDatabase instance;
    public static SettingsDatabase getInstance() {
        return instance;
    }

    public static SettingsDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, SettingsDatabase.class, "settings")
                    .addMigrations(SettingsDatabaseMigrations.MIGRATION_0_1)
                    .build();
        }
        return instance;
    }
}