package com.example.art_gallery_management_system;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;


import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class CheckController {

    @FXML
    private TextField checktxt;

    @FXML
    private ImageView checkimg;

    private String cartDetails;




    public void setCartDetails(String cartDetails) {
        this.cartDetails = cartDetails;
    }


    @FXML
    private void handleImageClick(MouseEvent event) {
        String enteredNIC = checktxt.getText().trim();
        System.out.println("Entered NIC: " + enteredNIC);


        if (!isValidNIC(enteredNIC)) {
            showAlert(AlertType.ERROR, "Invalid NIC format. Please enter a valid NIC.");
            return;
        }

        if (enteredNIC.isEmpty()) {
            showAlert(AlertType.WARNING, "Please enter a NIC.");
            return;
        }

        String[] userDetails = checkNICExists(enteredNIC);
        if (userDetails != null) {

            String firstName = userDetails[0];
            String lastName = userDetails[1];


            showCustomerGreetingWithCart(firstName, lastName);
        } else {
            // NIC not found, ask the visitor to register
            showRegisterAlert();
        }


        checktxt.clear();
    }

    public boolean isValidNIC(String nic) {

        String nicRegex = "^(\\d{9}[Vv]$|\\d{12})$";


        if (nic == null || nic.trim().isEmpty()) {
            return false;
        }


        return nic.matches(nicRegex);
    }

    private void showRegisterAlert() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Welcome to Art Haven Studio!");
        alert.setHeaderText("It looks like you’ve found a piece of art that speaks to you!");

        // Create a Label for the content message
        Label contentLabel = new Label("If you’re interested in purchasing the artwork you’ve selected, "
                + "you’ll need to become a part of our creative family. "
                + "Please register with us to unlock the ability to purchase and explore exclusive offers! "
                + "Would you like to register now?");
        contentLabel.setWrapText(true);


        VBox vbox = new VBox(contentLabel);
        vbox.setPadding(new Insets(10));
        alert.getDialogPane().setContent(vbox);


        alert.getButtonTypes().setAll(
                new ButtonType("Register Now"),
                new ButtonType("Cancel")
        );


        alert.showAndWait().ifPresent(response -> {
            if (response.getText().equals("Register Now")) {

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("C.Regi.fxml"));
                    Parent root = loader.load();


                    CregiController cregiController = loader.getController();


                    ObservableList<CartItem> cartItemsList = FXCollections.observableArrayList();

// Ensure cartDetails is not empty and properly formatted
                    if (this.cartDetails != null && !this.cartDetails.trim().isEmpty()) {
                        String[] cartDetailsArray = this.cartDetails.split("\n");


                        for (int i = 0; i < cartDetailsArray.length; i += 4) {  // Expected structure has 4 lines per item

                            if (i + 3 < cartDetailsArray.length) {
                                try {
                                    String itemId = cartDetailsArray[i].split(":")[1].trim();  // Item ID (String)
                                    String name = cartDetailsArray[i + 1].split(":")[1].trim();  // Item Name (String)
                                    int quantity = Integer.parseInt(cartDetailsArray[i + 2].split(":")[1].trim());  // Quantity (int)
                                    double price = Double.parseDouble(cartDetailsArray[i + 3].split(":")[1].trim());  // Price (double)


                                    CartItem cartItem = new CartItem(Integer.parseInt(itemId), name, price, quantity);


                                    cartItemsList.add(cartItem);
                                } catch (Exception e) {

                                    e.printStackTrace();
                                    showAlert(AlertType.ERROR, "Error parsing cart item data.");
                                }
                            } else {

                                break;
                            }
                        }
                    } else {

                        showAlert(AlertType.ERROR, "No cart items available.");
                    }


                    cregiController.setCartItems(cartItemsList);



                    Stage registerStage = new Stage();
                    registerStage.setTitle("Customer Registration");

                    registerStage.setScene(new Scene(root));

                    registerStage.centerOnScreen();


                    registerStage.show();


                    Stage currentStage = (Stage) checkimg.getScene().getWindow();
                    if (currentStage != null) {
                        currentStage.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert(AlertType.ERROR, "Failed to load registration page.");
                }
            } else {

                System.out.println("Registration canceled.");
            }
        });

    }






    // Method to check if the NIC exists in the database



    //customer keneknm greeting eka
    private void showCustomerGreetingWithCart(String firstName, String lastName) {

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Customer Greeting and Order Details");


        Stage currentStage = (Stage) checktxt.getScene().getWindow();
        dialog.initOwner(currentStage);


        String greetingMessage = "Hello, " + firstName + " " + lastName + "!";


        double totalPrice = calculateTotalPrice();


        VBox vbox = new VBox();
        Label greetingLabel = new Label(greetingMessage);


        TextArea cartTextArea = new TextArea(cartDetails);
        cartTextArea.setWrapText(true);
        cartTextArea.setEditable(false);
        cartTextArea.setPrefHeight(150);


        Label totalPriceLabel = new Label("Total Price: " + String.format("%.2f", totalPrice));



        vbox.getChildren().addAll(greetingLabel, cartTextArea, totalPriceLabel);


        Button confirmOrderButton = new Button("Confirm Order");
        Button cancelOrderButton = new Button("Cancel Order");


        HBox buttonBox = new HBox(10, confirmOrderButton, cancelOrderButton);
        buttonBox.setStyle("-fx-alignment: center; -fx-padding: 10;");
        vbox.getChildren().add(buttonBox);


        dialog.getDialogPane().setContent(vbox);

        confirmOrderButton.setOnAction(event -> {

            String orderID = generateUniqueOrderID();


            String nic = checktxt.getText().trim();

            if (nic.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Please enter a valid NIC.");
                return;
            }


            String[] cartItems = cartDetails.split("\n------------------------\n");


            confirmOrder(nic, orderID, cartItems);


            showAlert(Alert.AlertType.INFORMATION, "Your order has been confirmed. Thank you!");


            dialog.close();


            currentStage.close();

            try {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("UserView.fxml"));
                Parent root = loader.load();


                Stage userViewStage = new Stage();
                userViewStage.setScene(new Scene(root));
                userViewStage.centerOnScreen();
                userViewStage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Failed to load the User View page.");
            }
        });









        cancelOrderButton.setOnAction(event -> {
            System.out.println("Order Canceled!");
            dialog.close();


            currentStage.close();

            try {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("UserView.fxml"));
                Parent root = loader.load();


                Stage userViewStage = new Stage();
                userViewStage.setScene(new Scene(root));
                userViewStage.centerOnScreen();
                userViewStage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Failed to load the User View page.");
            }
        });


        dialog.showAndWait();
    }

    private String generateUniqueOrderID() {
        String orderID = "";
        boolean isUnique = false;

        while (!isUnique) {

            int orderNumber = (int) (Math.random() * 90000) + 10000;
            orderID = "ORD-" + orderNumber;


            isUnique = !isOrderIDExists(orderID);
        }

        return orderID;
    }

    private boolean isOrderIDExists(String orderID) {
        boolean exists = false;
        try (Connection connection = new DBConnection().getConnection()) {
            String query = "SELECT COUNT(*) FROM `order` WHERE OrderID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, orderID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                exists = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exists;
    }


    // Method to calculate the total price of the cart
    private double calculateTotalPrice() {
        double total = 0.0;


        String[] items = cartDetails.split("\n------------------------\n");

        for (String item : items) {
            if (item.contains("Price:") && item.contains("Quantity:")) {
                String priceString = item.split("Price:")[1].split("\n")[0].trim(); // Get the price part
                String quantityString = item.split("Quantity:")[1].split("\n")[0].trim(); // Get the quantity part


                System.out.println("Price String: " + priceString);
                System.out.println("Quantity String: " + quantityString);


                priceString = priceString.replaceAll("[^\\d.]", "").trim();

                if (priceString.isEmpty() || quantityString.isEmpty()) {
                    System.out.println("Invalid data encountered: Price - " + priceString + ", Quantity - " + quantityString);
                    continue;
                }

                try {
                    double price = Double.parseDouble(priceString);
                    int quantity = Integer.parseInt(quantityString);
                    total += price * quantity; // Add price * quantity to the total
                } catch (NumberFormatException e) {

                    System.out.println("Error parsing price or quantity. Price: " + priceString + ", Quantity: " + quantityString);
                    e.printStackTrace();
                }
            }
        }

        return total;
    }







    private String[] checkNICExists(String nic) {
        String query = "SELECT first_name, last_name FROM customer WHERE nic = ?";
        DBConnection dbConnection = new DBConnection();

        try (Connection conn = dbConnection.getConnection()) {
            if (conn == null) {
                showAlert(AlertType.ERROR, "Database connection failed.");
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, nic);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {

                    return new String[]{rs.getString("first_name"), rs.getString("last_name")};
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }



    private void confirmOrder(String nic, String orderID, String[] cartItems) {
        String query = "INSERT INTO `order` (NIC, OrderID, ArtworkID, OrderDateTime, ArtworkName, Quantity, Price) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        DBConnection dbConnection = new DBConnection();

        try (Connection conn = dbConnection.getConnection()) {
            if (conn == null) {
                showAlert(Alert.AlertType.ERROR, "Database connection failed.");
                return;
            }


            LocalDateTime localDateTime = LocalDateTime.now();
            ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("Asia/Colombo"));
            Timestamp currentTimestamp = Timestamp.from(zonedDateTime.toInstant());


            for (String item : cartItems) {
                String[] itemDetails = item.split("\n");
                int artworkID = Integer.parseInt(itemDetails[0].split(":")[1].trim());
                String artworkName = itemDetails[1].split(":")[1].trim();
                int quantity = Integer.parseInt(itemDetails[2].split(":")[1].trim());
                String priceString = itemDetails[3].split(":")[1].trim().replace("$", "");
                double price = Double.parseDouble(priceString);

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, nic);
                    stmt.setString(2, orderID);
                    stmt.setInt(3, artworkID);
                    stmt.setTimestamp(4, currentTimestamp);
                    stmt.setString(5, artworkName);
                    stmt.setInt(6, quantity);
                    stmt.setDouble(7, price);

                    stmt.executeUpdate();
                }
            }


            String email = getEmailByNIC(nic);

            if (email.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Customer email not found.");
                return;
            }


            sendOrderConfirmationEmail(email, orderID);

            System.out.println("Order placed and confirmation email sent.");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to place the order.");
        }
    }



    private String getEmailByNIC(String nic) {
        String email = "";
        String query = "SELECT email FROM customer WHERE nic = ?";

        try (Connection conn = new DBConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nic);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    email = rs.getString("email");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return email;
    }







 //email ekn order id ywnna
    private void sendOrderConfirmationEmail(String recipientEmail, String orderID) {
        String fromEmail = "darshuabey@gmail.com";
        String password = "hjit uubq ilve clmd";
        String subject = "ART HEVEN STUDIO   Order Confirmation - " + orderID;
        String body = "Thank you for placing your order! Your order ID is " + orderID + ".\n\n" +
                "To proceed with your order, please make the payment at Cashier No. 1. " +
                "Once the payment is received, we will begin processing and preparing your order for shipment. " +
                "We will notify you once your order is ready to be shipped.\n\n" +
                "If you have any questions, please do not hesitate to contact us.\n\n" +
                "Thank you for shopping with us!";

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);


            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body);


            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);


            message.setContent(multipart);


            Transport.send(message);
            System.out.println("Order confirmation sent to " + recipientEmail);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    private void showAlert(AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
