package uk.org.mattford.scoutlink.database.entities;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;
import androidx.room.PrimaryKey;
import uk.org.mattford.scoutlink.model.Message;

@Fts4
@Entity(tableName = "log_messages")
public class LogMessage {

    public LogMessage() {}

    public LogMessage(
        String conversationName,
        int conversationType,
        String sender,
        String message
    ) {
        this.date = new Date();
        this.conversationName = conversationName;
        this.conversationType = conversationType;
        this.sender = sender;
        this.message = message;
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    public Integer id;

    @ColumnInfo(name = "message_date")
    public Date date;

    @ColumnInfo(name = "conversation_name")
    public String conversationName;

    @ColumnInfo(name= "conversation_type")
    public Integer conversationType;

    @ColumnInfo(name = "sender")
    public String sender;

    @ColumnInfo(name = "message")
    public String message;

    public Message toMessage() {
        return new Message(this);
    }
}
