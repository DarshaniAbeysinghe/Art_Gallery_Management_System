package com.example.art_gallery_management_system;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Views.fxml"));
        Scene scene = new Scene(root, 758, 447);

        stage.initStyle(StageStyle.UNDECORATED);

        stage.setScene(scene);
        stage.setTitle("Hello!");


        stage.setOnShown(event -> {

            double screenWidth = stage.getOwner() == null ? java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth() : stage.getOwner().getWidth();
            double screenHeight = stage.getOwner() == null ? java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight() : stage.getOwner().getHeight();
            double windowWidth = stage.getWidth();
            double windowHeight = stage.getHeight();
            stage.setX((screenWidth - windowWidth) / 2);
            stage.setY((screenHeight - windowHeight) / 2);
        });

        stage.show();
    }

    public static void main(String[] args) {
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        launch();
    }





}
