import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Upload FXML file
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Client/fxml/welcome.fxml")));

        // Create a scene
        Scene scene = new Scene(root); // Initial size according to FXML

        // Set window title
        primaryStage.setTitle("Telegram Desktop");

        // Setting the scene and window display
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}