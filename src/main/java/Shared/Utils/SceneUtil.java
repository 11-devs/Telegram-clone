package Shared.Utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
}