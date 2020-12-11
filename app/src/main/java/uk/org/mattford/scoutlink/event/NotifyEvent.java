package uk.org.mattford.scoutlink.event;

/**
 * Event for handling notify lines from the server
 * Created by Matt Ford on 03/04/2015.
 */
public class NotifyEvent {

    private final String nick;
    private final int type;
    private final boolean isAway;
    private final boolean isOnline;
    private final boolean isAdded;
    private final String message;

    public static final int TYPE_MANAGELIST = 1;
    public static final int TYPE_AWAY = 2;
    public static final int TYPE_ONLINE = 3;

    public NotifyEvent(String nick, int type, boolean isAway, boolean isOnline, boolean isAdded, String message) {
        this.nick = nick;
        this.type = type;
        this.isAway = isAway;
        this.isOnline = isOnline;
        this.isAdded = isAdded;
        this.message = message;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public String getNick() {
        return nick;
    }

    public int getType() {
        return type;
    }

    public boolean isAway() {
        return isAway;
    }

    public String getMessage() {
        return message;
    }

    public boolean isAdded() {
        return isAdded;
    }


}
