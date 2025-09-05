package JSocket2.Example;

import JSocket2.Core.Server.ServerSessionManager;
import JSocket2.Protocol.EventHub.EventBase;
import JSocket2.Protocol.Message;

import java.io.IOException;

public class UserIsTypingEvent extends EventBase {

    ServerSessionManager serverSessionManager;
    public UserIsTypingEvent(ServerSessionManager serverSessionManager){
        this.serverSessionManager = serverSessionManager;
    }

    @Override
    public void Invoke(ServerSessionManager serverSessionManager,String receiverId,Object... args) throws IOException {
        Message message = createEventMessage("UserIsTypingEvent",args);
        serverSessionManager.publishMessage(receiverId,message);
    }
}
