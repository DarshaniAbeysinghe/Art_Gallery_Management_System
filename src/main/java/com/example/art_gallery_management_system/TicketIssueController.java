package com.example.art_gallery_management_system;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.geometry.Pos;



import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


import java.time.LocalDate;

public class TicketIssueController {

    private static final double TICKET_PRICE = 100.00;
    private static final String TICKET_DIRECTORY = "D:\\jav\\";

    @FXML
    private TextField nicField, tpField, emailField;

    @FXML
    private Spinner<Integer> quantitySpinner;

    @FXML
    private Label totalPriceLabel, statusLabel;

    @FXML
    private Button issueTicketButton;
    @FXML
    private AnchorPane centerPane;

    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
        quantitySpinner.setValueFactory(valueFactory);
        updateTotalPrice();
        quantitySpinner.valueProperty().addListener((obs, oldValue, newValue) -> updateTotalPrice());
        issueTicketButton.setOnAction(event -> issueTicket());
    }

    private void updateTotalPrice() {
        int quantity = quantitySpinner.getValue();
        double totalPrice = quantity * TICKET_PRICE;
        totalPriceLabel.setText("Total Price: $" + totalPrice);
    }

    private void issueTicket() {
        String nic = nicField.getText();
        String tp = tpField.getText();
        String email = emailField.getText();
        int quantity = quantitySpinner.getValue();
        statusLabel.setAlignment(Pos.CENTER);

        if (nic.isEmpty()) {
            statusLabel.setText("Please enter the NIC.");
            nicField.requestFocus();
            return;
        }


        if (!nic.matches("([0-9]{9}[vV])|([0-9]{12})")) {
            statusLabel.setText("Invalid NIC format.");
            nicField.requestFocus();
            return;
        }


        if (tp.isEmpty()) {
            statusLabel.setText("Please enter the telephone number.");
            tpField.requestFocus();
            return;
        }


        if (!tp.matches("[0-9]{10}")) {
            statusLabel.setText("Invalid Telephone Number. ");
            tpField.requestFocus();
            return;
        }


        if (email.isEmpty()) {
            statusLabel.setText("Please enter the email address.");
            emailField.requestFocus();
            return;
        }


        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            statusLabel.setText("Invalid Email format.");
            emailField.requestFocus();
            return;
        }






        DBConnection connectNow = new DBConnection();
        try (Connection connection = connectNow.getConnection()) {
            String insertVisitorQuery = "INSERT INTO visitors (NIC, TP, Email, ticket_quantity) VALUES (?, ?, ?, ?)";
            PreparedStatement visitorStmt = connection.prepareStatement(insertVisitorQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            visitorStmt.setString(1, nic);
            visitorStmt.setString(2, tp);
            visitorStmt.setString(3, email);
            visitorStmt.setInt(4, quantity);
            visitorStmt.executeUpdate();

            var generatedKeys = visitorStmt.getGeneratedKeys();
            int visitorID = 0;
            if (generatedKeys.next()) {
                visitorID = generatedKeys.getInt(1);
            }

            String insertTicketQuery = "INSERT INTO tickets (VisitorID, IssueDate, ValidDate, Price, QRCode) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ticketStmt = connection.prepareStatement(insertTicketQuery, PreparedStatement.RETURN_GENERATED_KEYS);

            for (int i = 0; i < quantity; i++) {
                LocalDate issueDate = LocalDate.now();
                LocalDate validDate = issueDate.plusDays(7);

                ticketStmt.setInt(1, visitorID);
                ticketStmt.setDate(2, java.sql.Date.valueOf(issueDate));
                ticketStmt.setDate(3, java.sql.Date.valueOf(validDate));
                ticketStmt.setDouble(4, TICKET_PRICE);

                String qrCodeText = java.util.UUID.randomUUID().toString();
                ticketStmt.setString(5, qrCodeText);

                String qrCodeImagePath = TICKET_DIRECTORY + "qr_" + visitorID + "_" + (i + 1) + ".png";
                saveQRCodeImage(qrCodeText, qrCodeImagePath);

                ticketStmt.executeUpdate();
                var ticketGeneratedKeys = ticketStmt.getGeneratedKeys();
                int ticketID = 0;
                if (ticketGeneratedKeys.next()) {
                    ticketID = ticketGeneratedKeys.getInt(1);
                }

                String ticketPath = TICKET_DIRECTORY + "ticket_" + ticketID + ".png";
                generateTicketImage(ticketID, qrCodeText, issueDate, validDate, TICKET_PRICE, ticketPath, qrCodeImagePath);


                sendTicketByEmail(email, ticketPath);
            }

            statusLabel.setText("Tickets issued successfully!");
            clearForm();

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error issuing tickets.");
        }
    }

    private void sendTicketByEmail(String recipientEmail, String ticketPath) {
        String fromEmail = "darshuabey@gmail.com";
        String password = "hjit uubq ilve clmd";
        String subject = "Your Art Gallery Ticket";
        String body = "Thank you for visiting our Art Gallery! Please find your ticket attached.";

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

            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(new File(ticketPath));

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            Transport.send(message);
            System.out.println("Ticket sent to " + recipientEmail);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateTicketImage(int ticketID, String qrCodeText, LocalDate issueDate, LocalDate validDate, double price, String ticketPath, String qrCodePath) {
        int width = 400;
        int height = 300;
        BufferedImage ticketImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = ticketImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Art Gallery", 150, 30);
        g2d.drawString("TID: " + ticketID, 50, 60);
        g2d.drawString("QR Code:", 50, 100);
        g2d.drawString("Issue Date: " + issueDate, 50, 170);
        g2d.drawString("Valid Date: " + validDate, 50, 200);
        g2d.drawString("Price per Ticket: $" + price, 50, 230);
        g2d.drawString("Thanks!", 150, 270);

        try {
            BufferedImage qrImage = ImageIO.read(new File(qrCodePath));
            g2d.drawImage(qrImage, 50, 110, 50, 50, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        g2d.dispose();
        try {
            ImageIO.write(ticketImage, "PNG", new File(ticketPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveQRCodeImage(String qrCodeText, String filePath) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, 200, 200);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ImageIO.write(qrImage, "PNG", new File(filePath));
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void closeForm() {
        centerPane.getChildren().clear();
    }



    private void clearForm() {
        nicField.clear();
        tpField.clear();
        emailField.clear();
        quantitySpinner.getValueFactory().setValue(1);
        updateTotalPrice();
    }
}