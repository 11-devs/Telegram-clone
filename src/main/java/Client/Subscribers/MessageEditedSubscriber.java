package Client.Subscribers;

import Client.Services.UI.ChatUIService;
import JSocket2.DI.Inject;
import JSocket2.Protocol.EventHub.EventSubscriberBase;
import JSocket2.Protocol.EventHub.OnEvent;
import Shared.Events.Models.MessageEditedEventModel;

public class MessageEditedSubscriber extends EventSubscriberBase {

    private final ChatUIService chatUIService;

    @Inject
    public MessageEditedSubscriber(ChatUIService chatUIService) {
        this.chatUIService = chatUIService;
    }

    @OnEvent("MessageEditedEvent")
    public void handleMessageEdited(MessageEditedEventModel message) {
        System.out.println("Message edited event received for message ID: " + message.getMessageId());
        chatUIService.onMessageEdited(message);
    }
}