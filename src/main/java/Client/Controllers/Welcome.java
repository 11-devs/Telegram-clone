package Client.Controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import static Shared.Utils.SceneUtil.changeSceneWithSameSize;

public class Welcome {
    @FXML
    private VBox root;

    public void initialize() {
        // ...
    }

    @FXML
    private void startMessaging() {
        changeSceneWithSameSize(root, "/Client/fxml/phoneNumber.fxml");
    }
}