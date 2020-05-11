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

@Database(entities = {LogMessage.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class LogDatabase extends RoomDatabase {
    public abstract LogMessageDao logMessageDao();

    public static LogDatabase getInstance(Context context) {
        return Room.databaseBuilder(context, LogDatabase.class, "logs")
                .addMigrations(LogDatabaseMigrations.MIGRATION_0_1)
                .build();
    }
}
