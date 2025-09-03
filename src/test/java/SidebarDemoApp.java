import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import Client.Controllers.SidebarMenuController;

/**
 * Demo application showing the Telegram-style sidebar menu
 */
public class SidebarDemoApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the sidebar FXML as a VBox
            FXMLLoader sidebarLoader = new FXMLLoader(getClass().getResource("/Client/fxml/sidebarMenu.fxml"));
            VBox sidebarRoot = sidebarLoader.load();
            SidebarMenuController sidebarController = sidebarLoader.getController();

            // Inject primaryStage into the controller
            sidebarController.setPrimaryStage(primaryStage);

            // Create main layout
            BorderPane mainLayout = new BorderPane();
            mainLayout.setLeft(sidebarRoot); // Add the VBox as the left component

            // Create scene
            Scene scene = new Scene(mainLayout, 1200, 800);

            // Set up stage
            primaryStage.setTitle("Telegram Desktop - Sidebar Demo");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);

            // Show the stage
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

/**
 * Integration example for existing MainChatController
 */
class MainChatIntegration {

    /**
     * Example of how to integrate the sidebar with existing main chat
     */
    public void integrateWithMainChat() {
        /*
        // In your MainChatController.initialize() method:

        try {
            // Load sidebar
            FXMLLoader sidebarLoader = new FXMLLoader(getClass().getResource("/fxml/sidebarMenu.fxml"));
            VBox sidebar = sidebarLoader.load();
            SidebarMenuController sidebarController = sidebarLoader.getController();

            // Add to your main layout (assuming you have a BorderPane or similar)
            mainBorderPane.setLeft(sidebar);

            // Connect menu actions to your main chat functionality
            setupSidebarIntegration(sidebarController);

        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }

    /**
     * Setup integration between sidebar and main chat
     */
    public void setupSidebarIntegration(SidebarMenuController sidebarController) {
        /*
        // Override button actions to integrate with existing functionality

        sidebarController.myProfileButton.setOnAction(event -> {
            // Show profile in right panel
            rightPanel.setVisible(true);
            // Load profile data
            loadUserProfileData();
        });

        sidebarController.contactsButton.setOnAction(event -> {
            // Switch to contacts view
            showContactsList();
        });

        sidebarController.settingsButton.setOnAction(event -> {
            // Show settings dialog or panel
            showSettingsPanel();
        });

        sidebarController.nightModeToggle.setOnAction(event -> {
            // Apply theme to entire application
            boolean isDark = sidebarController.isNightModeEnabled();
            applyGlobalTheme(isDark);
        });
        */
    }
}

/**
 * Custom ToggleSwitch implementation since JavaFX doesn't have one built-in
 */
class CustomToggleSwitch extends javafx.scene.control.Button {

    private boolean selected = false;

    public CustomToggleSwitch() {
        super();
        getStyleClass().add("toggle-switch");
        setOnAction(event -> toggle());
        updateAppearance();
    }

    public void toggle() {
        setSelected(!selected);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        updateAppearance();
    }

    public boolean isSelected() {
        return selected;
    }

    private void updateAppearance() {
        getStyleClass().removeAll("toggle-on", "toggle-off");
        if (selected) {
            getStyleClass().add("toggle-on");
        } else {
            getStyleClass().add("toggle-off");
        }
    }
}