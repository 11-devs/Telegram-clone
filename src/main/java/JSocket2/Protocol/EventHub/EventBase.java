package JSocket2.Protocol.EventHub;

import JSocket2.Example.UserIsTypingEventModel;
import JSocket2.Protocol.Message;
import JSocket2.Protocol.MessageHeader;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public abstract class EventBase<T> {
    public abstract void Invoke(String receiverId,Object... args) throws IOException;
    protected Gson gson = new Gson();
    protected Message createEventMessage(String eventName, Object[] payloadObject) {
        EventMetadata metadata = new EventMetadata(eventName);
        String metadataJson = gson.toJson(metadata);
        String payloadJson  = gson.toJson(payloadObject);
        byte[] metadataBytes = metadataJson.getBytes(StandardCharsets.UTF_8);
        byte[] payloadBytes  = payloadJson.getBytes(StandardCharsets.UTF_8);
        MessageHeader header = MessageHeader.BuildEventHeader(UUID.randomUUID(),metadataBytes.length, payloadBytes.length);
        return new Message(header, metadataBytes, payloadBytes);
    }
}
