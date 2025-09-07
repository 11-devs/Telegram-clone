package Client.Controllers;

import Client.AppConnectionManager;
import Client.RpcCaller;
import Client.Services.ChatService;
import Client.Services.ContactService;
import JSocket2.Protocol.Rpc.RpcResponse;
import JSocket2.Protocol.StatusCode;
import Shared.Api.Models.ContactController.AddContactOutputModel;
import Shared.Api.Models.ContactController.ContactInfo;
import Shared.Api.Models.ContactController.GetContactsOutputModel;
import Shared.Utils.AlertUtil;
import Shared.Utils.SceneUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.UUID;

public class ContactsSectionController {

    @FXML private VBox root;
    @FXML private TextField searchContactsField;
    @FXML private ListView<ContactInfo> contactsListView;

    private Stage dialogStage;
    private Object parentController; // Can be SidebarMenuController or another controller
    private ChatService chatService;
    private ObservableList<ContactInfo> allContacts = FXCollections.observableArrayList();
    private FilteredList<ContactInfo> filteredContacts;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setParentController(Object parentController) {
        this.parentController = parentController;
    }

    public void setData(Object data) {}

    @FXML
    private void initialize() {
        RpcCaller rpcCaller = AppConnectionManager.getInstance().getRpcCaller();
        chatService = new ChatService(rpcCaller);

        filteredContacts = new FilteredList<>(allContacts, p -> true);
        contactsListView.setItems(filteredContacts);
        contactsListView.setCellFactory(listView -> new ContactCell());

        searchContactsField.textProperty().addListener((obs, oldVal, newVal) -> searchContacts(newVal));

        loadContacts();
    }

    private void loadContacts() {
        Task<RpcResponse<GetContactsOutputModel>> fetchTask = chatService.fetchContacts();
        fetchTask.setOnSucceeded(e -> {
            RpcResponse<GetContactsOutputModel> response = fetchTask.getValue();
            if (response.getStatusCode() == StatusCode.OK && response.getPayload() != null) {
                Platform.runLater(() -> {
                    allContacts.setAll(response.getPayload().getContacts());
                });
            } else {
                Platform.runLater(() -> AlertUtil.showError("Failed to load contacts: " + response.getMessage()));
            }
        });
        fetchTask.setOnFailed(e -> {
            Platform.runLater(() -> AlertUtil.showError("An error occurred while fetching contacts."));
            fetchTask.getException().printStackTrace();
        });
        new Thread(fetchTask).start();
    }

    private void searchContacts(String searchText) {
        String lowerCaseFilter = searchText.toLowerCase().trim();
        filteredContacts.setPredicate(contact -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            String fullName = (contact.getFirstName() + " " + contact.getLastName()).toLowerCase();
            return fullName.contains(lowerCaseFilter) || (contact.getUsername() != null && contact.getUsername().toLowerCase().contains(lowerCaseFilter));
        });
    }

    @FXML
    private void handleNewContact() {
        try {
            Stage newContactDialog = SceneUtil.createDialog(
                    "/Client/fxml/newContactDialog.fxml",
                    this.dialogStage,
                    this,
                    null,
                    "New ContactViewModel"
            );
            newContactDialog.showAndWait();
            // The dialog controller will call the addContact method below.
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Could not open the new contact dialog.");
        }
    }
    
    public void addContact(String firstName, String lastName, String phoneNumber) {
        Task<RpcResponse<AddContactOutputModel>> addTask = chatService.addContact(firstName, lastName, phoneNumber);
        addTask.setOnSucceeded(e -> {
            RpcResponse<AddContactOutputModel> response = addTask.getValue();
            if (response.getStatusCode() == StatusCode.OK) {
                Platform.runLater(() -> {
                    AlertUtil.showSuccess("ContactViewModel added successfully!");
                    loadContacts(); // Refresh the list
                });
            } else {
                Platform.runLater(() -> AlertUtil.showError("Failed to add contact: " + response.getMessage()));
            }
        });
        addTask.setOnFailed(e -> {
            Platform.runLater(() -> AlertUtil.showError("An error occurred while adding the contact."));
            addTask.getException().printStackTrace();
        });
        new Thread(addTask).start();
    }
    
    @FXML
    private void handleBack() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}