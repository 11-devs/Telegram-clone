package Client.Subscribers;

import Client.Services.UI.ChatUIService;
import JSocket2.DI.Inject;
import JSocket2.Protocol.EventHub.EventSubscriberBase;
import JSocket2.Protocol.EventHub.OnEvent;
import Shared.Events.Models.MessageReadEventModel;

public class MessageReadSubscriber extends EventSubscriberBase {

    private final ChatUIService chatUIService;

    @Inject
    public MessageReadSubscriber(ChatUIService chatUIService) {
        this.chatUIService = chatUIService;
    }

    @OnEvent("MessageReadEvent")
    public void handleMessageRead(MessageReadEventModel message) {
        System.out.println("Message read event received for message ID: " + message.getMessageId());
        chatUIService.onMessageRead(message);
    }
}