package Client.Subscribers;

import Client.Services.UI.ChatUIService;
import JSocket2.DI.Inject;
import JSocket2.Protocol.EventHub.EventSubscriberBase;
import JSocket2.Protocol.EventHub.OnEvent;
import Shared.Events.Models.ChatInfoChangedEventModel;

public class ChatInfoChangedSubscriber extends EventSubscriberBase {

    private final ChatUIService chatUIService;

    @Inject
    public ChatInfoChangedSubscriber(ChatUIService chatUIService) {
        this.chatUIService = chatUIService;
    }

    @OnEvent("ChatInfoChangedEvent")
    public void handleChatInfoChanged(ChatInfoChangedEventModel model) {
        System.out.println("Chat info changed event received for chat ID: " + model.getChatId());
        chatUIService.onChatInfoChanged(model);
    }
}