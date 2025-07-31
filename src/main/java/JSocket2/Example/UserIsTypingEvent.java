package JSocket2.Example;

import JSocket2.Core.Server.ServerSessionManager;
import JSocket2.Protocol.EventHub.EventBase;
import JSocket2.Protocol.EventHub.EventMetadata;
import JSocket2.Protocol.EventHub.EventPublisher;
import JSocket2.Protocol.Message;
import JSocket2.Protocol.MessageHeader;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UserIsTypingEvent extends EventBase<UserIsTypingEventModel> {

    ServerSessionManager serverSessionManager;
    public UserIsTypingEvent(ServerSessionManager serverSessionManager){
        this.serverSessionManager = serverSessionManager;
    }

    @Override
    public void Invoke(String receiverId,UserIsTypingEventModel model) throws IOException {
        Message message = createEventMessage("UserIsTypingEvent",model);
        serverSessionManager.publishMessage(receiverId,message);
    }
}
