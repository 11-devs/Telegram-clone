import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    private static final String LOGO_PATH = "/Client/images/TelegramLogo.png";

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Upload FXML file
        // --------------------------------
        // Main order
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Client/fxml/welcome.fxml")));
        // --------------------------------
        // Custom order
        // Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Client/fxml/userInfo.fxml")));
        // --------------------------------

        // Create a scene
        Scene scene = new Scene(root); // Initial size according to FXML

        // Set window title
        primaryStage.setTitle("Telegram Desktop");

        // Set icon for app
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(
                Main.class.getResourceAsStream(LOGO_PATH)
        )));

        // Setting the scene and window display
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}