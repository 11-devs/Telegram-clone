package Shared.Utils;

import javafx.application.Platform;
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
import javafx.scene.layout.Region;

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

    public static <T> void changeScene(Node node, String fxmlPath, Consumer<T> dataUpdater) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneUtil.class.getResource(fxmlPath));
            Parent root = loader.load();
            T controller = loader.getController();

            if (dataUpdater != null) {
                dataUpdater.accept(controller);
            }

            Stage stage = (Stage) node.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading scene: " + fxmlPath);
            throw new RuntimeException("Error loading scene: " + fxmlPath, e);
        }
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

    // In Shared.Utils.SceneUtil

    /**
     * Loads a sub-scene and explicitly resizes the parent Stage to fit the
     * preferred size defined within the loaded FXML file.
     * This is a more robust method than sizeToScene().
     *
     * @param fxmlPath         The path to the FXML file.
     * @param parentController The parent controller.
     * @param dialogStage      The stage to be resized.
     * @param data             The data to be passed to the new controller.
     * @return The loaded Parent node.
     */
    public static <T extends Parent> T loadSubSceneAndResize(String fxmlPath, Object parentController, Stage dialogStage, Object data) {
        try {
            URL fxmlUrl = SceneUtil.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("FXML file not found: " + fxmlPath);
                return null;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            T subSectionRoot = loader.load();

            // --- NEW & IMPROVED RESIZING LOGIC ---
            if (dialogStage != null && subSectionRoot instanceof Region) {
                Region rootRegion = (Region) subSectionRoot;
                double prefWidth = rootRegion.getPrefWidth();
                double prefHeight = rootRegion.getPrefHeight();

                // Check if valid dimensions are set in the FXML
                if (prefWidth > 0 && prefHeight > 0) {
                    // We use Platform.runLater to ensure this happens after the scene graph is updated
                    Platform.runLater(() -> {
                        dialogStage.setWidth(prefWidth);
                        dialogStage.setHeight(prefHeight);
                        // Center the stage on the screen after resizing
                        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                        dialogStage.setX((screenBounds.getWidth() - dialogStage.getWidth()) / 2);
                        dialogStage.setY((screenBounds.getHeight() - dialogStage.getHeight()) / 2);
                    });
                }
            }

            // --- The rest of the method for setting up the controller remains the same ---
            Object subController = loader.getController();
            if (subController != null) {
                // ... (Your existing reflection code for setting controllers and data)
                try {
                    subController.getClass().getMethod("setDialogStage", Stage.class).invoke(subController, dialogStage);
                } catch (NoSuchMethodException ignored) {}
                try {
                    subController.getClass().getMethod("setParentController", parentController.getClass()).invoke(subController, parentController);
                } catch (NoSuchMethodException e) {
                    try {
                        subController.getClass().getMethod("setParentController", Object.class).invoke(subController, parentController);
                    } catch (NoSuchMethodException ignored) {}
                }
                try {
                    if (data != null) {
                        subController.getClass().getMethod("setData", data.getClass()).invoke(subController, data);
                    }
                } catch (NoSuchMethodException e) {
                    try {
                        subController.getClass().getMethod("setData", Object.class).invoke(subController, data);
                    } catch (NoSuchMethodException ignored) {}
                }
            }

            return subSectionRoot;

        } catch (Exception e) {
            System.err.println("Error loading and resizing sub-scene: " + fxmlPath);
            e.printStackTrace();
            return null;
        }
    }
}