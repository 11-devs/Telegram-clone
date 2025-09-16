package JSocket2.Protocol.EventHub;

public class EventHandlingException extends RuntimeException {
    public EventHandlingException(String s, Exception e) {
        super(s,e);
    }
}
