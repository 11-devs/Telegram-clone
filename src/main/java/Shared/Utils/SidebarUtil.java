package Shared.Utils;

import Client.Controllers.SidebarMenuController;
import javafx.animation.FadeTransition;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.fxml.FXMLLoader;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class SidebarUtil {

    public static void showSidebarDialog(Stage parentStage, String fxmlPath, Object controller) {
        try {
            // Loading Sidebar FXML
            URL fxmlUrl = SidebarUtil.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent sidebarRoot = loader.load();
            SidebarMenuController sidebarController = loader.getController();

            // Parent controller setting
            if (controller != null) {
                sidebarController.setParentController(controller);
            }

            // Adjust sidebar size (recast to Region)
            if (sidebarRoot instanceof Region) {
                Region region = (Region) sidebarRoot;
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                double sidebarWidth = 300; // fixed width
                double sidebarHeight = screenBounds.getHeight() - 40; // The height is proportional to the page
                region.setPrefWidth(sidebarWidth);
                region.setPrefHeight(sidebarHeight);
            } else {
                System.err.println("sidebarRoot is not a Region, manual size adjustment not applied.");
            }

            // Create a Scene for the Sidebar
            Scene sidebarScene = new Scene(sidebarRoot);
            sidebarScene.getStylesheets().add("/Client/css/mainChat.css");

            // Create a Stage for the Sidebar
            Stage sidebarStage = new Stage();
            sidebarStage.initStyle(StageStyle.TRANSPARENT);
            sidebarStage.setScene(sidebarScene);
            sidebarStage.initOwner(parentStage);
            sidebarStage.setResizable(false);

            // Stage size setting
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double sidebarWidth = 300;
            double sidebarHeight = screenBounds.getHeight() - 40;
            sidebarStage.setWidth(sidebarWidth);
            sidebarStage.setHeight(sidebarHeight);

            // Sidebar position (from left)
            sidebarStage.setX(screenBounds.getMinX());
            sidebarStage.setY(screenBounds.getMinY() + 20);

            // Apply dim effect to parent
            ColorAdjust dimEffect = new ColorAdjust();
            dimEffect.setBrightness(-0.3);
            parentStage.getScene().getRoot().setEffect(dimEffect);

            // Fade animation for dim
            FadeTransition dimFade = new FadeTransition(Duration.millis(200), parentStage.getScene().getRoot());
            dimFade.setFromValue(1.0);
            dimFade.setToValue(0.7);
            dimFade.play();

            // Close the sidebar by clicking outside
            sidebarScene.setOnMouseClicked((MouseEvent event) -> {
                if (!sidebarRoot.getBoundsInParent().contains(event.getSceneX(), event.getSceneY())) {
                    sidebarStage.close();
                }
            });

            // Sidebar close event
            sidebarStage.setOnCloseRequest(event -> {
                // Remove the dim effect and return to normal mode
                FadeTransition undimFade = new FadeTransition(Duration.millis(200), parentStage.getScene().getRoot());
                undimFade.setFromValue(0.7);
                undimFade.setToValue(1.0);
                undimFade.setOnFinished(e -> parentStage.getScene().getRoot().setEffect(null));
                undimFade.play();
            });

            // Show sidebar
            sidebarStage.show();

        } catch (IOException e) {
            System.err.println("Error loading sidebar dialog: " + e.getMessage());
        }
    }
}