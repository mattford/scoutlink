package uk.org.mattford.scoutlink.database.migrations;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public final class SettingsDatabaseMigrations {
    public static final Migration MIGRATION_0_1 = new Migration(0, 1) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE `aliases` (" +
                            "`rowid` INTEGER, " +
                            "`command_name` TEXT, " +
                            "`command_string` TEXT" +
                            ");"
            );
        }
    };
}
