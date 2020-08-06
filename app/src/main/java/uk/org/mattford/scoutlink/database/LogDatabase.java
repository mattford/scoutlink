package uk.org.mattford.scoutlink.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import uk.org.mattford.scoutlink.database.converters.Converters;
import uk.org.mattford.scoutlink.database.dao.LogMessageDao;
import uk.org.mattford.scoutlink.database.entities.LogMessage;
import uk.org.mattford.scoutlink.database.migrations.LogDatabaseMigrations;

@Database(entities = {LogMessage.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class LogDatabase extends RoomDatabase {
    public abstract LogMessageDao logMessageDao();
    private static LogDatabase instance;
    public static LogDatabase getInstance() {
        return instance;
    }

    public static LogDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, LogDatabase.class, "logs")
               .addMigrations(LogDatabaseMigrations.MIGRATION_0_1, LogDatabaseMigrations.MIGRATION_1_2)
               .build();
        }
        return instance;
    }
}
