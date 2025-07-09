package Shared.Utils;

import Client.Controllers.NotificationDialogController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.animation.Interpolator;

import java.io.IOException;

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

            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(DialogUtil.class.getResource("/Client/fxml/notificationDialog.fxml"));
            Parent root = loader.load();

            // Create and configure the dialog stage
            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.TRANSPARENT); // Use transparent style
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT); // Round corners
            dialogStage.setScene(scene);
            dialogStage.initOwner(parentStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setResizable(false);

            // Set the message and dialog stage to the controller
            NotificationDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setMessage(message);

            // Center the dialog stage with an offset downward
            Platform.runLater(() -> {
                double centerX = parentStage.getX() + (parentStage.getWidth() - dialogStage.getWidth()) / 2;
                double centerY = parentStage.getY() + (parentStage.getHeight() - dialogStage.getHeight()) / 2 + 50; // Offset 50px down
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