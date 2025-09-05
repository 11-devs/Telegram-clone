package Server.Events;

import JSocket2.Core.Server.ServerSessionManager;
import JSocket2.DI.Inject;
import JSocket2.Protocol.EventHub.EventBase;
import JSocket2.Protocol.Message;
import Shared.Events.Models.MessageEditedEventModel;

import java.io.IOException;

public class MessageEditedEvent extends EventBase {

    @Inject
    public MessageEditedEvent() {
    }

    @Override
    public void Invoke(ServerSessionManager serverSessionManager, String receiverId, Object... args) throws IOException {
        Message message = createEventMessage("MessageEditedEvent", args);
        serverSessionManager.publishMessage(receiverId, message);
    }
}