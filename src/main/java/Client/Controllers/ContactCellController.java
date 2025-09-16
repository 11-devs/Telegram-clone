package Client.Controllers;

import Client.Services.FileDownloadService;
import Shared.Api.Models.ContactController.ContactInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;

import java.util.Objects;

public class ContactCellController {

    @FXML private GridPane root;
    @FXML private ImageView avatarImage;
    @FXML private Label contactNameLabel;
    @FXML private Label contactStatusLabel;
    @FXML private Circle onlineIndicator;

    private ContactInfo currentContact;
    private final FileDownloadService fileDownloadService = FileDownloadService.getInstance();

    public void updateCell(ContactInfo contact) {
        this.currentContact = contact;
        if (contact == null) return;

        contactNameLabel.setText(contact.getFirstName() + " " + contact.getLastName());
        onlineIndicator.setVisible(true);
        contactStatusLabel.setText(true ? "online" : "last seen recently");
        updateAvatar(contact.getProfilePictureId());
    }

    private void updateAvatar(String pictureId) {
        loadDefaultAvatar();
        if (pictureId != null && !pictureId.isEmpty()) {
            fileDownloadService.getImage(pictureId).thenAccept(avatar -> {
                if (avatar != null && currentContact != null && pictureId.equals(currentContact.getProfilePictureId())) {
                    Platform.runLater(() -> avatarImage.setImage(avatar));
                }
            }).exceptionally(e -> {
                System.err.println("Failed to load contact avatar: " + e.getMessage());
                Platform.runLater(this::loadDefaultAvatar);
                return null;
            });
        }
    }

    private void loadDefaultAvatar() {
        try {
            avatarImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Client/images/11Devs-white.png"))));
        } catch (Exception e) {
            System.err.println("Failed to load default avatar image.");
        }
    }
}