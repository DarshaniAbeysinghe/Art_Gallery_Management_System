package com.example.art_gallery_management_system;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class Regi_AdminController {

    @FXML
    private TextField fnametxt, lnametxt, tptxt, leveltxt, unametxt, qrtxt;
    @FXML
    private ImageView qrImageView;
    @FXML
    private Button qrbtn;

    @FXML
    public void qrbtnAction(ActionEvent e) {
        if (allFieldsFilled()) {
            byte[] qrCodeData = generateQRCode();
            if (qrCodeData != null) {
                register(qrCodeData);
                showAlert("Registration Successful", "Admin registered successfully.");
            }
        } else {
            showAlert("Registration Failed", "Please fill in all fields!");
        }
    }

    private boolean allFieldsFilled() {
        return !fnametxt.getText().isBlank() && !lnametxt.getText().isBlank() &&
                !tptxt.getText().isBlank() && !leveltxt.getText().isBlank() &&
                !unametxt.getText().isBlank() && !qrtxt.getText().isBlank();
    }

    private byte[] generateQRCode() {
        String text = qrtxt.getText();
        try {
            BitMatrix bitMatrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 200, 200);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);

            qrImageView.setImage(new Image(new ByteArrayInputStream(byteArrayOutputStream.toByteArray())));
            saveQRCodeImage(bufferedImage, text, "D:\\javaa\\");
            return byteArrayOutputStream.toByteArray();
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveQRCodeImage(BufferedImage image, String text, String directoryPath) {
        try {
            ImageIO.write(image, "png", new File(directoryPath + text + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void register(byte[] qrCodeData) {
        String query = "INSERT INTO admin (FirstName, LastName, Tp, Level, UserName, Qr, Qrtext) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connectDB = new DBConnection().getConnection();
             PreparedStatement preparedStatement = connectDB.prepareStatement(query)) {

            preparedStatement.setString(1, fnametxt.getText());
            preparedStatement.setString(2, lnametxt.getText());
            preparedStatement.setString(3, tptxt.getText());
            preparedStatement.setString(4, leveltxt.getText());
            preparedStatement.setString(5, unametxt.getText());
            preparedStatement.setBytes(6, qrCodeData);
            preparedStatement.setString(7, qrtxt.getText());
            preparedStatement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
