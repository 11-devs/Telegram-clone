package Client.Subscribers;

import Client.Services.UI.ChatUIService;
import JSocket2.DI.Inject;
import JSocket2.Protocol.EventHub.EventSubscriberBase;
import JSocket2.Protocol.EventHub.OnEvent;
import Shared.Events.Models.UserStatusChangedEventModel;

public class UserStatusChangedSubscriber extends EventSubscriberBase {

    private final ChatUIService chatUIService;

    @Inject
    public UserStatusChangedSubscriber(ChatUIService chatUIService) {
        this.chatUIService = chatUIService;
    }

    @OnEvent("UserStatusChangedEvent")
    public void handleUserStatusChanged(UserStatusChangedEventModel model) {
        System.out.println("User status changed event received for user ID: " + model.getUserId() + ", isOnline: " + model.isOnline());
        chatUIService.onUserStatusChanged(model);
    }
}