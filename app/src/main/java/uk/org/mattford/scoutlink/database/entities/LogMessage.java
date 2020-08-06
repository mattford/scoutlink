package uk.org.mattford.scoutlink.database.entities;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;

@Entity(tableName = "log_messages")
public class LogMessage {

    public LogMessage() {}

    public LogMessage(Conversation conversation, Message message) {
        this.date = message.getTimestamp();
        this.conversationName = conversation.getName();
        this.conversationType = conversation.getType();
        this.sender = message.getSender();
        this.message = message.getText();
        this.senderType = message.getSenderType();
        this.messageType = message.getType();
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

    @ColumnInfo(name="sender_type")
    public Integer senderType;

    @ColumnInfo(name="message_type")
    public Integer messageType;

    @ColumnInfo(name = "sender")
    public String sender;

    @ColumnInfo(name = "message")
    public String message;

    public Message toMessage() {
        return new Message(this);
    }
}
