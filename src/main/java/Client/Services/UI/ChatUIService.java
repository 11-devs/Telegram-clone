package Client.Services.UI;

import Client.Controllers.MainChatController;
import Shared.Events.Models.*;
import javafx.application.Platform;

import java.util.function.Consumer;

public class ChatUIService {
    private Consumer<NewMessageEventModel> newMessageEventHandler;
    private Consumer<MessageEditedEventModel> messageEditedEventHandler;
    private Consumer<MessageDeletedEventModel> messageDeletedEventHandler;
    private Consumer<MessageReadEventModel> messageReadEventHandler;
    private Consumer<UserIsTypingEventModel> userTypingEventHandler;
    private Consumer<UserStatusChangedEventModel> userStatusChangedEventHandler;
    private Consumer<ChatInfoChangedEventModel> chatInfoChangedEventHandler;

    public void setActiveChatController(MainChatController controller) {
        if (controller != null) {
            this.newMessageEventHandler = controller::handleIncomingMessage;
            this.messageEditedEventHandler = controller::handleMessageEdited;
            this.messageDeletedEventHandler = controller::handleMessageDeleted;
            this.messageReadEventHandler = controller::handleMessageRead;
            this.userTypingEventHandler = controller::handleUserTyping;
            this.userStatusChangedEventHandler = controller::handleUserStatusChanged;
            this.chatInfoChangedEventHandler = controller::handleChatInfoChanged;
        } else {
            this.newMessageEventHandler = null;
            this.messageEditedEventHandler = null;
            this.messageDeletedEventHandler = null;
            this.messageReadEventHandler = null;
            this.userTypingEventHandler = null;
            this.userStatusChangedEventHandler = null;
            this.chatInfoChangedEventHandler = null;
        }
    }

    public void onNewMessage(NewMessageEventModel message) {
        if (newMessageEventHandler != null) {
            Platform.runLater(() -> newMessageEventHandler.accept(message));
        } else {
            System.err.println("ChatUIService: No active chat controller to handle new message.");
        }
    }

    public void onMessageEdited(MessageEditedEventModel message) {
        if (messageEditedEventHandler != null) {
            Platform.runLater(() -> messageEditedEventHandler.accept(message));
        } else {
            System.err.println("ChatUIService: No active chat controller to handle message edited event.");
        }
    }

    public void onMessageDeleted(MessageDeletedEventModel message) {
        if (messageDeletedEventHandler != null) {
            Platform.runLater(() -> messageDeletedEventHandler.accept(message));
        } else {
            System.err.println("ChatUIService: No active chat controller to handle message deleted event.");
        }
    }

    public void onMessageRead(MessageReadEventModel message) {
        if (messageReadEventHandler != null) {
            Platform.runLater(() -> messageReadEventHandler.accept(message));
        } else {
            System.err.println("ChatUIService: No active chat controller to handle message read event.");
        }
    }

    public void onUserTyping(UserIsTypingEventModel eventModel) {
        if (userTypingEventHandler != null) {
            Platform.runLater(() -> userTypingEventHandler.accept(eventModel));
        } else {
            System.err.println("ChatUIService: No active chat controller to handle user typing event.");
        }
    }
    public void onUserStatusChanged(UserStatusChangedEventModel eventModel) {
        if (userStatusChangedEventHandler != null) {
            Platform.runLater(() -> userStatusChangedEventHandler.accept(eventModel));
        } else {
            System.err.println("ChatUIService: No active chat controller to handle user status changed event.");
        }
    }

    public void onChatInfoChanged(ChatInfoChangedEventModel eventModel) {
        if (chatInfoChangedEventHandler != null) {
            Platform.runLater(() -> chatInfoChangedEventHandler.accept(eventModel));
        } else {
            System.err.println("ChatUIService: No active chat controller to handle chat info changed event.");
        }
    }
}