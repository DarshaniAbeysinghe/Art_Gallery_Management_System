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
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public class CregiTController {

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

    // Cart details passed from the previous scene
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
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid NIC. Please enter a valid Sri Lankan NIC.");
            return;
        }
        if (!validatePhone(phone)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid phone number. Please enter a 10-digit number.");
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
                clearFields();
                closeCurrentWindow();

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
    private void closeCurrentWindow() {

        Stage stage = (Stage) registerBtn.getScene().getWindow();
        stage.close();
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






}
