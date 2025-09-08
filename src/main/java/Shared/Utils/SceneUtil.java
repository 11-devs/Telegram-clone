package Shared.Utils;

import Client.Services.ThemeManager;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.Objects;
import java.util.function.Consumer;
import java.io.IOException;

public class SceneUtil {

    /**
     * Helper method to apply theme stylesheets and the current theme class to a scene.
     * @param scene The scene to apply the theme to.
     */
    private static void applyThemeToScene(Scene scene) {
        if (scene == null) return;
        scene.getStylesheets().add(Objects.requireNonNull(SceneUtil.class.getResource("/Client/css/themes.css")).toExternalForm());
        ThemeManager.getInstance().applyTheme(scene);
    }

    public static void changeScene(Node node, String fxmlPath) {
        changeScene(node, fxmlPath, (Object controller) -> {}, false);
    }

    public static <T> void changeScene(Node node, String fxmlPath, Consumer<T> dataUpdater) {
        changeScene(node, fxmlPath, dataUpdater, false);
    }

    public static <T> void changeScene(Node node, String fxmlPath, Consumer<T> dataUpdater, boolean maximized) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneUtil.class.getResource(fxmlPath));
            Parent root = loader.load();
            T controller = loader.getController();

            if (dataUpdater != null) {
                dataUpdater.accept(controller);
            }

            Stage stage = (Stage) node.getScene().getWindow();
            Scene scene = new Scene(root);

            applyThemeToScene(scene); // Apply theme to the new scene

            stage.setScene(scene);
            stage.setMaximized(maximized);

            if (!maximized) {
                stage.sizeToScene();
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
                stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
            }

            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading scene: " + fxmlPath);
            throw new RuntimeException("Error loading scene: " + fxmlPath, e);
        }
    }

    public static void changeSceneWithSameSize(Node node, String fxmlPath) {
        changeSceneWithSameSize(node, fxmlPath, (Object controller) -> {});
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

            applyThemeToScene(newScene); // Apply theme to the new scene

            stage.setScene(newScene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading scene with same size: " + fxmlPath);
            throw new RuntimeException("Error loading scene with same size: " + fxmlPath, e);
        }
    }

    public static <T> Stage createDialog(String fxmlPath, Stage parentStage, Object parentController, Object data, String title) throws IOException {
        URL fxmlUrl = SceneUtil.class.getResource(fxmlPath);
        if (fxmlUrl == null) {
            throw new IOException("FXML file not found: " + fxmlPath);
        }
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        Stage dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        applyThemeToScene(scene); // Apply theme to the new dialog's scene

        dialogStage.setScene(scene);
        dialogStage.initOwner(parentStage);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(false);
        dialogStage.setTitle(title);

        T controller = loader.getController();
        if (controller != null) {
            try {
                controller.getClass().getMethod("setDialogStage", Stage.class).invoke(controller, dialogStage);
                if (parentController != null) {
                    try {
                        controller.getClass().getMethod("setParentController", Object.class).invoke(controller, parentController);
                    } catch (NoSuchMethodException e) {
                        System.err.println("The controller " + controller.getClass().getName() + " does not have a 'setParentController(Object parentController)' method.");
                    }
                }
                if (data != null) {
                    try {
                        controller.getClass().getMethod("setData", Object.class).invoke(controller, data);
                    } catch (NoSuchMethodException e) {
                        System.err.println("The controller " + controller.getClass().getName() + " does not have a 'setData(Object data)' method.");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error setting controller properties: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return dialogStage;
    }

    // The loadSubScene method does not create a new scene, so it doesn't need changes.
    public static <T extends Parent> T loadSubScene(String fxmlPath, Object parentController, Stage dialogStage, Object data) {
        try {
            URL fxmlUrl = SceneUtil.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("FXML file not found: " + fxmlPath);
                return null;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            T subSectionRoot = loader.load();
            Object subController = loader.getController();

            if (subController != null) {
                // Dynamically set properties on the sub-controller using reflection
                try {
                    // Set Dialog Stage
                    subController.getClass().getMethod("setDialogStage", Stage.class).invoke(subController, dialogStage);
                } catch (NoSuchMethodException ignored) { /* Method not required */ }

                try {
                    // Set Parent Controller
                    subController.getClass().getMethod("setParentController", parentController.getClass()).invoke(subController, parentController);
                } catch (NoSuchMethodException e) {
                    try {
                        subController.getClass().getMethod("setParentController", Object.class).invoke(subController, parentController);
                    } catch (NoSuchMethodException ignored) { /* Method not required */ }
                }

                try {
                    // Set Data
                    if (data != null) {
                        subController.getClass().getMethod("setData", data.getClass()).invoke(subController, data);
                    }
                } catch (NoSuchMethodException e) {
                    try {
                        subController.getClass().getMethod("setData", Object.class).invoke(subController, data);
                    } catch (NoSuchMethodException ignored) { /* Method not required */ }
                }
            }

            return subSectionRoot;

        } catch (Exception e) {
            System.err.println("Error loading sub-scene: " + fxmlPath);
            e.printStackTrace();
            return null;
        }
    }
}