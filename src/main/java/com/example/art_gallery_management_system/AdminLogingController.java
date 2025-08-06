package com.example.art_gallery_management_system;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AdminLogingController {

    @FXML
    private TextField usernametxt;

    @FXML
    private Button scanbtn, addbtn;

    @FXML
    private Label textlbl;

    @FXML
    private ImageView qrimg;

    @FXML
    private File selectedFile;

    private static final String VOICENAME_KEVIN = "kevin16";

    @FXML
    public void addbtnAction(ActionEvent e) {

        if (usernametxt.getText().isBlank()) {
            textlbl.setText("Please enter your username");
            return;
        }


        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open QR Code Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        selectedFile = fileChooser.showOpenDialog(qrimg.getScene().getWindow());


        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString());
            qrimg.setImage(image);
            textlbl.setText("QR Code image loaded. Ready to scan.");
        } else {
            textlbl.setText("No image selected.");
        }
    }

    @FXML
    public void scanbtnAction(ActionEvent e) {
        if (selectedFile == null) {
            textlbl.setText("Please upload a QR code image first.");
            return;
        }

        try {

            BufferedImage bufferedImage = ImageIO.read(selectedFile);
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);


            String qrText = result.getText();


            String username = usernametxt.getText();
            if (checkQrAndUsernameInDatabase(qrText, username)) {

                speak("              You are Login successfully..............!");

                Stage currentStage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                Parent root = FXMLLoader.load(getClass().getResource("Dashboard.fxml"));
                Stage DashboardStage = new Stage();
                DashboardStage.initStyle(StageStyle.UNDECORATED);


                DashboardStage.setScene(new Scene(root, 1535, 821));
                DashboardStage.show();


               currentStage.close();
            } else {
                textlbl.setText("Invalid username or QR code.");
            }

        } catch (Exception ex) {
            textlbl.setText("Failed to scan QR Code. Please ensure the image is a valid QR code.");
            System.out.println(ex);
        }
    }

    private boolean checkQrAndUsernameInDatabase(String qrText, String username) {
        String query = "SELECT * FROM admin WHERE Qrtext = ? AND UserName = ?";
        DBConnection connectNow = new DBConnection();
        Connection connectDB = connectNow.getConnection();

        try (PreparedStatement stmt = connectDB.prepareStatement(query)) {
            stmt.setString(1, qrText);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (connectDB != null && !connectDB.isClosed()) {
                    connectDB.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }




    private void speak(String sayText) {
        Voice voice;
        VoiceManager voiceManager = VoiceManager.getInstance();
        voice = voiceManager.getVoice(VOICENAME_KEVIN);

        if (voice == null) {
            System.err.println("Voice not found: " + VOICENAME_KEVIN);
            return;
        }

        voice.allocate();
        voice.speak(sayText);
        voice.deallocate();
    }
}
