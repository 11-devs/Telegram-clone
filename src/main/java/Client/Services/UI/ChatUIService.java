package Client.Services.UI;

import Client.Controllers.MainChatController;
import Shared.Events.Models.MessageDeletedEventModel;
import Shared.Events.Models.MessageDeliveredEventModel;
import Shared.Events.Models.MessageEditedEventModel;
import Shared.Events.Models.MessageReadEventModel;
import Shared.Events.Models.NewMessageEventModel;
import Shared.Events.Models.UserIsTypingEventModel;
import javafx.application.Platform;

import java.util.function.Consumer;

public class ChatUIService {
    private Consumer<NewMessageEventModel> newMessageEventHandler;
    private Consumer<MessageDeliveredEventModel> messageDeliveredEventHandler;
    private Consumer<MessageEditedEventModel> messageEditedEventHandler;
    private Consumer<MessageDeletedEventModel> messageDeletedEventHandler;
    private Consumer<MessageReadEventModel> messageReadEventHandler;
    private Consumer<UserIsTypingEventModel> userTypingEventHandler;

    public void setActiveChatController(MainChatController controller) {
        if (controller != null) {
            this.newMessageEventHandler = controller::handleIncomingMessage;
            this.messageDeliveredEventHandler = controller::handleMessageDelivered;
            this.messageEditedEventHandler = controller::handleMessageEdited;
            this.messageDeletedEventHandler = controller::handleMessageDeleted;
            this.messageReadEventHandler = controller::handleMessageRead;
            this.userTypingEventHandler = controller::handleUserTyping;
        } else {
            this.newMessageEventHandler = null;
            this.messageDeliveredEventHandler = null;
            this.messageEditedEventHandler = null;
            this.messageDeletedEventHandler = null;
            this.messageReadEventHandler = null;
            this.userTypingEventHandler = null;
        }
    }

    public void onNewMessage(NewMessageEventModel message) {
        if (newMessageEventHandler != null) {
            Platform.runLater(() -> newMessageEventHandler.accept(message));
        } else {
            System.err.println("ChatUIService: No active chat controller to handle new message.");
        }
    }

    public void onMessageDelivered(MessageDeliveredEventModel message) {
        if (messageDeliveredEventHandler != null) {
            Platform.runLater(() -> messageDeliveredEventHandler.accept(message));
        } else {
            System.err.println("ChatUIService: No active chat controller to handle message delivered event.");
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
}