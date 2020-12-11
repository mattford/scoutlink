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
                            "`command_name` TEXT, " +
                            "`command_string` TEXT" +
                            ");"
            );
            database.execSQL("INSERT INTO `aliases` (`command_name`, `command_string`) VALUES ('example', '/say This is an example of an alias created in the SL Android App.\nThis is the first parameter passed to the alias: $1\nand this is the text passed afterwards: $2-')");
        }
    };
}
