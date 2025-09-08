package Client.Controllers;

import Client.ContactViewModel;
import Client.Services.ChatService;
import Client.Services.FileDownloadService;
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
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class AddMembersDialogController {

    // --- FXML Bindings for the new UI ---
    @FXML private VBox dialogRoot;
    @FXML private TextField searchField;
    @FXML private Label selectedCountLabel;
    @FXML private ListView<SelectableContact> contactsListView;
    @FXML private Button backButton;
    @FXML private Button createButton;
    @FXML private ScrollPane selectedMembersScrollPane;
    @FXML private HBox selectedMembersHBox;
    @FXML private Separator listSeparator;
    @FXML private VBox emptyStateContainer; // ADDED: FXML binding for the empty state
    @FXML private VBox selectedMembersContainer;  // Add this new binding
    @FXML private Label selectedMembersCount;     // Add this new binding

    // --- Class members ---
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
                if (newVal == null || newVal.isEmpty()) return true;
                String lowerCaseFilter = newVal.toLowerCase();
                return contact.getContact().getSavedName().toLowerCase().contains(lowerCaseFilter);
            });
            // Update empty state visibility when search results change
            updateEmptyStateVisibility(filteredContacts.isEmpty());
        });

        contactsListView.setItems(filteredContacts);
        contactsListView.setCellFactory(param -> new ContactCell());

        selectedContacts.addListener((ListChangeListener<SelectableContact>) c -> {
            updateSelectedCount();
            updateSelectedMembersPane();
        });
    }

    private void updateSelectedCount() {
        int count = selectedContacts.size();
        selectedCountLabel.setText(count + " / 200,000");

        // Update the create button state
        if (createButton != null) {
            createButton.setDisable(count == 0);
        }
    }

    private void updateSelectedMembersPane() {
        boolean hasSelections = !selectedContacts.isEmpty();

        // Update the container visibility (this was missing!)
        selectedMembersContainer.setVisible(hasSelections);
        selectedMembersContainer.setManaged(hasSelections);

        // Update the scroll pane visibility (existing code)
        selectedMembersScrollPane.setVisible(hasSelections);
        selectedMembersScrollPane.setManaged(hasSelections);

        // Update separator visibility (existing code)
        listSeparator.setVisible(hasSelections);
        listSeparator.setManaged(hasSelections);

        // Update the selected members count label
        if (selectedMembersCount != null) {
            selectedMembersCount.setText(selectedContacts.size() + " selected");
        }

        // Clear and rebuild the member chips (existing code)
        selectedMembersHBox.getChildren().clear();
        for (SelectableContact selected : selectedContacts) {
            selectedMembersHBox.getChildren().add(createMemberChip(selected));
        }
    }


    private HBox createMemberChip(SelectableContact contact) {
        ImageView avatarView = new ImageView();
        avatarView.setFitHeight(28);
        avatarView.setFitWidth(28);
        avatarView.setClip(new Circle(14, 14, 14));

        Image placeholder = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Client/images/11Devs-white.png")));
        avatarView.setImage(placeholder);

        if (contact.getContact().getProfilePictureId() != null && !contact.getContact().getProfilePictureId().isEmpty()) {
            FileDownloadService.getInstance().getImage(contact.getContact().getProfilePictureId()).thenAccept(img -> {
                if (img != null) Platform.runLater(() -> avatarView.setImage(img));
            });
        }

        Label nameLabel = new Label(contact.getContact().getSavedName().split(" ")[0]); // Show first name
        nameLabel.getStyleClass().add("selected-member-name");

        Button removeButton = new Button();
        SVGPath crossIcon = new SVGPath();
        crossIcon.setContent("M6 18L18 6M6 6l12 12");
        crossIcon.setStyle("-fx-stroke: white; -fx-stroke-width: 2;");
        removeButton.setGraphic(crossIcon);
        removeButton.getStyleClass().add("remove-member-button");
        removeButton.setOnAction(e -> contact.selectedProperty().set(false)); // This triggers removal via binding

        HBox chip = new HBox(avatarView, nameLabel, removeButton);
        chip.getStyleClass().add("selected-member-chip");
        chip.setAlignment(Pos.CENTER);
        return chip;
    }

    private void loadContacts() {
        var task = mainChatController.getChatService().fetchContacts();
        task.setOnSucceeded(event -> {
            RpcResponse<GetContactsOutputModel> response = task.getValue();
            if (response.getStatusCode() == StatusCode.OK && response.getPayload() != null) {
                Platform.runLater(() -> {
                    allContacts.clear();
                    if (response.getPayload().getContacts().isEmpty()) {
                        updateEmptyStateVisibility(true);
                    } else {
                        response.getPayload().getContacts().forEach(c -> allContacts.add(new SelectableContact(new ContactViewModel(c))));
                        updateEmptyStateVisibility(false);
                    }
                });
            } else {
                Platform.runLater(() -> {
                    AlertUtil.showError("Could not load contacts: " + response.getMessage());
                    updateEmptyStateVisibility(true, "Failed to load contacts.", response.getMessage());
                });
            }
        });
        task.setOnFailed(event -> Platform.runLater(() -> {
            AlertUtil.showError("Could not load contacts: " + task.getException().getMessage());
            updateEmptyStateVisibility(true, "Failed to load contacts.", task.getException().getMessage());
        }));
        new Thread(task).start();
    }

    /**
     * Helper method to toggle visibility of the contacts list and empty state container.
     * @param isEmpty True if the contact list is empty.
     */
    private void updateEmptyStateVisibility(boolean isEmpty) {
        updateEmptyStateVisibility(isEmpty, "No contacts found", "Add new contacts to start a group chat.");
    }

    /**
     * Overload to show a custom message on the empty state.
     * @param isEmpty True if the contact list is empty.
     * @param title Custom title for the empty state.
     * @param subtitle Custom subtitle for the empty state.
     */
    private void updateEmptyStateVisibility(boolean isEmpty, String title, String subtitle) {
        contactsListView.setVisible(!isEmpty);
        contactsListView.setManaged(!isEmpty);
        emptyStateContainer.setVisible(isEmpty);
        emptyStateContainer.setManaged(isEmpty);
        if (isEmpty) {
            if (emptyStateContainer.getChildren().size() >= 2 && emptyStateContainer.getChildren().get(1) instanceof Label) {
                ((Label)emptyStateContainer.getChildren().get(1)).setText(title);
            }
            if (emptyStateContainer.getChildren().size() >= 3 && emptyStateContainer.getChildren().get(2) instanceof Label) {
                ((Label)emptyStateContainer.getChildren().get(2)).setText(subtitle);
            }
        }
    }


    @FXML
    private void handleCreate() {
        if (selectedContacts.isEmpty()) {
            AlertUtil.showWarning("Please select at least one member to create a group.");
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
            mainChatController.loadUserChatsFromServer(); // Refresh the list with new data
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

        public SelectableContact(ContactViewModel contact) { this.contact = contact; }
        public ContactViewModel getContact() { return contact; }
        public BooleanProperty selectedProperty() { return selected; }
    }

    private class ContactCell extends ListCell<SelectableContact> {
        private final HBox content;
        private final ImageView avatarView;
        private final Label nameLabel;
        private final CheckBox checkBox;
        private SelectableContact currentItem;

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
            content.getStyleClass().add("contact-list-cell");

            // FIX: The listener logic is now tied to the item's property, not the checkbox itself.
            // This is cleaner and handles changes from any source.
        }

        @Override
        protected void updateItem(SelectableContact item, boolean empty) {
            super.updateItem(item, empty);

            // Clean up listener and binding from the previous item
            if (currentItem != null) {
                checkBox.selectedProperty().unbindBidirectional(currentItem.selectedProperty());
                currentItem.selectedProperty().removeListener(this::selectionChanged);
            }

            currentItem = item;

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                nameLabel.setText(item.getContact().getSavedName());
                loadAvatar(item.getContact().getProfilePictureId());

                checkBox.selectedProperty().bindBidirectional(item.selectedProperty());
                item.selectedProperty().addListener(this::selectionChanged);

                setGraphic(content);
            }
        }

        private void selectionChanged(javafx.beans.value.ObservableValue<? extends Boolean> obs, Boolean wasSelected, Boolean isSelected) {
            if (currentItem == null) return;
            if (isSelected) {
                if (!selectedContacts.contains(currentItem)) {
                    selectedContacts.add(currentItem);
                }
            } else {
                selectedContacts.remove(currentItem);
            }
        }

        private void loadAvatar(String pictureId) {
            Image placeholder = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Client/images/11Devs-white.png")));
            avatarView.setImage(placeholder);
            if (pictureId != null && !pictureId.isEmpty()) {
                FileDownloadService.getInstance().getImage(pictureId).thenAccept(avatar -> {
                    if (avatar != null && currentItem != null && pictureId.equals(currentItem.getContact().getProfilePictureId())) {
                        Platform.runLater(() -> avatarView.setImage(avatar));
                    }
                });
            }
        }
    }
}