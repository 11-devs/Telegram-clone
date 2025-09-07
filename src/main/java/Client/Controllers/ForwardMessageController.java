package Client.Controllers;

import Shared.Models.UserViewModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ForwardMessageController {

    @FXML private TextField searchField;
    @FXML private ListView<UserViewModel> chatsListView;
    @FXML private Button cancelButton;
    @FXML private Button forwardButton;

    private Stage dialogStage;
    private MainChatController parentController;
    private UUID messageIdToForward;
    private final ObservableList<UserViewModel> allChats = FXCollections.observableArrayList();
    private final ObservableList<UserViewModel> filteredChats = FXCollections.observableArrayList();

    public void initialize() {
        // Allow multiple selections
        chatsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // Use the same custom cell as the main chat list
        chatsListView.setCellFactory(lv -> new UserCustomCell());
        chatsListView.setItems(filteredChats);

        // Disable forward button if no chat is selected
        forwardButton.disableProperty().bind(chatsListView.getSelectionModel().selectedItemProperty().isNull());

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterChats(newVal));
    }

    // This method will be called by DialogUtil
    public void setData(Object data) {
        if (data instanceof ForwardingData) {
            ForwardingData fwdData = (ForwardingData) data;
            this.messageIdToForward = fwdData.messageId();
            this.allChats.setAll(fwdData.userList());
            this.filteredChats.setAll(allChats);
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setParentController(Object parentController) {
        if (parentController instanceof MainChatController) {
            this.parentController = (MainChatController) parentController;
        }
    }

    private void filterChats(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredChats.setAll(allChats);
            return;
        }
        String lowerCaseFilter = searchText.toLowerCase().trim();
        List<UserViewModel> searchResult = allChats.stream()
                .filter(user -> user.getDisplayName().toLowerCase().contains(lowerCaseFilter))
                .collect(Collectors.toList());
        filteredChats.setAll(searchResult);
    }

    @FXML
    private void handleForward() {
        ObservableList<UserViewModel> selectedChats = chatsListView.getSelectionModel().getSelectedItems();
        if (!selectedChats.isEmpty() && parentController != null) {
            List<UUID> recipientChatIds = selectedChats.stream()
                    .map(uvm -> UUID.fromString(uvm.getUserId()))
                    .collect(Collectors.toList());

            // Call the forward method in the parent controller
            parentController.executeForwardMessage(messageIdToForward, recipientChatIds);
        }
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    // A record to pass multiple pieces of data easily
    public record ForwardingData(UUID messageId, List<UserViewModel> userList) {}
}