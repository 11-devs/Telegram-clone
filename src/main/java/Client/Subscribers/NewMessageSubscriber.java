package Client.Subscribers;

import Client.Services.UI.ChatUIService;
import JSocket2.DI.Inject;
import JSocket2.Protocol.EventHub.EventSubscriberBase;
import JSocket2.Protocol.EventHub.OnEvent;
import Shared.Events.Models.NewMessageEventModel;

public class NewMessageSubscriber extends EventSubscriberBase {

    private final ChatUIService chatUIService;

    @Inject
    public NewMessageSubscriber(ChatUIService chatUIService) {
        this.chatUIService = chatUIService;
    }

    @OnEvent("NewMessageEvent")
    public void handleNewMessage(NewMessageEventModel message) {
        System.out.println("New message event received from sender: " + message.getSenderName());
        chatUIService.onNewMessage(message);
    }
}