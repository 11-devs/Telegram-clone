package Client.Controllers;

import Client.AppConnectionManager;
import JSocket2.Core.Client.ConnectionManager;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import static Shared.Utils.SceneUtil.changeSceneWithSameSize;

public class WelcomeController {
    @FXML
    private VBox root;
    private ConnectionManager connectionManager;
    public void initialize() {
        // Animation of moving from slightly lower to the original position
        connectionManager = AppConnectionManager.getInstance().getConnectionManager();
        if (root != null) {
            root.setTranslateY(75); // Starting a little lower (e.g. 75 pixels lower)
            TranslateTransition transition = new TranslateTransition(Duration.seconds(0.75), root); // Duration 0.75 second
            transition.setToY(0); // Return to the original position (center)
            transition.setAutoReverse(false);
            transition.setCycleCount(1);
            transition.play();
        }
    }

    // TODO: Connect to the server

    @FXML
    private void startMessaging() {
        changeSceneWithSameSize(root, "/Client/fxml/phoneNumber.fxml");
    }
}