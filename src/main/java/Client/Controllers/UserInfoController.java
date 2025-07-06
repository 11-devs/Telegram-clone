package Client.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static Shared.Utils.SceneUtil.changeSceneWithSameSize;

public class UserInfoController implements Initializable {

    @FXML
    private VBox root;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private ImageView photoPreview;
    @FXML
    public Button signUpButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Controller initialized successfully");
        // Animation of moving from a little further right to the main target
        if (root != null) {
            root.setTranslateX(75);
            var transition = new javafx.animation.TranslateTransition(javafx.util.Duration.seconds(0.5), root);
            transition.setToX(0);
            transition.setAutoReverse(false);
            transition.setCycleCount(1);
            transition.play();
        }
        firstNameField.requestFocus(); // Initial focus on first name field
    }

    @FXML
    private void handleSignUp() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
            System.out.println("Sign Up details - First Name: " + firstName + ", Last Name: " + lastName);
            // TODO: Add sign-up logic (e.g., photo upload, validation)
            // Example: changeSceneWithSameSize(root, "Client/fxml/verificationViaSms.fxml");
        } else {
            System.out.println("Please fill all fields.");
        }
    }

    // TODO: There is a problem with vertical stretching of long photos
    @FXML
    private void handleUploadPhoto(MouseEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(photoPreview.getScene().getWindow());
        if (selectedFile != null) {
            System.out.println("File selected: " + selectedFile.getAbsolutePath());
            long maxSizeBytes = 10L * 1024 * 1024; // 10 MB
            long fileSizeBytes = selectedFile.length();
            if (fileSizeBytes <= maxSizeBytes) {
                // Load image and crop to fit within 168x171, centered
                Image originalImage = new Image(selectedFile.toURI().toString());
                double fitWidth = 171.0; // Fixed width from FXML
                double fitHeight = 168.0; // Fixed height from FXML
                // Create a temporary ImageView to handle the cropping
                ImageView tempView = new ImageView(originalImage);
                tempView.setFitWidth(fitWidth);
                tempView.setFitHeight(fitHeight);
                tempView.setPreserveRatio(false); // Allow stretching to fit
                // Set the cropped image to the main ImageView
                photoPreview.setFitWidth(fitWidth);
                photoPreview.setFitHeight(fitHeight);
                photoPreview.setPreserveRatio(false);
                photoPreview.setImage(originalImage);
                System.out.println("Image loaded successfully with fixed size and centered crop");
            } else {
                System.out.println("File size exceeds 10 MB limit. Please select a smaller image.");
            }
        } else {
            System.out.println("No file selected or dialog cancelled.");
        }
    }
}