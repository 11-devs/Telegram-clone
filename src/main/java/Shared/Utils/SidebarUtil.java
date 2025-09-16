package Shared.Utils;

import Client.Controllers.SidebarMenuController;
import javafx.animation.*;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class SidebarUtil {

    private static boolean isSidebarOpen = false; // Flag to prevent multiple openings

    public static void showSidebarDialog(Stage parentStage, String fxmlPath, Object parentController) {
        if (isSidebarOpen || parentStage == null || parentStage.getScene() == null) {
            return;
        }
        isSidebarOpen = true;

        try {
            URL fxmlUrl = SidebarUtil.class.getResource(fxmlPath);

            if (fxmlUrl == null) {
                System.err.println("CRITICAL FXML LOAD ERROR: Resource not found at path: " + fxmlPath);
                isSidebarOpen = false; // Reset flag on error
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent sidebarRoot = loader.load();

            System.out.println("DEBUG: SidebarUtil - FXML loaded successfully. Controller is: " + loader.getController().getClass().getName());
            SidebarMenuController sidebarController = loader.getController();

            sidebarController.setParentController(parentController);

            // 2. Get the root StackPane from the parent stage
            if (!(parentStage.getScene().getRoot() instanceof StackPane rootStackPane)) {
                System.err.println("CRITICAL LAYOUT ERROR: The root of mainChat.fxml is NOT a StackPane.");
                System.err.println("Actual root type is: " + parentStage.getScene().getRoot().getClass().getName());
                isSidebarOpen = false;
                return;
            }

            System.out.println("DEBUG: SidebarUtil - rootStackPane found successfully.");

            Node mainContent = rootStackPane.getChildren().getFirst();

            // 3. Create and configure the dimming pane
            Pane dimPane = new Pane();
            dimPane.getStyleClass().add("sidebar-dim-pane");
            dimPane.setOpacity(0.0);

            dimPane.prefWidthProperty().bind(rootStackPane.widthProperty());
            dimPane.prefHeightProperty().bind(rootStackPane.heightProperty());

            // 4. Position and size the sidebar
            StackPane.setAlignment(sidebarRoot, Pos.CENTER_LEFT);
            if (sidebarRoot instanceof Region) {
                ((Region) sidebarRoot).prefHeightProperty().bind(rootStackPane.heightProperty());
            }
            sidebarRoot.setTranslateX(-((Region) sidebarRoot).getPrefWidth()); // Start off-screen

            // Add dim pane first, then sidebar on top of it
            rootStackPane.getChildren().addAll(dimPane, sidebarRoot);
            mainContent.setMouseTransparent(true); // Disable clicks on content behind

            // 5. Define animations
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(150), sidebarRoot);
            slideIn.setToX(0);
            slideIn.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), dimPane);
            fadeIn.setToValue(1.0);

            // 6. Define the closing logic
            Runnable closeSidebar = () -> {
                dimPane.setMouseTransparent(true);

                TranslateTransition slideOut = new TranslateTransition(Duration.millis(150), sidebarRoot);
                slideOut.setToX(-sidebarRoot.getBoundsInLocal().getWidth());
                slideOut.setInterpolator(Interpolator.EASE_IN);

                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), dimPane);
                fadeOut.setToValue(0.0);

                slideOut.setOnFinished(e -> {
                    rootStackPane.getChildren().removeAll(dimPane, sidebarRoot);
                    mainContent.setMouseTransparent(false);
                    isSidebarOpen = false;
                });

                fadeOut.play();
                slideOut.play();
            };

            sidebarController.setCloseHandler(closeSidebar);
            dimPane.setOnMouseClicked(event -> {
                closeSidebar.run();
                event.consume(); // Consume event to prevent it from bubbling further
            });

            // 7. Play animations
            fadeIn.play();
            slideIn.play();

        } catch (IOException e) {
            System.err.println("Error loading sidebar dialog: " + e.getMessage());
            e.printStackTrace();
            isSidebarOpen = false;
        }
    }
}