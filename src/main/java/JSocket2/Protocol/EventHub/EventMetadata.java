package JSocket2.Protocol.EventHub;

public class EventMetadata {
    private String eventName;
    public EventMetadata(String eventName){
        this.eventName = eventName;
    };

    public String getEventName() {
        return eventName;
    }
}
