package uk.org.mattford.scoutlink.event;

public class MessageNotSentEvent {

    public String channel;
    public String message;

    public MessageNotSentEvent(String channel, String message) {
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
