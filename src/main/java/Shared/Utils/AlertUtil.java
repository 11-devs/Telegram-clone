package Shared.Utils;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class AlertUtil {

    private static final String LOGO_PATH = "/Client/images/TelegramLogo.png";

    public static void showAlert(AlertType type, String title, String headerText, String message) {
        showAlert(type, title, headerText, message, "/Client/css/alert.css");
    }

    public static void showAlert(AlertType type, String title, String headerText, String message, String cssPath) {
        try {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(headerText != null ? headerText : "");
            alert.setContentText(message);
            if (cssPath != null) {
                alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(AlertUtil.class.getResource(cssPath)).toExternalForm());
            }

            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(Objects.requireNonNull(
                    AlertUtil.class.getResourceAsStream(LOGO_PATH)
            )));

            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error showing alert: " + e.getMessage());
        }
    }

    public static void showError(String message) {
        showAlert(AlertType.ERROR, "Error", null, message);
    }

    public static void showSuccess(String message) {
        showAlert(AlertType.INFORMATION, "Success", null, message);
    }

    public static void showWarning(String message) {
        showAlert(AlertType.WARNING, "Warning", null, message);
    }

    public static Alert showCustomAlert(AlertType type, String title, String headerText, String message, String cssPath, ButtonType... buttonTypes) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headerText != null ? headerText : "");
        alert.setContentText(message);
        alert.getButtonTypes().setAll(buttonTypes);
        if (cssPath != null) {
            alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(AlertUtil.class.getResource(cssPath)).toExternalForm());
        }

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Objects.requireNonNull(
                AlertUtil.class.getResourceAsStream(LOGO_PATH)
        )));

        alert.showAndWait();
        return alert;
    }

    public static String showInputDialog(String title, String headerText, String defaultValue, String cssPath) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText("Please enter your input:");
        if (cssPath != null) {
            dialog.getDialogPane().getStylesheets().add(Objects.requireNonNull(AlertUtil.class.getResource(cssPath)).toExternalForm());
        }

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Objects.requireNonNull(
                AlertUtil.class.getResourceAsStream(LOGO_PATH)
        )));

        return dialog.showAndWait().orElse(null);
    }
}