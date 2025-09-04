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
            currentUser = null;
        } else {
            currentUser = item; // Storage within the cell itself
            if (controller != null) {
                controller.setCurrentUser(item); // Transfer to controller
                controller.updateCell(item); // Display update
                setGraphic(root);
            } else {
                System.err.println("Controller is null, skipping updateCell.");
            }
        }
    }
}
