package Server.Events;

import JSocket2.Core.Server.ServerSessionManager;
import JSocket2.DI.Inject;
import JSocket2.Protocol.EventHub.EventBase;
import JSocket2.Protocol.Message;

import java.io.IOException;

public class UserTypingEvent extends EventBase {

    @Inject
    public UserTypingEvent() {
    }

    @Override
    public void Invoke(ServerSessionManager serverSessionManager,String receiverId,Object... args) throws IOException {
        Message message = createEventMessage("UserTypingEvent",args);
        serverSessionManager.publishMessage(receiverId,message);
    }
}