package com.example.art_gallery_management_system;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType; // Add this import for AlertType


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardController {

    @FXML
    private AnchorPane centerPane;
    @FXML
    private ImageView yourImageView;
    @FXML
    private AnchorPane homeanchor;
    @FXML
    private TextField txtOrderId;
    @FXML
    private ImageView searchimg;



    @FXML
    public void ticketIssueAction() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/art_gallery_management_system/TicketIssue.fxml"));
            AnchorPane ticketIssueForm = loader.load();
            centerPane.getChildren().clear();
            centerPane.getChildren().add(ticketIssueForm);


            double centerWidth = centerPane.getWidth();
            double centerHeight = centerPane.getHeight();
            double formWidth = ticketIssueForm.getPrefWidth();
            double formHeight = ticketIssueForm.getPrefHeight();


            AnchorPane.setTopAnchor(ticketIssueForm, (centerHeight - formHeight) / 2);
            AnchorPane.setLeftAnchor(ticketIssueForm, (centerWidth - formWidth) / 2);
            AnchorPane.setRightAnchor(ticketIssueForm, (centerWidth - formWidth) / 2);
            AnchorPane.setBottomAnchor(ticketIssueForm, (centerHeight - formHeight) / 2);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void manageartistbtnAction() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/art_gallery_management_system/ArtistRegistration.fxml"));
            AnchorPane artistRegistrationForm = loader.load();

            centerPane.getChildren().clear();
            centerPane.getChildren().add(artistRegistrationForm);


            double centerWidth = centerPane.getWidth();
            double centerHeight = centerPane.getHeight();
            double formWidth = artistRegistrationForm.getPrefWidth();
            double formHeight = artistRegistrationForm.getPrefHeight();


            AnchorPane.setTopAnchor(artistRegistrationForm, (centerHeight - formHeight) / 2);
            AnchorPane.setLeftAnchor(artistRegistrationForm, (centerWidth - formWidth) / 2);
            AnchorPane.setRightAnchor(artistRegistrationForm, (centerWidth - formWidth) / 2);
            AnchorPane.setBottomAnchor(artistRegistrationForm, (centerHeight - formHeight) / 2);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void artworkRegistrationAction() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/art_gallery_management_system/ArtworkRegistration.fxml"));
            AnchorPane artworkRegistrationForm = loader.load();

            centerPane.getChildren().clear();
            centerPane.getChildren().add(artworkRegistrationForm);


            double centerWidth = centerPane.getWidth();
            double centerHeight = centerPane.getHeight();
            double formWidth = artworkRegistrationForm.getPrefWidth();
            double formHeight = artworkRegistrationForm.getPrefHeight();


            AnchorPane.setTopAnchor(artworkRegistrationForm, (centerHeight - formHeight) / 2);
            AnchorPane.setLeftAnchor(artworkRegistrationForm, (centerWidth - formWidth) / 2);
            AnchorPane.setRightAnchor(artworkRegistrationForm, (centerWidth - formWidth) / 2);
            AnchorPane.setBottomAnchor(artworkRegistrationForm, (centerHeight - formHeight) / 2);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void openViewsForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/art_gallery_management_system/Views.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) yourImageView.getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);


            stage.sizeToScene();


            javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
            javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();


            double width = bounds.getWidth();
            double height = bounds.getHeight();
            double stageWidth = stage.getWidth();
            double stageHeight = stage.getHeight();


            stage.setX((width - stageWidth) / 2);
            stage.setY((height - stageHeight) / 2);


            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML


    public void btnhomeAction() {
        try {

            centerPane.getChildren().clear();


            centerPane.getChildren().add(homeanchor);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }










    @FXML
    private void handleSearchAction() {
        String orderId = txtOrderId.getText().trim();

        if (orderId.isEmpty()) {
            showAlert("Error", "Order ID field cannot be empty!", Alert.AlertType.ERROR);
            return;
        }


        System.out.println("Searching for OrderID: " + orderId);


        String query = "SELECT * FROM `order` WHERE OrderID = ?";

        try (Connection conn = new DBConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String status = rs.getString("status");
                System.out.println("Order found: " + rs.getString("OrderID"));

                if ("placed".equalsIgnoreCase(status)) {

                    showAlert("Order Status", "This order has already been placed and cannot be reopened.", Alert.AlertType.INFORMATION);
                } else {

                    openOrderPage(orderId);
                }
            } else {
                System.out.println("No order found for OrderID: " + orderId);
                showAlert("No Orders", "No order found for the provided Order ID!", Alert.AlertType.INFORMATION);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while checking the order. Please try again.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred. Please try again.", Alert.AlertType.ERROR);
        }
        txtOrderId.clear();
    }




    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



//search karata passe order eka pennanna
    private void openOrderPage(String orderId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Order.fxml"));
            AnchorPane orderPage = loader.load();


            OrderController orderController = loader.getController();


            orderController.loadOrderDetails(orderId);

            Scene orderScene = new Scene(orderPage);


            Stage stage = new Stage();
            stage.setScene(orderScene);


            stage.show();


            double x = (java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth() - stage.getWidth()) / 2;
            double y = (java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight() - stage.getHeight()) / 2;
            stage.setX(x);
            stage.setY(y);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the order page. Please try again.", Alert.AlertType.ERROR);
        }
    }



    @FXML
    public void handlePsbtnAction() {
        loadAndCenterFXML("/com/example/art_gallery_management_system/PaintingSale.fxml");
    }
    @FXML
    public void handleorderAction() {
        loadAndCenterFXML("/com/example/art_gallery_management_system/OrderCRUD.fxml");
    }

    @FXML
    public void handleTsbtnAction() {
        loadAndCenterFXML("/com/example/art_gallery_management_system/TicketChart.fxml");
    }

    @FXML
    public void handlememberAction() {
        loadAndCenterFXML("/com/example/art_gallery_management_system/Member.fxml");
    }
    @FXML
    public void handleSdbtnAction() {
        loadAndCenterFXML("/com/example/art_gallery_management_system/ArtistSale.fxml");
    }

    @FXML
    public void handleCustomerManageBtnAction() {
        loadAndCenterFXML("/com/example/art_gallery_management_system/CustomerRegi.fxml");
    }

    /**
     * Loads an FXML file into the centerPane and centers it.
     * @param fxmlPath The path of the FXML file to load.
     */
    private void loadAndCenterFXML(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane loadedPane = loader.load();


            centerPane.getChildren().clear();


            centerPane.getChildren().add(loadedPane);


            double centerWidth = centerPane.getWidth();
            double centerHeight = centerPane.getHeight();
            double formWidth = loadedPane.getPrefWidth();
            double formHeight = loadedPane.getPrefHeight();

            AnchorPane.setTopAnchor(loadedPane, (centerHeight - formHeight) / 2);
            AnchorPane.setLeftAnchor(loadedPane, (centerWidth - formWidth) / 2);
            AnchorPane.setRightAnchor(loadedPane, (centerWidth - formWidth) / 2);
            AnchorPane.setBottomAnchor(loadedPane, (centerHeight - formHeight) / 2);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }














}