package com.example.art_gallery_management_system;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CustomPopup {

    public void showRegisterPopup() {

        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Welcome to Art Haven Studio!");


        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-background-color: #f5f5f5; -fx-border-radius: 10px;");


        Label headerLabel = new Label("It looks like you’ve found a piece of art that speaks to you!");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");


        Label messageLabel = new Label("If you’re interested in purchasing the artwork you’ve selected, "
                + "you’ll need to become a part of our creative family. "
                + "Please register with us to unlock the ability to purchase and explore exclusive offers! "
                + "Would you like to register now?");
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");


        Button registerButton = new Button("Register Now");
        Button cancelButton = new Button("Cancel");


        registerButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-size: 14px;");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-size: 14px;");


        registerButton.setOnAction(e -> {
            System.out.println("Redirecting to registration...");
            popupStage.close();  // Close the popup when the user clicks "Register Now"
        });


        cancelButton.setOnAction(e -> {
            System.out.println("Registration canceled.");
            popupStage.close();  // Close the popup when the user clicks "Cancel"
        });


        vbox.getChildren().addAll(headerLabel, messageLabel, registerButton, cancelButton);

        // Set the scene and size of the popup window
        Scene scene = new Scene(vbox, 500, 500);
        popupStage.setScene(scene);


        popupStage.showAndWait();  // Block interaction with other windows until the popup is closed
    }
}