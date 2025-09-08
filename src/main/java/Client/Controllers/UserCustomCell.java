package Client.Controllers;

import Shared.Models.UserViewModel;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
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
            // *** NEW: Handle placeholder item ***
            if ("SEARCHING_PLACEHOLDER".equals(item.getUserId())) {
                Label searchingLabel = new Label("Searching globally...");
                searchingLabel.getStyleClass().add("searching-placeholder-label");
                searchingLabel.setPrefHeight(60); // Match cell height
                searchingLabel.setAlignment(Pos.CENTER);
                setGraphic(searchingLabel);
            } else {
                // Original logic for real items
                if (currentUser != item) {
                    if (controller != null) {
                        controller.updateCell(item);
                    }
                }
                currentUser = item;
                setGraphic(root);
            }
        }
    }
}