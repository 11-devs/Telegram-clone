package Client.Controllers;

import Shared.Api.Models.ContactController.ContactInfo;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;

public class ContactCell extends ListCell<ContactInfo> {
    private ContactCellController controller;
    private GridPane root;

    public ContactCell() {
        try {
            URL resourceUrl = getClass().getResource("/Client/fxml/contactCell.fxml");
            if (resourceUrl == null) {
                throw new IOException("Cannot find FXML file for ContactCell.");
            }
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            root = loader.load();
            controller = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading contactCell.fxml: " + e.getMessage());
        }
    }

    @Override
    protected void updateItem(ContactInfo item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            controller.updateCell(item);
            setGraphic(root);
        }
    }
}