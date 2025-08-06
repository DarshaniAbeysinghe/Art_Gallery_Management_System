package com.example.art_gallery_management_system;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;

public class AddArtFormController {

    @FXML
    private TextField artTitleField;

    @FXML
    private TextField artistField;

    @FXML
    public void handleSubmit() {
        String title = artTitleField.getText();
        String artist = artistField.getText();

        // Validation
        if (title.isEmpty() || artist.isEmpty()) {
            showAlert("Input Error", "Please fill in all fields.");
            return;
        }

        showAlert("Success", "Art titled \"" + title + "\" by " + artist + " has been added.");

        artTitleField.clear();
        artistField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
