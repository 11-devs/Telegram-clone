package Client.Controllers;

import Client.Services.ThemeManager;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AppearanceSettingsController {

    @FXML
    private Label sectionTitleLabel;
    @FXML
    private ChoiceBox<ThemeManager.Theme> themeChoiceBox;

    private SettingsController parentController;
    private Stage dialogStage;

    public void setParentController(SettingsController parentController) {
        this.parentController = parentController;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setSectionTitle(String title) {
        if (sectionTitleLabel != null) {
            sectionTitleLabel.setText(title);
        }
    }

    @FXML
    private void initialize() {
        // Populate the ChoiceBox with all available themes from the enum
        themeChoiceBox.getItems().setAll(ThemeManager.Theme.values());

        // Set the current selection based on the ThemeManager
        ThemeManager themeManager = ThemeManager.getInstance();
        themeChoiceBox.setValue(themeManager.getCurrentTheme());

        // Add a listener to update the theme when the user selects a new one
        themeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldTheme, newTheme) -> {
            if (newTheme != null) {
                themeManager.setTheme(newTheme);
                // The theme change is now propagated automatically via listeners
            }
        });
    }

    @FXML
    private void handleBack() {
        if (parentController != null) {
            parentController.goBackToMainSettings();
        }
    }
}