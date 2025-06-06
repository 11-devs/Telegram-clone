import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

public class DialogTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Dialog Test");

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

        root.getChildren().addAll(infoButton, warningButton, errorButton, inputButton);

        Scene scene = new Scene(root, 300, 200);
        scene.getStylesheets().add(getClass().getResource("dialog.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showInfoDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("This is an info message");
        alert.setContentText("This is a detailed info message that should wrap text if too long.");
        alert.getDialogPane().getStylesheets().add(getClass().getResource("dialog.css").toExternalForm());
        alert.showAndWait();
    }

    private void showWarningDialog() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText("This is a warning message");
        alert.setContentText("Please be cautious with this action.");
        alert.getDialogPane().getStylesheets().add(getClass().getResource("dialog.css").toExternalForm());
        alert.showAndWait();
    }

    private void showErrorDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("This is an error message");
        alert.setContentText("Something went wrong. Please try again.");
        alert.getDialogPane().getStylesheets().add(getClass().getResource("dialog.css").toExternalForm());
        alert.showAndWait();
    }

    private void showTextInputDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Text Input");
        dialog.setHeaderText("Enter your name");
        dialog.setContentText("Please enter your name:");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("dialog.css").toExternalForm());
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> System.out.println("Your name: " + name));
    }

    public static void main(String[] args) {
        launch(args);
    }
}