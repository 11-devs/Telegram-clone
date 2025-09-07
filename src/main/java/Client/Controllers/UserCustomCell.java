package Client.Controllers;

import Shared.Models.UserViewModel;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import java.io.IOException;

public class UserCustomCell extends ListCell<UserViewModel> {

    private UserCustomCellController controller;
    private GridPane root;
    private UserViewModel currentUser; // TODO: Should be connected to the DAO

    public UserCustomCell() {
        try {
            java.net.URL resourceUrl = getClass().getResource("/Client/fxml/userCustomCell.fxml");
            if (resourceUrl == null) {
                System.err.println("FXML not found at /Client/fxml/userCustomCell.fxml. Checking classloader...");
                resourceUrl = getClass().getClassLoader().getResource("Client/fxml/userCustomCell.fxml");
                if (resourceUrl == null) {
                    System.err.println("FXML resource not found. Verify resources folder structure.");
                    return; // Exit gracefully if FXML is not found
                }
            }
            System.out.println("Loading FXML from: " + resourceUrl);
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            root = loader.load();
            controller = loader.getController();
            if (controller == null) {
                System.err.println("Controller not loaded from FXML.");
            }
            setGraphic(root); // Basic graphics setting
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading FXML file: " + e.getMessage());
        }
    }

    @Override
    protected void updateItem(UserViewModel item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            // When a cell is cleared, make sure to unbind any listeners
            // to prevent memory leaks and incorrect updates.
            if (controller != null && currentUser != null) {
                controller.unbindAll();
            }
            currentUser = null;
        } else {
            // Key optimization: only run the expensive update logic if the UserViewModel has changed.
            // This prevents reloading images and rebinding listeners when scrolling.
            if (currentUser != item) {
                if (controller != null) {
                    controller.updateCell(item);
                }
            }
            // It's important to set the currentUser and graphic outside the if-block.
            // `currentUser` must be up-to-date for the next `updateItem` call.
            // `setGraphic` must be called to ensure the cell's content is visible,
            // especially when it's being reused.
            currentUser = item;
            setGraphic(root);
        }
    }
}