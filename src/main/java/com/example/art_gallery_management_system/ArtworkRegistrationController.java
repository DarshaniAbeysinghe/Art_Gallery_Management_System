package com.example.art_gallery_management_system;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import javax.imageio.ImageIO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Optional;


public class ArtworkRegistrationController {
    private Connection connection;
    public ArtworkRegistrationController() {
        // Initialize database connection
        DBConnection dbConnection = new DBConnection();
        connection = dbConnection.getConnection();
    }



    @FXML
    private TextField nametxt, sizetxt, pricetxt;
    @FXML
    private TextArea desarea;
    @FXML
    private ComboBox<String> artistcmb, mediumcmb, subjectcmb;
    @FXML
    private ImageView photoimg, artimg;
    @FXML
    private Button addbtn, addartbtn;
    @FXML
    private TableView<Artwork> artworkTable;
    @FXML
    private TableColumn<Artwork, String> columnName, columnSize, columnMedium, columnSubject;
    @FXML
    private TableColumn<Artwork, Double> columnPrice;
    @FXML
    private TableColumn<Artwork, Void> columnActions;
    @FXML
    private TabPane tabPane;
    @FXML
    private TableColumn<Artwork, Integer> columnArtworkID;
    @FXML
    private TableColumn<Artwork, Integer> columnArtistID;
    @FXML
    private TextField searchtxt;
    @FXML
    private Button deleteArtworkBtn;
    @FXML
    private ImageView imageView;


    @FXML
    private TableColumn<Artwork, String> descriptionColumn;
    @FXML
    private TableColumn<Artwork, String> artistColumn;
    @FXML
    private TableColumn<Artwork, Image> imageColumn;



    private File artworkImageFile;

    @FXML
    public void initialize() {

        loadArtists();
        loadMediums();
        loadSubjects();
        loadArtworkDetails();

        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if ("Art Details".equals(newTab.getText())) {
                loadArtworkDetails();
            }
        });

        columnArtworkID.setCellValueFactory(cellData -> cellData.getValue().artworkIDProperty().asObject());
        columnArtistID.setCellValueFactory(cellData -> cellData.getValue().artistIDProperty().asObject());
        columnName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        columnSize.setCellValueFactory(cellData -> cellData.getValue().sizeProperty());
        columnMedium.setCellValueFactory(cellData -> cellData.getValue().mediumProperty());
        columnSubject.setCellValueFactory(cellData -> cellData.getValue().subjectProperty());
        columnPrice.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        artistColumn.setCellValueFactory(cellData -> cellData.getValue().artistProperty());
        imageColumn.setCellValueFactory(cellData -> cellData.getValue().imageProperty());


        artworkTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                populateForm(newValue);
            }
        });
    }


    private void populateForm(Artwork selectedArtwork) {
        nametxt.setText(selectedArtwork.getName());
        sizetxt.setText(selectedArtwork.getSize());
        pricetxt.setText(String.valueOf(selectedArtwork.getPrice()));
        desarea.setText(selectedArtwork.getDescription());
        artistcmb.setValue(selectedArtwork.getArtist());
        mediumcmb.setValue(selectedArtwork.getMedium());
        subjectcmb.setValue(selectedArtwork.getSubject());
        artimg.setImage(selectedArtwork.getImage());
    }



    public void loadArtworkDetails() {
        ObservableList<Artwork> artworkList = FXCollections.observableArrayList();
        String query = "SELECT a.AID, a.ID, a.Name, a.Size, a.Medium, a.Subject, a.Price, a.Description, a.Artist, a.Image " +
                "FROM artwork a";
        try (Connection connectDB = new DBConnection().getConnection();
             PreparedStatement stmt = connectDB.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                int artworkID = resultSet.getInt("AID");
                int artistID = resultSet.getInt("ID");
                String name = resultSet.getString("Name");
                String size = resultSet.getString("Size");
                String medium = resultSet.getString("Medium");
                String subject = resultSet.getString("Subject");
                double price = resultSet.getDouble("Price");
                String description = resultSet.getString("Description");
                String artist = resultSet.getString("Artist");


                byte[] imageBytes = resultSet.getBytes("Image");
                Image image = (imageBytes != null) ? new Image(new ByteArrayInputStream(imageBytes)) : null;


                artworkList.add(new Artwork(artworkID, artistID, name, size, medium, subject, price, description, artist, image));
            }
        } catch (Exception e) {
            showAlert("Error", "Unable to load artwork data: " + e.getMessage());
        }


        artworkTable.setItems(artworkList);
    }








    private void loadArtists() {
        String query = "SELECT ID, CONCAT(FirstName, ' ', LastName) AS FullName FROM artist";
        try (Connection connectDB = new DBConnection().getConnection();
             PreparedStatement stmt = connectDB.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {


            while (resultSet.next()) {
                artistcmb.getItems().add(resultSet.getString("FullName"));
            }
        } catch (Exception e) {
            showAlert("Error", "Unable to load artists: " + e.getMessage());
        }
    }

    private void loadMediums() {
        mediumcmb.getItems().addAll("Oil", "Watercolor", "Pastel", "Charcoal");
    }

    private void loadSubjects() {
        subjectcmb.getItems().addAll("Nature", "People", "Animal", "Love");
    }

    @FXML
    public void addArtworkImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("D:\\javaa\\"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        artworkImageFile = fileChooser.showOpenDialog(addartbtn.getScene().getWindow());

        if (artworkImageFile != null) {
            Image artworkImage = new Image(artworkImageFile.toURI().toString());
            artimg.setImage(artworkImage);
            showAlert("Artwork Image Loaded", "Artwork image loaded successfully from: " + artworkImageFile.getPath());
        } else {
            showAlert("Error", "No artwork image selected.");
        }
    }

    @FXML
    public void registerArtwork(ActionEvent event) {
        if (validateInputs()) {
            saveArtwork();
        }
    }

    private boolean validateInputs() {
        if (nametxt.getText().isBlank()) {
            showAlert("Validation Error", "Please input Name");
            nametxt.requestFocus();
            return false;
        }
        if (sizetxt.getText().isBlank()) {
            showAlert("Validation Error", "Please input Size");
            sizetxt.requestFocus();
            return false;
        }
        if (mediumcmb.getValue() == null) {
            showAlert("Validation Error", "Please select a Medium");
            mediumcmb.requestFocus();
            return false;
        }
        if (subjectcmb.getValue() == null) {
            showAlert("Validation Error", "Please select a Subject");
            subjectcmb.requestFocus();
            return false;
        }
        if (desarea.getText().isBlank()) {
            showAlert("Validation Error", "Please enter a Description");
            desarea.requestFocus();
            return false;
        }
        if (pricetxt.getText().isBlank() || !pricetxt.getText().matches("\\d+(\\.\\d{1,2})?")) {
            showAlert("Validation Error", "Please enter a valid Price (up to two decimal places)");
            pricetxt.requestFocus();
            return false;
        }
        if (artistcmb.getValue() == null) {
            showAlert("Validation Error", "Please select an Artist");
            artistcmb.requestFocus();
            return false;
        }




        return true;
    }

    private void saveArtwork() {
        int artistID = getArtistID(artistcmb.getValue());
        if (artistID == -1) {
            showAlert("Error", "Invalid Artist selected.");
            return;
        }

        String query = "INSERT INTO artwork (ID, Name, Size, Medium, Subject, Description, Artist, Price, image, Qr) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connectDB = new DBConnection().getConnection();
             PreparedStatement stmt = connectDB.prepareStatement(query)) {

            stmt.setInt(1, artistID);
            stmt.setString(2, nametxt.getText());
            stmt.setString(3, sizetxt.getText());
            stmt.setString(4, mediumcmb.getValue());
            stmt.setString(5, subjectcmb.getValue());
            stmt.setString(6, desarea.getText());
            stmt.setString(7, artistcmb.getValue());
            stmt.setDouble(8, Double.parseDouble(pricetxt.getText()));


            if (artworkImageFile != null && artworkImageFile.exists()) {
                try (FileInputStream fis = new FileInputStream(artworkImageFile);
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    stmt.setBytes(9, baos.toByteArray());
                } catch (IOException e) {
                    showAlert("Error", "Error reading artwork image: " + e.getMessage());
                    e.printStackTrace();
                    return;
                }
            } else {
                stmt.setNull(9, java.sql.Types.BLOB);
            }


            String qrData = nametxt.getText() + " | Price: " + pricetxt.getText();
            byte[] qrCodeImage = generateQRCode(qrData);
            stmt.setBytes(10, qrCodeImage);

            stmt.executeUpdate();
            showAlert("Success", "Artwork added successfully.");
            clearFields();


            loadArtworkDetails();

        } catch (Exception e) {
            showAlert("Error", "Error adding artwork: " + e.getMessage());
            e.printStackTrace();
        }
    }




    private byte[] generateQRCode(String data) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 250, 250);
        BufferedImage qrImage = new BufferedImage(250, 250, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 250; x++) {
            for (int y = 0; y < 250; y++) {
                qrImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "png", baos);
        return baos.toByteArray();
    }

    private int getArtistID(String artistName) {
        String query = "SELECT ID FROM artist WHERE CONCAT(FirstName, ' ', LastName) = ?";
        try (Connection connectDB = new DBConnection().getConnection();
             PreparedStatement stmt = connectDB.prepareStatement(query)) {
            stmt.setString(1, artistName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID");
            }
        } catch (Exception e) {
            showAlert("Error", "Error fetching artist ID: " + e.getMessage());
        }
        return -1;
    }



    @FXML
    private void searchArtworkAction() {
        String searchText = searchtxt.getText().trim();

        if (searchText.isEmpty()) {
            clearFields();
            loadArtworkDetails();
        } else {
            ObservableList<Artwork> filteredList = FXCollections.observableArrayList();
            String query = "SELECT a.AID, a.ID, a.Name, a.Size, a.Medium, a.Subject, a.Price, a.Description, a.Artist, a.Image " +
                    "FROM artwork a WHERE a.AID LIKE ?";

            try (Connection connectDB = new DBConnection().getConnection();
                 PreparedStatement stmt = connectDB.prepareStatement(query)) {

                stmt.setString(1, "%" + searchText + "%");
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    int artworkID = rs.getInt("AID");
                    int artistID = rs.getInt("ID");
                    String name = rs.getString("Name");
                    String size = rs.getString("Size");
                    String medium = rs.getString("Medium");
                    String subject = rs.getString("Subject");
                    double price = rs.getDouble("Price");
                    String description = rs.getString("Description");
                    String artist = rs.getString("Artist");

                    byte[] imageBytes = rs.getBytes("Image");
                    Image image = (imageBytes != null) ? new Image(new ByteArrayInputStream(imageBytes)) : null;

                    filteredList.add(new Artwork(artworkID, artistID, name, size, medium, subject, price, description, artist, image));
                }

                if (filteredList.isEmpty()) {
                    showAlert("No Results", "No artwork found with the given criteria.");
                } else {
                    artworkTable.setItems(filteredList);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Database Error", "Failed to fetch artwork data: " + e.getMessage());
            }
        }
    }

    @FXML
    private void deleteSelectedArtwork() {

        Artwork selectedArtwork = artworkTable.getSelectionModel().getSelectedItem();

        if (selectedArtwork != null) {

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Delete Confirmation");
            confirmation.setHeaderText("Are you sure you want to delete this artwork?");
            confirmation.setContentText("Artwork: " + selectedArtwork.getName());

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {

                deleteArtworkFromDatabase(selectedArtwork.getArtworkID());


                artworkTable.getItems().remove(selectedArtwork);
            }
        } else {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Artwork Selected");
            alert.setContentText("Please select an artwork to delete.");
            alert.showAndWait();
        }
    }

    private void deleteArtworkFromDatabase(int artworkID) {
        String query = "DELETE FROM artwork WHERE AID = ?";

        try (Connection connectDB = new DBConnection().getConnection();
             PreparedStatement statement = connectDB.prepareStatement(query)) {

            statement.setInt(1, artworkID);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Could not delete artwork");
            alert.setContentText("An error occurred while trying to delete the artwork: " + e.getMessage());
            alert.showAndWait();
        }
    }


    @FXML
    private void updateArtwork() {

        Artwork selectedArtwork = artworkTable.getSelectionModel().getSelectedItem();

        if (selectedArtwork == null) {
            showAlert("No Selection", "Please select an artwork to update.");
            return;
        }


        if (!validateInputs()) {
            return;
        }

        String query = "UPDATE artwork SET Name = ?, Size = ?, Medium = ?, Subject = ?, Description = ?, Artist = ?, Price = ?, Image = ? WHERE AID = ?";

        try (Connection connectDB = new DBConnection().getConnection();
             PreparedStatement stmt = connectDB.prepareStatement(query)) {


            stmt.setString(1, nametxt.getText());
            stmt.setString(2, sizetxt.getText());
            stmt.setString(3, mediumcmb.getValue());
            stmt.setString(4, subjectcmb.getValue());
            stmt.setString(5, desarea.getText());
            stmt.setString(6, artistcmb.getValue());
            stmt.setDouble(7, Double.parseDouble(pricetxt.getText()));


            if (artworkImageFile != null && artworkImageFile.exists()) {
                try (FileInputStream fis = new FileInputStream(artworkImageFile);
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    stmt.setBytes(8, baos.toByteArray());
                } catch (IOException e) {
                    showAlert("Error", "Error reading artwork image: " + e.getMessage());
                    e.printStackTrace();
                    return;
                }
            } else {
                stmt.setNull(8, java.sql.Types.BLOB); // If no image is provided
            }

            // Set the artwork ID for the WHERE clause
            stmt.setInt(9, selectedArtwork.getArtworkID());

            // Execute the update
            stmt.executeUpdate();

            showAlert("Success", "Artwork updated successfully.");

            // Reload the artwork list
            loadArtworkDetails();
            clearFields();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Error updating artwork: " + e.getMessage());
        }
    }




    private void clearFields() {
        nametxt.clear();
        sizetxt.clear();
        pricetxt.clear();
        desarea.clear();
        artistcmb.getSelectionModel().clearSelection();
        mediumcmb.getSelectionModel().clearSelection();
        subjectcmb.getSelectionModel().clearSelection();
        artimg.setImage(null);
        artworkImageFile = null;
    }

    @FXML
    private void clearbtnAction() {
        nametxt.clear();
        sizetxt.clear();
        pricetxt.clear();
        desarea.clear();
        artistcmb.getSelectionModel().clearSelection();
        mediumcmb.getSelectionModel().clearSelection();
        subjectcmb.getSelectionModel().clearSelection();
        artimg.setImage(null);
        artworkImageFile = null;
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

