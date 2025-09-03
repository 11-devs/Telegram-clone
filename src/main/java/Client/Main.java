package Client;

import JSocket2.Protocol.StatusCode;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    private static final String LOGO_PATH = "/Client/images/TelegramLogo.png";

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Upload FXML file
        // --------------------------------
        // Client.Main order
        var authModel = AccessKeyManager.loadAuthModel();
        var connectionManager = AppConnectionManager.getInstance().getConnectionManager();
        connectionManager.addExternalConnectedListener(clientApplication -> {
                    if(authModel!= null && authModel.getAccessKeyCount() != 0) {
                        try {
                            var result = clientApplication.sendAuthModel(authModel);
                            if(result == StatusCode.OK) System.out.println("Login Successful");
                            else System.out.println("Login failed");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
        connectionManager.createAndStartClient();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Client/fxml/resetPassword.fxml")));
//        if(authModel!= null && authModel.getAccessKeyCount() != 0){
//            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Client/fxml/CloudPasswordCheck.fxml")));
//        }

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