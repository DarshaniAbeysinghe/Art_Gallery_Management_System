package com.example.art_gallery_management_system;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.animation.ScaleTransition;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UserViewController {

    @FXML
    private TilePane artworkTilePane;

    @FXML
    private ComboBox<String> mediumcmb;

    @FXML
    private ComboBox<String> subjectcmb;

    @FXML
    private Button cartButton;

    @FXML
    private TableView<CartItem> cartTable;
    @FXML
    private ImageView recycleimg;


    @FXML
    private TableColumn<CartItem, Integer> idColumn;
    @FXML
    private TableColumn<CartItem, String> nameColumn;
    @FXML
    private TableColumn<CartItem, Integer> quantityColumn;
    @FXML
    private TableColumn<CartItem, Double> priceColumn;
    @FXML
    private TableColumn<CartItem, Button> removeColumn;



    private Connection connection;
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    @FXML
    private TextField totalPriceField;

    @FXML
    public void addToCart(String id, String name, double price) {
        int artworkId = Integer.parseInt(id);


        for (CartItem item : cartItems) {
            if (item.getArtworkId() == artworkId) {
                item.setQuantity(item.getQuantity() + 1);
                item.setPrice(item.getQuantity() * price);
                cartTable.refresh();
                updateTotalPrice();
                return;
            }
        }


        cartItems.add(new CartItem(artworkId, name, price, 1));
        cartTable.setItems(cartItems);
        updateTotalPrice();
    }
    @FXML
    public void clearCart() {
        cartItems.clear();

        cartTable.setItems(cartItems);
        updateTotalPrice();
        System.out.println("Cart has been cleared.");
    }




    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice();
        }
        totalPriceField.setText(String.format("%.2f", total));
    }

    private void applyHoverEffect(ImageView imageView) {

        double originalScaleX = imageView.getScaleX();
        double originalScaleY = imageView.getScaleY();


        imageView.setOnMouseEntered(event -> {

            imageView.setScaleX(originalScaleX * 1.1);
            imageView.setScaleY(originalScaleY * 1.1);
        });


        imageView.setOnMouseExited(event -> {

            imageView.setScaleX(originalScaleX);
            imageView.setScaleY(originalScaleY);
        });
    }

    public void initialize() {
        connection = new DBConnection().getConnection();


        loadMediums();
        loadSubjects();


        loadArtworks("", "");


        cartTable.setItems(cartItems);


        mediumcmb.setOnAction(event -> applyFilters());
        subjectcmb.setOnAction(event -> applyFilters());


        cartButton.setOnMouseClicked(event -> openCart());


        setupCartTable();
    }

    private void setupCartTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("artworkId"));
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        removeColumn.setCellValueFactory(cellData -> cellData.getValue().removeButtonProperty());

        // Customize the remove column to show dustbin image
        removeColumn.setCellFactory(col -> new TableCell<CartItem, Button>() {
            @Override
            protected void updateItem(Button button, boolean empty) {
                super.updateItem(button, empty);
                if (empty || button == null) {
                    setGraphic(null);
                } else {

                    ImageView removeImage = new ImageView(new Image(getClass().getResourceAsStream("/dustbin.png")));
                    removeImage.setFitWidth(20);
                    removeImage.setFitHeight(20);
                    removeImage.setCursor(Cursor.HAND);


                    HBox hbox = new HBox(removeImage);
                    hbox.setAlignment(Pos.CENTER);


                    removeImage.setOnMouseClicked(event -> {
                        CartItem cartItem = getTableRow().getItem();
                        if (cartItem != null) {
                            cartItems.remove(cartItem);
                            cartTable.refresh();
                            updateTotalPrice();
                        }
                    });

                    setGraphic(hbox);
                }
            }
        });
    }









    private void loadMediums() {
        mediumcmb.getItems().addAll("Oil", "Watercolor", "Pastel", "Charcoal");
    }

    private void loadSubjects() {
        subjectcmb.getItems().addAll("Nature", "People", "Animal", "Love");
    }

    private void loadArtworks(String subjectFilter, String mediumFilter) {
        artworkTilePane.getChildren().clear();

        String query = "SELECT AID, image, Qr, Subject, Medium FROM artwork WHERE 1=1";

        if (!subjectFilter.isEmpty()) {
            query += " AND Subject = ?";
        }

        if (!mediumFilter.isEmpty()) {
            query += " AND Medium = ?";
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            int paramIndex = 1;

            if (!subjectFilter.isEmpty()) {
                preparedStatement.setString(paramIndex++, subjectFilter);
            }

            if (!mediumFilter.isEmpty()) {
                preparedStatement.setString(paramIndex++, mediumFilter);
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int artworkId = resultSet.getInt("AID");
                byte[] imageBytes = resultSet.getBytes("image");
                byte[] qrBytes = resultSet.getBytes("Qr");

                Pane artworkPane = createArtworkPane(artworkId, imageBytes, qrBytes);
                artworkTilePane.getChildren().add(artworkPane);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetFilters() {
        artworkTilePane.getChildren().clear(); // Clear the TilePane

        // Reset ComboBoxes to their default states
        mediumcmb.getSelectionModel().clearSelection(); // Clear Medium ComboBox selection
        subjectcmb.getSelectionModel().clearSelection(); // Clear Subject ComboBox selection


        mediumcmb.getItems().clear();
        mediumcmb.getItems().addAll("Oil", "Watercolor", "Pastel", "Charcoal");

        subjectcmb.getItems().clear();
        subjectcmb.getItems().addAll("Nature", "People", "Animal", "Love");


        String query = "SELECT AID, image, Qr, Subject, Medium FROM artwork";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int artworkId = resultSet.getInt("AID");
                byte[] imageBytes = resultSet.getBytes("image");
                byte[] qrBytes = resultSet.getBytes("Qr");

                Pane artworkPane = createArtworkPane(artworkId, imageBytes, qrBytes);
                artworkTilePane.getChildren().add(artworkPane);
            }

            System.out.println("Filters cleared. All artworks are loaded.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error resetting filters: " + e.getMessage());
        }
    }



    public void setupRecycleImgEvent() {
        recycleimg.setOnMouseClicked(event -> {
            resetFilters();
        });
    }


//photo pennanna paintings
    private Pane createArtworkPane(int artworkId, byte[] imageBytes, byte[] qrBytes) {

        VBox artworkItem = new VBox();
        artworkItem.setSpacing(5);
        artworkItem.setStyle("-fx-alignment: center; -fx-padding: 10; -fx-border-color: lightgray; -fx-border-width: 1;");


        ImageView imageView = new ImageView();
        if (imageBytes != null) {
            Image image = new Image(new ByteArrayInputStream(imageBytes));
            imageView.setImage(image);
            imageView.setFitWidth(150);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(true);
        }


        applyHoverEffect(imageView);


        Label idLabel = new Label("ID: " + artworkId);


        ImageView qrView = new ImageView();
        if (qrBytes != null) {
            Image qrImage = new Image(new ByteArrayInputStream(qrBytes));
            qrView.setImage(qrImage);
            qrView.setFitWidth(50);
            qrView.setFitHeight(50);
            qrView.setPreserveRatio(true);


            qrView.setCursor(Cursor.HAND);


            Tooltip tooltip = new Tooltip("Scan QR for details");
            Tooltip.install(qrView, tooltip);


            qrView.setOnMouseClicked(event -> {

                String artworkDetails = getArtworkDetails(artworkId);


                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Artwork Details");
                alert.setHeaderText("Details of Artwork ID: " + artworkId);
                alert.setContentText(artworkDetails);
                alert.showAndWait();
            });
        }


        String[] artworkDetails = getArtworkNameAndPrice(artworkId);
        String name = artworkDetails[0];
        double price = Double.parseDouble(artworkDetails[1]);


        artworkItem.getChildren().addAll(imageView, idLabel, qrView);


        imageView.setOnMouseClicked(event -> {

            Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Add to Cart");
            confirmationAlert.setHeaderText("Do you want to add this artwork to your cart?");
            confirmationAlert.setContentText("Artwork: " + name + "\nPrice: " + price);


            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {

                    addToCart(String.valueOf(artworkId), name, price);
                }
            });
        });

        return artworkItem;
    }



    private String[] getArtworkNameAndPrice(int artworkId) {
        String query = "SELECT Name, Price FROM artwork WHERE AID = ?";
        String[] artworkDetails = new String[2];

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, artworkId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                artworkDetails[0] = resultSet.getString("Name");
                artworkDetails[1] = resultSet.getString("Price");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return artworkDetails;
    }




    private String getArtworkDetails(int artworkId) {

        String query = "SELECT Name, Size, Medium, Subject, Description, Artist, Price FROM artwork WHERE AID = ?";
        String artworkDetails = "";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, artworkId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String name = resultSet.getString("Name");
                String size = resultSet.getString("Size");
                String medium = resultSet.getString("Medium");
                String subject = resultSet.getString("Subject");
                String description = resultSet.getString("Description");
                String artist = resultSet.getString("Artist");
                double price = resultSet.getDouble("Price");


                artworkDetails = String.format(
                        "Name: %s\nSize: %s\nMedium: %s\nSubject: %s\nDescription: %s\nArtist: %s\nPrice: %.2f",
                        name, size, medium, subject, description, artist, price
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return artworkDetails;
    }

    private void addItemToCart(CartItem cartItem) {

        boolean itemExists = false;
        for (CartItem item : cartItems) {
            if (item.getArtworkId() == cartItem.getArtworkId()) {
                item.increaseQuantity();
                itemExists = true;
                break;
            }
        }


        if (!itemExists) {
            cartItems.add(cartItem);
        }


        showAddedToCartAlert();
    }




    private void showAddedToCartAlert() {

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Added to Cart");
        alert.setHeaderText(null);
        alert.setContentText("Artwork has been added to your cart.");
        alert.showAndWait();
    }

    @FXML
    public void openViewsForm() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/art_gallery_management_system/Views.fxml"));
            Parent root = loader.load();


            Scene scene = new Scene(root);


            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("Views");
            newStage.initStyle(StageStyle.UNDECORATED);


            newStage.show();


            newStage.setOnShown(event -> {

                double rootWidth = root.getBoundsInLocal().getWidth();
                double rootHeight = root.getBoundsInLocal().getHeight();


                double screenWidth = newStage.getOwner().getWidth();
                double screenHeight = newStage.getOwner().getHeight();


                double centerX = (screenWidth - rootWidth) / 2;
                double centerY = (screenHeight - rootHeight) / 2;


                newStage.setX(centerX);
                newStage.setY(centerY);
            });


            Stage currentStage = (Stage) artworkTilePane.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private void applyFilters() {
        String selectedMedium = mediumcmb.getValue() != null ? mediumcmb.getValue() : "";
        String selectedSubject = subjectcmb.getValue() != null ? subjectcmb.getValue() : "";
        loadArtworks(selectedSubject, selectedMedium);
    }

    private void openCart() {

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Cart");
        alert.setHeaderText("Your Cart");

        StringBuilder cartContent = new StringBuilder();
        for (CartItem item : cartItems) {
            cartContent.append(item.toString()).append("\n");
        }

        alert.setContentText(cartContent.toString());
        alert.showAndWait();
    }

    private void updateCartTable(CartItem cartItem) {

        for (CartItem item : cartItems) {
            if (item.getArtworkId() == cartItem.getArtworkId()) {

                return;
            }
        }


        cartItems.add(cartItem);


        cartTable.refresh();
    }

    public void handleBuyNowClick(ActionEvent event) {
        // Check if the cart is empty
        if (cartItems.isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Empty Cart");
            alert.setHeaderText(null);
            alert.setContentText("You have not selected any items. Please add items to the cart before proceeding.");
            alert.showAndWait();
        } else {
            // If the cart is not empty,
            try {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("Check.fxml"));
                Parent root = loader.load();


                CheckController checkController = loader.getController();
                if (checkController != null) {

                    checkController.setCartDetails(getCartDetails());
                }


                Scene checkScene = new Scene(root);


                Stage stage = new Stage();
                stage.setScene(checkScene);
                stage.setTitle("Check Out");
                stage.show();


                Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                currentStage.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    @FXML
    public void openRegiForm() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("C.RegiTController.fxml"));

            Parent root = loader.load();


            Stage stage = new Stage();
            stage.setTitle("Registration Form");
            stage.setScene(new Scene(root));


            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading Registration FXML: " + e.getMessage());
        }
    }
    @FXML
    private void openFeedbackForm(MouseEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("feedback.fxml"));
            Parent root = loader.load();


            Stage feedbackStage = new Stage();
            feedbackStage.setTitle("Submit Feedback");
            feedbackStage.setScene(new Scene(root));
            feedbackStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error opening feedback form.");
        }
    }


    private String getCartDetails() {
        StringBuilder cartDetails = new StringBuilder();

        for (CartItem item : cartItems) {

            cartDetails.append("Item ID: ").append(item.getArtworkId())   // Artwork ID
                    .append("\nName: ").append(item.getName())            // Name
                    .append("\nQuantity: ").append(item.getQuantity())    // Quantity
                    .append("\nPrice: ").append(item.getPrice())          // Price
                    .append("\n------------------------\n");
        }

        return cartDetails.toString();
    }

}

