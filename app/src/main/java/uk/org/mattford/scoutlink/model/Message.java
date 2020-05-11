package uk.org.mattford.scoutlink.model;

import android.text.SpannableString;
import java.util.Date;
import uk.org.mattford.scoutlink.database.entities.LogMessage;
import uk.org.mattford.scoutlink.utils.MircColors;

public class Message {
    private int senderType;
    private int type;
	private String text;
    private String sender;
    private Date timestamp;

    /**
     * TYPE_ERROR - for events such as the user being kicked, killed, connect failing
     * TYPE_MESSAGE - normal messages
     * TYPE_ACTION - actions
     * TYPE_EVENT - mode changes, topic change etc
     * TYPE_SERVER - Server messages e.g., connect messages/MOTD
     */
    public static int TYPE_ERROR = 0;
    public static int TYPE_MESSAGE = 1;
    public static int TYPE_ACTION = 2;
    public static int TYPE_EVENT = 3;
    public static int TYPE_SERVER = 4;
    public static int TYPE_NOTICE = 5;

    public static int SENDER_TYPE_SELF = 0;
    public static int SENDER_TYPE_OTHER = 1;
    public static int SENDER_TYPE_SERVER = 2;

    public Message(LogMessage message) {
        this.text = message.message;
        this.timestamp = message.date;
        this.sender = message.sender;
        if (this.sender == null) {
            this.senderType = SENDER_TYPE_SERVER;
            this.type = TYPE_SERVER;
        } else {
            this.senderType = SENDER_TYPE_OTHER;
            this.type = TYPE_MESSAGE;
        }
    }

    public Message (String text, int senderType, int type) {
        this.text = text;
        this.senderType = senderType;
        this.type = type;
        this.timestamp = new Date();
    }

    public Message (String sender, String text, int senderType, int type) {
        this(text, senderType, type);
        this.sender = sender;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Set the timestamp for this message
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

	public String getText() {
		return text;
	}

	public SpannableString getFormattedText() {
        String text = getText();
        if (text == null) {
            return null;
        }
        return applySpans(getText());
    }

	public void setText(String text) {
		this.text = text;
	}

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    private static SpannableString applySpans(String text) {
        return MircColors.toSpannable(text);
    }

    public boolean isType(int otherType) {
        return type == otherType;
    }

    public boolean isSenderType(int otherType) {
        return senderType == otherType;
    }
}
