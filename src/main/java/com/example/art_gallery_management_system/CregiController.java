package com.example.art_gallery_management_system;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public class CregiController {

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
    private TextArea addressField;

    @FXML
    private RadioButton maleRadioButton;

    @FXML
    private RadioButton femaleRadioButton;

    @FXML
    private Button registerBtn;

    @FXML
    private Button clearBtn;


    private ObservableList<CartItem> cartItems;


    @FXML
    private void handleAddCustomer(ActionEvent event) {
        String nic = nicField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String phone = tpNoField.getText();
        String address = addressField.getText();
        String gender = maleRadioButton.isSelected() ? "Male" : (femaleRadioButton.isSelected() ? "Female" : "");

        // Validation
        if (!validateNIC(nic)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid NIC. Please enter a valid NIC.");
            return;
        }
        if (!validatePhone(phone)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid phone number.");
            return;
        }
        if (!validateEmail(email)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid email. Please enter a valid email address.");
            return;
        }
        if (nic.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || gender.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all fields.");
            return;
        }

        try (Connection connection = new DBConnection().getConnection()) {
            // Insert customer details into the database
            String sql = "INSERT INTO customer (nic, first_name, last_name, email, phone, address, gender) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, nic);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setString(4, email);
            statement.setString(5, phone);
            statement.setString(6, address);
            statement.setString(7, gender);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer registered successfully!");

                // Pass cart details to order table
                String orderID = generateUniqueOrderID();
                confirmOrder(nic, orderID, cartItems);

                clearFields();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while adding the customer.");
        }
    }

    @FXML
    private void clearFields() {
        nicField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        tpNoField.clear();
        addressField.clear();
        maleRadioButton.setSelected(false);
        femaleRadioButton.setSelected(false);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean validateNIC(String nic) {
        return nic.matches("\\d{9}[vV]") || nic.matches("\\d{12}");
    }

    private boolean validatePhone(String phone) {
        return phone.matches("\\d{10}");
    }

    private boolean validateEmail(String email) {
        return Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$").matcher(email).matches();
    }


    private String generateUniqueOrderID() {
        String orderID = "";
        boolean isUnique = false;

        while (!isUnique) {
            // Generate a random order ID
            int orderNumber = (int) (Math.random() * 90000) + 10000;
            orderID = "ORD-" + orderNumber;

            // Check if the generated orderID exists in the database
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

    private void confirmOrder(String nic, String orderID, ObservableList<CartItem> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Order Error", "No cart items available to confirm the order.");
            return;
        }

        String query = "INSERT INTO `order` (NIC, OrderID, ArtworkID, OrderDateTime, ArtworkName, Quantity, Price) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = new DBConnection().getConnection()) {
            LocalDateTime localDateTime = LocalDateTime.now();
            ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("Asia/Colombo"));
            Timestamp currentTimestamp = Timestamp.from(zonedDateTime.toInstant());

            for (CartItem cartItem : cartItems) {
                // Validate the cart item
                if (cartItem.getQuantity() <= 0) {
                    System.out.println("Skipping item with zero or negative quantity: " + cartItem);
                    continue; // Skip invalid items
                }

                // Insert into the database
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, nic);
                    stmt.setString(2, orderID);
                    stmt.setInt(3, cartItem.getArtworkId());
                    stmt.setTimestamp(4, currentTimestamp);
                    stmt.setString(5, cartItem.getName());
                    stmt.setInt(6, cartItem.getQuantity());
                    stmt.setDouble(7, cartItem.getPrice());
                    stmt.executeUpdate();
                }
            }
            String email = getEmailByNIC(nic);  // Use DBConnection to get email

            if (email.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Customer email not found.");
                return;
            }


            sendOrderConfirmationEmail(email, orderID);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Order placed successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while placing the order.");
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


    public void setCartItems(ObservableList<CartItem> cartItems) {
        this.cartItems = cartItems;
        System.out.println("Received cart items:");
        for (CartItem item : cartItems) {
            System.out.println(item);
        }
    }


    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
