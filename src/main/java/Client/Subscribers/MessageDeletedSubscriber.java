package Client.Subscribers;

import Client.Services.UI.ChatUIService;
import JSocket2.DI.Inject;
import JSocket2.Protocol.EventHub.EventSubscriberBase;
import JSocket2.Protocol.EventHub.OnEvent;
import Shared.Events.Models.MessageDeletedEventModel;

public class MessageDeletedSubscriber extends EventSubscriberBase {

    private final ChatUIService chatUIService;

    @Inject
    public MessageDeletedSubscriber(ChatUIService chatUIService) {
        this.chatUIService = chatUIService;
    }

    @OnEvent("MessageDeletedEvent")
    public void handleMessageDeleted(MessageDeletedEventModel message) {
        System.out.println("Message deleted event received for message ID: " + message.getMessageId());
        chatUIService.onMessageDeleted(message);
    }
}