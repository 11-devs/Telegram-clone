import Shared.Utils.AlertUtil;
import Shared.Utils.DialogUtil;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class DialogTest extends Application {

    private static final String LOGO_PATH = "/Client/images/TelegramLogo.png";

    @Override
    public void start(Stage primaryStage) {

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #17212B;");

        Button infoButton = new Button("Show Info Dialog");
        infoButton.setOnAction(e -> showInfoDialog());

        Button warningButton = new Button("Show Warning Dialog");
        warningButton.setOnAction(e -> showWarningDialog());

        Button errorButton = new Button("Show Error Dialog");
        errorButton.setOnAction(e -> showErrorDialog());

        Button inputButton = new Button("Show Text Input Dialog");
        inputButton.setOnAction(e -> showTextInputDialog());

        Button notifyButton = new Button("Show notify Dialog");
        notifyButton.setOnAction(e -> showNotifyDialog(primaryStage));

        root.getChildren().addAll(infoButton, warningButton, errorButton, inputButton, notifyButton);

        primaryStage.getIcons().add(new Image(Objects.requireNonNull(
                AlertUtil.class.getResourceAsStream(LOGO_PATH)
        )));

        Scene scene = new Scene(root, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showInfoDialog() {
        AlertUtil.showSuccess("This is a detailed info message that should wrap text if too long.");
    }

    private void showWarningDialog() {
        AlertUtil.showWarning("Please be cautious with this action.");
    }

    private void showErrorDialog() {
        AlertUtil.showError("Something went wrong. Please try again.");
    }

    private void showTextInputDialog() {
        String result = AlertUtil.showInputDialog("Text Input", "Enter your name", "", "/Client/css/alert.css");
        if (result != null) {
            System.out.println("Your name: " + result);
        }
    }

    private void showNotifyDialog(Stage primaryStage) {
        DialogUtil.showNotificationDialog(primaryStage,"notification...\nnotification...\nnotification...");
    }

    public static void main(String[] args) {
        launch(args);
    }
}