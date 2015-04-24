package uk.org.mattford.scoutlink.event;

public class JoinFailedEvent {

    public String channel;
    public String message;

    public JoinFailedEvent(String channel, String message) {
        this.channel = channel;
        this.message = message;
    }

    public String getChannel() {
        return this.channel;
    }

    public String getMessage() {
        return this.message;
    }
}
