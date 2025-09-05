package Client.Services.UI;

import Client.Controllers.MainChatController;
import Shared.Events.Models.NewMessageEventModel;
import javafx.application.Platform;

import java.util.function.Consumer;

public class ChatUIService {
    private Consumer<NewMessageEventModel> messageHandler;

    public void setActiveChatController(MainChatController controller) {
        if (controller != null) {
            this.messageHandler = controller::handleIncomingMessage;
        } else {
            this.messageHandler = null;
        }
    }

    public void onNewMessage(NewMessageEventModel message) {
        if (messageHandler != null) {
            Platform.runLater(() -> messageHandler.accept(message));
        } else {
            System.err.println("ChatUIService: No active chat controller to handle new message.");
            // Potential enhancement: Queue messages or show a system notification.
        }
    }
}