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

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                "CREATE TABLE `log_messages_new` (" +
                    "`rowid` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "`message_date` INTEGER, " +
                    "`conversation_name` TEXT, " +
                    "`conversation_type` INTEGER, " +
                    "`sender` TEXT, " +
                    "`sender_type` INTEGER, " +
                    "`message_type` INTEGER, " +
                    "`message` TEXT" +
                    ");"
            );
            database.execSQL("INSERT INTO `log_messages_new` (" +
                                 "`rowid`," +
                                 "`message_date`," +
                                 "`conversation_name`," +
                                 "`conversation_type`," +
                                 "`sender`," +
                                 "`sender_type`," +
                                 "`message_type`," +
                                 "`message`" +
                                 ") " +
                                 "    SELECT " +
                                 "        `rowid`," +
                                 "        `message_date`," +
                                 "        `conversation_name`," +
                                 "        `conversation_type`," +
                                 "        `sender`," +
                                 "        2 AS `sender_type`," +
                                 "        CASE `sender` WHEN NULL THEN 4 ELSE 1 END AS `message_type`," +
                                 "        `message` " +
                                 "    FROM `log_messages`;");
            database.execSQL("DROP TABLE `log_messages`;");
            database.execSQL("ALTER TABLE `log_messages_new` RENAME TO `log_messages`;");
        }
    };
}
