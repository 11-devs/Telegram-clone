// CREATE NEW FILE: main/java/Client/Controllers/AddMembersDialogController.java
package Client.Controllers;
import Client.ContactViewModel;
import Client.Services.ChatService;
import JSocket2.Protocol.Rpc.RpcResponse;
import JSocket2.Protocol.StatusCode;
import Shared.Api.Models.ChatController.CreateGroupInputModel;
import Shared.Api.Models.ContactController.GetContactsOutputModel;
import Shared.Utils.AlertUtil;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class AddMembersDialogController {

    @FXML private VBox dialogRoot;
    @FXML private TextField searchField;
    @FXML private Label selectedCountLabel;
    @FXML private ListView<SelectableContact> contactsListView;
    @FXML private Button backButton;
    @FXML private Button createButton;

    private MainChatController mainChatController;
    private String groupName;
    private String profilePictureId;

    private final ObservableList<SelectableContact> allContacts = FXCollections.observableArrayList();
    private final ObservableList<SelectableContact> selectedContacts = FXCollections.observableArrayList();

    public void init(MainChatController mainChatController, String groupName, String profilePictureId) {
        this.mainChatController = mainChatController;
        this.groupName = groupName;
        this.profilePictureId = profilePictureId;
        setupListView();
        loadContacts();
    }

    private void setupListView() {
        FilteredList<SelectableContact> filteredContacts = new FilteredList<>(allContacts, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredContacts.setPredicate(contact -> {
                if (newVal == null || newVal.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newVal.toLowerCase();
                return contact.getContact().getSavedName().toLowerCase().contains(lowerCaseFilter);
            });
        });

        contactsListView.setItems(filteredContacts);
        contactsListView.setCellFactory(param -> new ContactCell());

        selectedContacts.addListener((ListChangeListener<SelectableContact>) c -> updateSelectedCount());
    }

    private void updateSelectedCount() {
        int count = selectedContacts.size();
        selectedCountLabel.setText(count + (count == 1 ? " member selected" : " members selected"));
    }

    private void loadContacts() {
        ChatService contactService = mainChatController.getChatService();
        var task = contactService.fetchContacts();
        task.setOnSucceeded(event -> {
            RpcResponse<GetContactsOutputModel> response = task.getValue();
            if (response.getStatusCode() == StatusCode.OK && response.getPayload() != null) {
                Platform.runLater(() -> {
                    allContacts.clear();
                    response.getPayload().getContacts().forEach(c -> allContacts.add(new SelectableContact(new ContactViewModel(c))));
                });
            } else {
                Platform.runLater(() -> AlertUtil.showError("Could not load contacts: " + response.getMessage()));
            }
        });
        task.setOnFailed(event -> Platform.runLater(() -> AlertUtil.showError("Could not load contacts: " + task.getException().getMessage())));
        new Thread(task).start();
    }

    @FXML
    private void handleCreate() {
        if (selectedContacts.isEmpty()) {
            AlertUtil.showWarning("No members selected ,Please select at least one member to create a group.");
            return;
        }

        createButton.setDisable(true);
        backButton.setDisable(true);

        List<UUID> memberIds = selectedContacts.stream()
                .map(sc -> sc.getContact().getContactAccountId())
                .collect(Collectors.toList());

        CreateGroupInputModel input = new CreateGroupInputModel();
        input.setTitle(groupName);
        input.setProfilePictureId(profilePictureId);
        input.setMemberIds(memberIds);
        input.setDescription("");

        var task = mainChatController.getChatService().createGroup(input);
        task.setOnSucceeded(event -> Platform.runLater(() -> {
            AlertUtil.showSuccess("Group '" + groupName + "' created successfully.");
            mainChatController.refreshChatList();
            closeDialog();
        }));
        task.setOnFailed(event -> Platform.runLater(() -> {
            AlertUtil.showError("Failed to create group: " + task.getException().getMessage());
            createButton.setDisable(false);
            backButton.setDisable(false);
        }));
        new Thread(task).start();
    }

    @FXML
    private void handleBack() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) dialogRoot.getScene().getWindow();
        stage.close();
    }

    private static class SelectableContact {
        private final ContactViewModel contact;
        private final BooleanProperty selected = new SimpleBooleanProperty(false);

        public SelectableContact(ContactViewModel contact) {
            this.contact = contact;
        }

        public ContactViewModel getContact() {
            return contact;
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }
    }

    private class ContactCell extends ListCell<SelectableContact> {
        private final HBox content;
        private final ImageView avatarView;
        private final Label nameLabel;
        private final CheckBox checkBox;

        public ContactCell() {
            super();
            avatarView = new ImageView();
            avatarView.setFitHeight(40);
            avatarView.setFitWidth(40);
            avatarView.setClip(new Circle(20, 20, 20));

            nameLabel = new Label();
            nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

            checkBox = new CheckBox();

            VBox nameContainer = new VBox(nameLabel);
            nameContainer.setAlignment(Pos.CENTER_LEFT);

            HBox infoBox = new HBox(10, avatarView, nameContainer);
            infoBox.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(nameContainer, Priority.ALWAYS);

            content = new HBox(10, infoBox, new Region(), checkBox);
            HBox.setHgrow(content.getChildren().get(1), Priority.ALWAYS);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setPadding(new Insets(5));
        }

        @Override
        protected void updateItem(SelectableContact item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                nameLabel.setText(item.getContact().getSavedName());
                Image placeholder = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Client/images/default-profile-icon.png")));
                avatarView.setImage(placeholder);

                checkBox.selectedProperty().unbind();
                checkBox.selectedProperty().bindBidirectional(item.selectedProperty());

                item.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                    if (isSelected) {
                        if (!selectedContacts.contains(item)) {
                            selectedContacts.add(item);
                        }
                    } else {
                        selectedContacts.remove(item);
                    }
                });
                setGraphic(content);
            }
        }
    }
}