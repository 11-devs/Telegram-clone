package Server.Events;

import JSocket2.Core.Server.ServerSessionManager;
import JSocket2.DI.Inject;
import JSocket2.Protocol.EventHub.EventBase;
import JSocket2.Protocol.Message;
import Shared.Events.Models.NewMessageEventModel;

import java.io.IOException;

public class NewMessageEvent extends EventBase {

    @Inject
    public NewMessageEvent() {
    }

    @Override
    public void Invoke(ServerSessionManager serverSessionManager,String receiverId, Object... args) throws IOException {
        Message message = createEventMessage("NewMessageEvent", args);
        serverSessionManager.publishMessage(receiverId, message);
    }
}