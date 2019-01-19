package uk.org.mattford.scoutlink.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import uk.org.mattford.scoutlink.database.converters.Converters;
import uk.org.mattford.scoutlink.database.dao.LogMessageDao;
import uk.org.mattford.scoutlink.database.entities.LogMessage;

@Database(entities = {LogMessage.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class LogDatabase extends RoomDatabase {
    public abstract LogMessageDao logMessageDao();
}
