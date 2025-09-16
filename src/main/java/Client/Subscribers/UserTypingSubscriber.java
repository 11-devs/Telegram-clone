package Client.Subscribers;

import Client.Services.UI.ChatUIService;
import JSocket2.DI.Inject;
import JSocket2.Protocol.EventHub.EventSubscriberBase;
import JSocket2.Protocol.EventHub.OnEvent;
import Shared.Events.Models.UserIsTypingEventModel;

public class UserTypingSubscriber extends EventSubscriberBase {

    private final ChatUIService chatUIService;

    @Inject
    public UserTypingSubscriber(ChatUIService chatUIService) {
        this.chatUIService = chatUIService;
    }

    @OnEvent("UserTypingEvent")
    public void handleUserTyping(UserIsTypingEventModel eventModel) {
        System.out.println("User typing event received: " + eventModel.getSenderName() + " is typing: " + eventModel.isTyping());
        chatUIService.onUserTyping(eventModel);
    }
}