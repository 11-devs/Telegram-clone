package Shared.Utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.function.Consumer;
import java.io.IOException;

public class SceneUtil {

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
    public static <T> void changeSceneWithSameSize(Node node, String fxmlPath, Consumer<T> dataUpdater) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneUtil.class.getResource(fxmlPath));
            Parent root = loader.load();
            T controller = loader.getController();

            if (dataUpdater != null) {
                dataUpdater.accept(controller);
            }

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