package com.example.art_gallery_management_system;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class ViewsController {

    @FXML
    private Button userbtn;



    public void gotoUser(ActionEvent e) {
        try {

            Stage currentStage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("UserView.fxml"));
            Stage userViewStage = new Stage();
            userViewStage.setScene(new Scene(root, 1535, 821));
            userViewStage.initStyle(StageStyle.UNDECORATED);
            userViewStage.show();

            currentStage.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void gotoAdmin(ActionEvent e) {
        try {

            Stage currentStage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("AdminLoging.fxml"));
            Stage adminViewStage = new Stage();
            adminViewStage.setScene(new Scene(root, 797, 494));
            adminViewStage.initStyle(StageStyle.UNDECORATED);
            adminViewStage.show();

            currentStage.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
