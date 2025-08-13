package Client.Controllers;

import Shared.Models.CountryCode;
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

public class CountrySelectionController {
    @FXML
    TextField searchField;
    @FXML
    ListView<String> countryList;
    @FXML
    VBox rootVBox;

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
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setParentController(PhoneNumberController parentController) {
        this.parentController = parentController;
    }

    public void setAllCountries(ObservableList<CountryCode> allCountries) {
        this.allCountries = allCountries;
        updateCountryList();
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