package Client.Subscribers;

import Client.Services.UI.ChatUIService;
import JSocket2.DI.Inject;
import JSocket2.Protocol.EventHub.EventSubscriberBase;
import JSocket2.Protocol.EventHub.OnEvent;
import Shared.Events.Models.MessageDeliveredEventModel;

public class MessageDeliveredSubscriber extends EventSubscriberBase {

    private final ChatUIService chatUIService;

    @Inject
    public MessageDeliveredSubscriber(ChatUIService chatUIService) {
        this.chatUIService = chatUIService;
    }
    @OnEvent("MessageDeliveredEvent")
    public void handleMessageDelivered(MessageDeliveredEventModel message) {
        System.out.println("Message delivered event received for message ID: " + message.getMessageId());
        chatUIService.onMessageDelivered(message);
    }
}