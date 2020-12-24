package uk.org.mattford.scoutlink.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "aliases")
public class Alias {

    public Alias(@NonNull String commandName, String commandText) {
        this.commandName = commandName;
        this.commandText = commandText;
    }

    @PrimaryKey()
    @NonNull
    @ColumnInfo(name = "command_name")
    public String commandName;

    @ColumnInfo(name = "command_text")
    public String commandText;
}
