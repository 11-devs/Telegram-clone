package Shared.Utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;

/**
 * Utility class for managing scenes and dialog windows in JavaFX applications.
 */
public class SceneUtil {

    /**
     * Changes the current scene to a new scene loaded from an FXML file.
     *
     * @param node The node to get the current stage from
     * @param fxmlPath The path to the FXML file (e.g., "/Client/fxml/newScene.fxml")
     */
    public static void changeScene(Node node, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneUtil.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) node.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading scene: " + fxmlPath);
            throw new RuntimeException("Error loading scene: " + fxmlPath, e);
        }
    }

    /**
     * Changes the current scene to a new scene loaded from an FXML file with the same size.
     *
     * @param node The node to get the current stage from
     * @param fxmlPath The path to the FXML file (e.g., "/Client/fxml/newScene.fxml")
     */
    public static void changeSceneWithSameSize(Node node, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneUtil.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) node.getScene().getWindow();
            Scene currentScene = node.getScene();
            double width = currentScene.getWidth();
            double height = currentScene.getHeight();
            Scene newScene = new Scene(root, width, height);
            stage.setScene(newScene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading scene with same size: " + fxmlPath);
            throw new RuntimeException("Error loading scene with same size: " + fxmlPath, e);
        }
    }

    /**
     * Creates and configures a dialog stage from an FXML file.
     *
     * @param fxmlPath The path to the FXML file (e.g., "/Client/fxml/countrySelection.fxml")
     * @param parentStage The parent stage to initialize the dialog ownership
     * @param parentController The parent controller (optional, can be null)
     * @param data Any additional data to pass to the dialog controller (e.g., a list of countries)
     * @param title The title of the dialog window
     * @param <T> The type of the dialog controller
     * @return The configured dialog stage
     * @throws IOException If the FXML file cannot be loaded
     */
    public static <T> Stage createDialog(String fxmlPath, Stage parentStage, Object parentController, Object data, String title) throws IOException {
        // Load the FXML file
        URL fxmlUrl = SceneUtil.class.getResource(fxmlPath);
        if (fxmlUrl == null) {
            throw new IOException("FXML file not found: " + fxmlPath);
        }
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        // Create and configure the dialog stage
        Stage dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(scene);
        dialogStage.initOwner(parentStage);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(false);
        dialogStage.setTitle(title); // Set the custom title

        // Set the dialog stage and data to the controller
        T controller = loader.getController();
        if (controller != null) {
            try {
                // Use reflection to call setter methods dynamically
                if (parentController != null) {
                    controller.getClass().getMethod("setParentController", Object.class).invoke(controller, parentController);
                }
                if (data != null) {
                    controller.getClass().getMethod("setData", Object.class).invoke(controller, data);
                }
                controller.getClass().getMethod("setDialogStage", Stage.class).invoke(controller, dialogStage);
            } catch (Exception e) {
                System.err.println("Error setting controller properties: " + e.getMessage());
            }
        }

        return dialogStage;
    }
}