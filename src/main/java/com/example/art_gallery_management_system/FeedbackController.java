package com.example.art_gallery_management_system;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class FeedbackController {

    @FXML
    private TextArea feedbackTextArea;

    @FXML
    private Button submitButton;


    @FXML
    private void handleSubmitFeedback(ActionEvent event) {
        String feedbackText = feedbackTextArea.getText();

        if (feedbackText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Feedback cannot be empty.");
            return;
        }


        saveFeedback(feedbackText);


        feedbackTextArea.clear();


        showAlert(Alert.AlertType.INFORMATION, "Feedback Submitted", "Thank you for your feedback!");
    }

    private void saveFeedback(String feedbackText) {
        String query = "INSERT INTO feedback (feedback_text) VALUES (?)";

        try (Connection connection = new DBConnection().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, feedbackText);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Feedback saved successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while saving the feedback.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while saving the feedback.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
