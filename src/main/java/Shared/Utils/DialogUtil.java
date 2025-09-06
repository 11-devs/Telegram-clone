package Shared.Utils;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.effect.ColorAdjust;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

import static Shared.Utils.SceneUtil.createDialog;

/**
 * Utility class for managing dialog windows in JavaFX applications.
 */
public class DialogUtil {

    // dim : [-1,1]
    public static void showNotificationDialog(Stage parentStage, String message) {
        try {
            // Apply dim effect to parent stage with animation
            ColorAdjust dimEffect = new ColorAdjust();
            parentStage.getScene().getRoot().setEffect(dimEffect);

            // Animation for dimming with smoother interpolation
            Timeline fadeIn = new Timeline(
                    new KeyFrame(Duration.ZERO, new javafx.animation.KeyValue(dimEffect.brightnessProperty(), 0)),
                    new KeyFrame(Duration.millis(300), new javafx.animation.KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH))
            );
            fadeIn.play();

            // Use SceneUtil.createDialog to load and configure the dialog
            Stage dialogStage = createDialog("/Client/fxml/notificationDialog.fxml", parentStage, null, message, "Notification");

            // Center the dialog stage with an offset downward
            Platform.runLater(() -> {
                double centerX = parentStage.getX() + (parentStage.getWidth() - dialogStage.getWidth()) / 2;
                double centerY = parentStage.getY() + (parentStage.getHeight() - dialogStage.getHeight()) / 2;
                dialogStage.setX(centerX);
                dialogStage.setY(centerY);
            });

            // Show the dialog and handle closing with reverse animation
            dialogStage.showAndWait();

            // Reverse animation for undimming with smoother interpolation
            Timeline fadeOut = new Timeline(
                    new KeyFrame(Duration.ZERO, new javafx.animation.KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.millis(300), new javafx.animation.KeyValue(dimEffect.brightnessProperty(), 0))
            );
            fadeOut.setOnFinished(e -> parentStage.getScene().getRoot().setEffect(null));
            fadeOut.play();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading notification dialog: " + e.getMessage());
        }
    }
}