package Server.Events;

import JSocket2.Core.Server.ServerSessionManager;
import JSocket2.DI.Inject;
import JSocket2.Protocol.EventHub.EventBase;
import JSocket2.Protocol.Message;
import Shared.Events.Models.MessageDeliveredEventModel;

import java.io.IOException;

public class MessageDeliveredEvent extends EventBase {

    @Inject
    public MessageDeliveredEvent() {
    }

    @Override
    public void Invoke(ServerSessionManager serverSessionManager, String receiverId, Object... args) throws IOException {
        Message message = createEventMessage("MessageDeliveredEvent", args);
        serverSessionManager.publishMessage(receiverId, message);
    }
}