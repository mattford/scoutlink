package uk.org.mattford.scoutlink.database.migrations;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class LogDatabaseMigrations {

    public static final Migration MIGRATION_0_1 = new Migration(0, 1) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE `log_messages` (" +
                            "`rowid` INTEGER, " +
                            "`message_date` INTEGER, " +
                            "`conversation_name` TEXT, " +
                            "`conversation_type` TEXT, " +
                            "`sender` TEXT, " +
                            "`message` TEXT" +
                            ");"
            );
        }
    };
}
