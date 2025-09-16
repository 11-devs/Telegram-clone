package Client;

import Client.Controllers.VideoPlayerController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * A standalone test application to launch and test the VideoPlayer dialog.
 * <p>
 * This class opens a file chooser, then loads the selected video file
 * into the video player FXML and displays it.
 */
public class VideoPlayerTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Video Player Test Launcher"); // Hidden stage for file chooser parent

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a Video File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.mkv", "*.avi", "*.mov", "*.flv"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/fxml/videoPlayerDialog.fxml"));
                Parent root = loader.load(); // FXML loader now instantiates the controller because of fx:controller attribute

                VideoPlayerController controller = loader.getController(); // <--- This will now correctly return the controller instance

                // IMPORTANT: Ensure the controller was successfully retrieved
                if (controller == null) {
                    throw new IllegalStateException("FXMLLoader failed to retrieve the VideoPlayerController. " +
                            "Check if 'fx:controller=\"Client.Controllers.VideoPlayerController\"' is correctly set in videoPlayerDialog.fxml.");
                }

                controller.setVideoFile(selectedFile);

                Stage videoStage = new Stage();
                videoStage.setTitle(selectedFile.getName());
                videoStage.setScene(new Scene(root));

                videoStage.setOnCloseRequest(event -> {
                    System.out.println("Video player closed. Cleaning up resources...");
                    controller.cleanup();
                    Platform.exit();
                });

                videoStage.show();

            } catch (IOException e) {
                e.printStackTrace();
                showErrorDialog("FXML Loading Error", "Could not load the video player UI. Please check the FXML file path and content.", e);
                Platform.exit();
            } catch (IllegalStateException e) {
                // Catch the specific error if controller is null
                e.printStackTrace();
                showErrorDialog("Controller Initialization Error", e.getMessage(), e);
                Platform.exit();
            }
        } else {
            System.out.println("No file selected. Exiting application.");
            Platform.exit();
        }
    }

    private void showErrorDialog(String title, String header, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText("Error: " + e.getLocalizedMessage());
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}