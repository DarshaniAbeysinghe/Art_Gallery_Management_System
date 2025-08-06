package com.example.art_gallery_management_system;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;

public class ArtistRegistrationController {


    @FXML
    private TextField nicField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField tpNoField;
    @FXML
    private TextField searchtxt;
    @FXML
    private Button searchBtn;
    @FXML
    private ToggleGroup genderGroup;

    @FXML
    private RadioButton maleRadioButton;
    @FXML
    private RadioButton femaleRadioButton;
    @FXML
    private TextArea addressField;

    @FXML
    private ImageView imgphoto;


    @FXML
    private TableColumn<Artist, String> idColumn;
    @FXML
    private TableView<Artist> artistTable;
    @FXML
    private TableColumn<Artist, String> nicColumn;
    @FXML
    private TableColumn<Artist, String> firstNameColumn;
    @FXML
    private TableColumn<Artist, String> lastNameColumn;
    @FXML
    private TableColumn<Artist, String> emailColumn;
    @FXML
    private TableColumn<Artist, String> tpNoColumn;
    @FXML
    private TableColumn<Artist, String> genderColumn;
    @FXML
    private TableColumn<Artist, String> addressColumn;
    @FXML
    private TableColumn<Artist, String> photoColumn;


    @FXML
    private Button addImageButton;


    private Connection connection;
    private ObservableList<Artist> artistList = FXCollections.observableArrayList();
    private File imageFile;


    @FXML
    private void initialize() {

        genderGroup = new ToggleGroup();
        maleRadioButton.setToggleGroup(genderGroup);
        femaleRadioButton.setToggleGroup(genderGroup);
        //table ekata data gannawa
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        nicColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNic()));
        firstNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));
        lastNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        tpNoColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTpNo()));
        genderColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGender()));
        addressColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAddress()));
        photoColumn.setCellValueFactory(cellData -> new SimpleStringProperty("Image"));


        DBConnection connectNow = new DBConnection();
        connection = connectNow.getConnection();


        loadArtistData();
        artistTable.setItems(artistList);

        searchtxt.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {

                artistTable.setItems(artistList);
            }
        });

    }


    @FXML
    private void addbtnAction(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(addImageButton.getScene().getWindow());

        if (selectedFile != null) {
            imageFile = selectedFile;
            imgphoto.setImage(new javafx.scene.image.Image(selectedFile.toURI().toString()));
        }
    }


    @FXML
    private void registerbtnAction(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }
        String nic = nicField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String tpNoString = tpNoField.getText();
        String gender = maleRadioButton.isSelected() ? "Male" : "Female";
        String address = addressField.getText();

        if (nic.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || tpNoString.isEmpty() || address.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Form Error", "Please fill in all fields.");
        } else {
            try {
                int tpNo = Integer.parseInt(tpNoString);

                String query = "INSERT INTO artist (NIC, firstName, lastName, email, tpNo, gender, address, Photo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, nic);
                stmt.setString(2, firstName);
                stmt.setString(3, lastName);
                stmt.setString(4, email);
                stmt.setInt(5, tpNo);
                stmt.setString(6, gender);
                stmt.setString(7, address);

                if (imageFile != null) {
                    FileInputStream fis = new FileInputStream(imageFile);
                    stmt.setBinaryStream(8, fis, (int) imageFile.length());
                } else {
                    stmt.setNull(8, Types.BLOB);
                }

                int result = stmt.executeUpdate();

                if (result > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Artist registered successfully!");
                    clearForm();
                    loadArtistData();
                }
            } catch (SQLException | java.io.IOException | NumberFormatException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to register artist.");
            }
        }
    }



    private void loadArtistData() {
        try {
            artistList.clear();
            String query = "SELECT * FROM artist";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int id = rs.getInt("ID");
                String nic = rs.getString("NIC");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                String email = rs.getString("email");
                int tpNo = rs.getInt("tpNo");
                String gender = rs.getString("gender");
                String address = rs.getString("address");


                artistList.add(new Artist(id, nic, firstName, lastName, email, String.valueOf(tpNo), gender, address));
            }

            artistTable.setItems(artistList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }




    @FXML
    private void updateArtistAction(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }
        Artist selectedArtist = artistTable.getSelectionModel().getSelectedItem();

        if (selectedArtist == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select an artist to update.");
            return;
        }

        String nic = nicField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String tpNoString = tpNoField.getText();
        String gender = maleRadioButton.isSelected() ? "Male" : "Female";
        String address = addressField.getText();

        if (nic.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || tpNoString.isEmpty() || address.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Form Error", "Please fill in all fields.");
            return;
        }

        try {
            int tpNo = Integer.parseInt(tpNoString);


            String query = "UPDATE artist SET firstName = ?, lastName = ?, email = ?, tpNo = ?, gender = ?, address = ? WHERE NIC = ?";
            PreparedStatement stmt = connection.prepareStatement(query);


            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setInt(4, tpNo);
            stmt.setString(5, gender);
            stmt.setString(6, address);
            stmt.setString(7, nic);

            int result = stmt.executeUpdate();

            if (result > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Update Successful", "Artist details updated.");


                loadArtistData();

                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Error", "Failed to update artist details.");
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update artist details.");
        }
    }







    @FXML
    private void deleteArtistAction(ActionEvent event) {
        Artist selectedArtist = artistTable.getSelectionModel().getSelectedItem();
        if (selectedArtist != null) {
            try {
                String query = "DELETE FROM artist WHERE NIC = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, selectedArtist.getNic());
                int result = stmt.executeUpdate();

                if (result > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Artist deleted successfully!");
                    loadArtistData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Deletion Error", "Failed to delete artist.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete artist.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select an artist to delete.");
        }
    }



    @FXML
    private void searchbtnAction() {
        String searchText = searchtxt.getText().trim();

        if (searchText.isEmpty()) {

            clearForm();
            loadArtistData();
            artistTable.setItems(artistList);
        } else {

            try {
                String query = "SELECT * FROM artist WHERE NIC = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, searchText);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {

                    nicField.setText(rs.getString("NIC"));
                    firstNameField.setText(rs.getString("firstName"));
                    lastNameField.setText(rs.getString("lastName"));
                    emailField.setText(rs.getString("email"));
                    tpNoField.setText(rs.getString("tpNo"));
                    addressField.setText(rs.getString("address"));


                    if ("Male".equals(rs.getString("gender"))) {
                        maleRadioButton.setSelected(true);
                    } else {
                        femaleRadioButton.setSelected(true);
                    }


                    InputStream inputStream = rs.getBinaryStream("Photo");
                    if (inputStream != null) {
                        Image image = new Image(inputStream);
                        imgphoto.setImage(image);
                    }


                    ObservableList<Artist> filteredList = FXCollections.observableArrayList();
                    Artist artist = new Artist(
                            rs.getInt("ID"),
                            rs.getString("NIC"),
                            rs.getString("firstName"),
                            rs.getString("lastName"),
                            rs.getString("email"),
                            rs.getString("tpNo"),
                            rs.getString("gender"),
                            rs.getString("address")
                    );
                    filteredList.add(artist);


                    artistTable.setItems(filteredList);

                } else {
                    showAlert(Alert.AlertType.INFORMATION, "No Results", "No artist found with the given NIC.");

                    clearForm();
                    loadArtistData();
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to fetch artist data.");
            }
        }
    }

    @FXML
    private void clearbtnAction(){
        clearForm();
    }

    private boolean validateInputs() {

        if (nicField.getText().isBlank() || !nicField.getText().matches("\\d{9}[VvXx]|\\d{12}")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid NIC");
            nicField.requestFocus();
            return false;
        }

        if (firstNameField.getText().isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter First Name.");
            firstNameField.requestFocus();
            return false;
        }

        if (lastNameField.getText().isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter Last Name.");
            lastNameField.requestFocus();
            return false;
        }

        if (genderGroup.getSelectedToggle() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select Gender.");
            maleRadioButton.requestFocus();
            return false;
        }

        if (emailField.getText().isBlank() || !emailField.getText().matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid Email address.");
            emailField.requestFocus();
            return false;
        }

        if (addressField.getText().isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter Address.");
            addressField.requestFocus();
            return false;
        }

        if (tpNoField.getText().isBlank() || !tpNoField.getText().matches("\\d{10}")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid 10-digit Telephone Number.");
            tpNoField.requestFocus();
            return false;
        }

        if (imageFile == null) { // Ensure an image is selected
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select an Image.");
            return false;
        }

        return true;
    }


    private void clearForm() {
        nicField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        tpNoField.clear();
        addressField.clear();
        genderGroup.selectToggle(null);
        imgphoto.setImage(null);
        imageFile = null;
    }

}
