package Client.Controllers;

import Shared.Models.CountryCode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.stream.Collectors;

/**
 * Controller for the country selection dialog.
 */
public class CountrySelectionController {

    @FXML
    private TextField searchField;

    @FXML
    private ListView<String> countryList;

    @FXML
    private VBox rootVBox;

    private Stage dialogStage;
    private PhoneNumberController parentController;
    private ObservableList<CountryCode> allCountries;

    public void initialize() {
        if (countryList == null) {
            System.err.println("countryList is null in initialize!");
        } else {
            System.out.println("countryList initialized with items: " + (countryList.getItems() != null ? countryList.getItems().size() : 0));
            countryList.setOnMouseClicked((MouseEvent event) -> {
                System.out.println("Mouse clicked on countryList, click count: " + event.getClickCount());
                if (event.getClickCount() == 1) {
                    selectCountry();
                }
            });

            // Adjust height dynamically after layout is computed
            Platform.runLater(this::adjustHeight);
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        System.out.println("Dialog stage set: " + (dialogStage != null));
    }

    public void setParentController(Object parentController) {
        if (parentController instanceof PhoneNumberController) {
            this.parentController = (PhoneNumberController) parentController;
            System.out.println("Parent controller set: " + true);
        } else {
            System.err.println("Parent controller is not an instance of PhoneNumberController");
        }
    }

    public void setData(Object data) {
        if (data instanceof ObservableList<?>) {
            @SuppressWarnings("unchecked")
            ObservableList<CountryCode> countries = (ObservableList<CountryCode>) data;
            this.allCountries = countries;
            updateCountryList();
            System.out.println("All countries set with size: " + (allCountries != null ? allCountries.size() : 0));
            Platform.runLater(this::adjustHeight); // Adjust height after data is set
        } else {
            System.err.println("Data is not an instance of ObservableList<CountryCode>");
        }
    }

    private void adjustHeight() {
        if (countryList != null && searchField != null && rootVBox != null && dialogStage != null) {
            countryList.applyCss();
            double listHeight = countryList.getHeight() > 0 ? countryList.getHeight() + 50 : 250;
            double marginTotal = 20.0;
            double searchHeight = searchField.getHeight() > 0 ? searchField.getHeight() : 30.0;
            double newVBoxHeight = listHeight + marginTotal + searchHeight;
            rootVBox.setPrefHeight(newVBoxHeight);
            dialogStage.sizeToScene();
        } else {
            System.err.println("One of countryList, searchField, rootVBox, or dialogStage is null in adjustHeight!");
        }
    }

    private void updateCountryList() {
        if (allCountries != null && countryList != null) {
            ObservableList<String> countryNames = allCountries.stream()
                    .map(country -> country.getCountry() + " +" + country.getCode().replace("-", ""))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            countryList.setItems(countryNames);
            System.out.println("Updated countryList with " + countryNames.size() + " items");
        } else {
            System.err.println("allCountries or countryList is null in updateCountryList!");
        }
    }

    @FXML
    private void searchCountries(KeyEvent event) {
        if (allCountries != null && countryList != null) {
            String searchText = searchField.getText().toLowerCase();
            ObservableList<String> filteredList = allCountries.stream()
                    .filter(country -> country.getCountry().toLowerCase().contains(searchText) || country.getCode().replace("-", "").contains(searchText))
                    .map(country -> country.getCountry() + " +" + country.getCode().replace("-", ""))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            countryList.setItems(filteredList);
        }
    }

    private void selectCountry() {
        if (countryList != null && parentController != null && dialogStage != null) {
            String selectedItem = countryList.getSelectionModel().getSelectedItem();
            System.out.println("Selected item: " + selectedItem); // Debug log
            if (selectedItem != null) {
                String code = selectedItem.substring(selectedItem.indexOf("+") + 1);
                String countryName = updateCountryName(code);
                System.out.println("Selected code: " + code + ", Country: " + countryName); // Debug log
                parentController.setCountryCode(code, countryName);
                dialogStage.close();
            } else {
                System.out.println("No item selected in countryList");
            }
        } else {
            System.err.println("One of countryList, parentController, or dialogStage is null");
        }
    }

    public String updateCountryName(String code) {
        System.out.println("Entering updateCountryName with code: " + code);
        if (code != null && !code.isEmpty() && allCountries != null) {
            String cleanCode = code.replace("-", "");
            System.out.println("Processing code: " + cleanCode + ", allCountries size: " + allCountries.size());
            String countryName = allCountries.stream()
                    .filter(c -> c.getCode().replace("+", "").replace("-", "").equals(cleanCode))
                    .findFirst()
                    .map(CountryCode::getCountry)
                    .orElse("Invalid Country Code");
            System.out.println("Found country name: " + countryName);
            return countryName;
        }
        return "Country Code";
    }

    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}