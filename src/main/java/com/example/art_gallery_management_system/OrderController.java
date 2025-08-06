package com.example.art_gallery_management_system;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.scene.control.Alert.AlertType;




import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.datatype.DataTypes;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.exception.DRException;

import java.io.FileOutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static net.sf.dynamicreports.report.builder.DynamicReports.stl;

public class OrderController {

    @FXML
    private TextField nicField;

    @FXML
    private TextField orderIdField;

    @FXML
    private TextField totalPriceField;

    @FXML
    private TableView<OrderItem> orderTable;

    @FXML
    private TableColumn<OrderItem, Integer> colArtworkId;

    @FXML
    private TableColumn<OrderItem, String> colArtworkName;

    @FXML
    private TableColumn<OrderItem, Integer> colQuantity;

    @FXML
    private TableColumn<OrderItem, Double> colPrice;
    @FXML
    private TextField midtxt;
    @FXML
    private TextField discounttxt;

    @FXML
    private TextField amounttxt;
    @FXML
    private AnchorPane mainPane;
    @FXML
    private TextField deliveryAddressField;



    private ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();


    public void initialize() {

        colArtworkId.setCellValueFactory(new PropertyValueFactory<>("artworkId"));
        colArtworkName.setCellValueFactory(new PropertyValueFactory<>("artworkName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));


        colPrice.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));


        colPrice.setOnEditCommit(event -> {

            OrderItem item = event.getRowValue();
            item.setPrice(event.getNewValue());


            updateTotalPrice();
        });


        orderTable.setItems(orderItems);
        updateTotalPrice();


        discounttxt.setText("0.00");
        amounttxt.setText(totalPriceField.getText());

        // Membership ID TextField for Enter key
        midtxt.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                checkMembership();
            }
        });


        mainPane.setOnMouseClicked(event -> {
            checkMembership();
        });
    }
    private void checkMembership() {
        String membershipId = midtxt.getText();
        String nic = nicField.getText();
        double totalPrice = Double.parseDouble(totalPriceField.getText()); // Get the total price entered
        double discount = 0.0;

        // If no membership ID is entered, no discount
        if (membershipId.isEmpty()) {
            discounttxt.setText("0.00");
            amounttxt.setText(String.format("%.2f", totalPrice));
            return;
        }

        // SQL query to check if the Membership ID and NIC exist in the database
        String query = "SELECT COUNT(*) FROM member WHERE MID = ? AND NIC = ?";

        try (Connection conn = new DBConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, membershipId); // Set Membership ID
            stmt.setString(2, nic); // Set NIC

            ResultSet rs = stmt.executeQuery();

            // If both MID and NIC match
            if (rs.next() && rs.getInt(1) > 0) {
                discount = totalPrice * 0.04; // Apply 4% discount
                discounttxt.setText(String.format("%.2f", discount)); // Display discount
            } else {
                discounttxt.setText("0.00"); // No discount if no match
            }

            // Calculate the amount after applying the discount
            double amountAfterDiscount = totalPrice - discount;
            amounttxt.setText(String.format("%.2f", amountAfterDiscount)); // Display the discounted amount

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while checking membership.", Alert.AlertType.ERROR);
        }
    }



    public void loadOrderDetails(String orderId) { // Changed to accept OrderID
        String query = "SELECT * FROM `order` WHERE OrderID = ?";

        try (Connection conn = new DBConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Set NIC and Order ID in the TextFields
                nicField.setText(rs.getString("nic"));
                orderIdField.setText(rs.getString("OrderID"));

                // Check the status of the order
                String status = rs.getString("status");
                if ("placed".equalsIgnoreCase(status)) {
                    showAlert("Order Status", "This order has already been placed and cannot be reopened.", Alert.AlertType.INFORMATION);
                    return;
                }


                loadOrderItems(rs.getString("OrderID"));

            } else {

                showAlert("No Orders", "No order found for the provided Order ID!", Alert.AlertType.INFORMATION);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while retrieving the order details.", Alert.AlertType.ERROR);
        }
    }

    // load the artwork details for the order
    private void loadOrderItems(String orderId) {

        String query = "SELECT ArtworkID, ArtworkName, Quantity, Price FROM `order` WHERE OrderID = ?";

        try (Connection conn = new DBConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {


            stmt.setString(1, orderId);
            ResultSet rs = stmt.executeQuery();


            orderItems.clear();


            while (rs.next()) {
                int artworkId = rs.getInt("ArtworkID");
                String artworkName = rs.getString("ArtworkName");
                int quantity = rs.getInt("Quantity");
                double price = rs.getDouble("Price");


                orderItems.add(new OrderItem(artworkId, artworkName, quantity, price));
            }


            orderTable.setItems(orderItems);


            updateTotalPrice();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while loading the order items.", AlertType.ERROR);
        }
    }

    //  update the total price
    private void updateTotalPrice() {
        double total = 0;
        for (OrderItem item : orderItems) {
            total += item.getPrice();
        }
        totalPriceField.setText(String.format("%.2f", total));


        amounttxt.setText(String.format("%.2f", total));
    }


    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handlePlaceOrder() {
        String nic = nicField.getText();
        String orderId = orderIdField.getText();
        String deliverAddress = deliveryAddressField.getText();


        double totalAmount;
        try {
            totalAmount = Double.parseDouble(amounttxt.getText());
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Invalid total amount.", Alert.AlertType.WARNING);
            return;
        }


        if (nic.isEmpty() || orderId.isEmpty() || deliverAddress.isEmpty()) {
            showAlert("Input Error", "Please fill in all fields.", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = new DBConnection().getConnection()) {

            conn.setAutoCommit(false);


            String insertOrderQuery = "INSERT INTO placeorder (OrderID, nic, total_amount, deliver_address) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertOrderQuery)) {
                stmt.setString(1, orderId);
                stmt.setString(2, nic);
                stmt.setDouble(3, totalAmount);
                stmt.setString(4, deliverAddress);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected <= 0) {
                    conn.rollback();
                    showAlert("Failure", "Failed to place the order.", Alert.AlertType.ERROR);
                    return;
                }
            }


            String fetchOrderItemsQuery = "SELECT OID, Price, Quantity FROM `order` WHERE OrderID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(fetchOrderItemsQuery)) {
                stmt.setString(1, orderId);

                try (ResultSet rs = stmt.executeQuery()) {
                    double totalItemsPrice = 0.0;
                    Map<Integer, Double> itemTotals = new HashMap<>();

                    // First, calculate the total price for all items
                    while (rs.next()) {
                        int oid = rs.getInt("OID");
                        double price = rs.getDouble("Price");
                        int quantity = rs.getInt("Quantity");

                        double itemTotal = price * quantity;
                        totalItemsPrice += itemTotal;
                        itemTotals.put(oid, itemTotal);
                    }

                    // Now, calculate shares proportionally for each item
                    for (Map.Entry<Integer, Double> entry : itemTotals.entrySet()) {
                        int oid = entry.getKey();
                        double itemTotal = entry.getValue();


                        double itemProportion = itemTotal / totalItemsPrice;
                        double proportionalAmount = totalAmount * itemProportion;


                        double artistShare = proportionalAmount * 0.40;
                        double galleryShare = proportionalAmount * 0.60;


                        String updateSharesQuery = "UPDATE `order` SET artist_share = ?, gallery_share = ?, status = ? WHERE OID = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSharesQuery)) {
                            updateStmt.setDouble(1, artistShare);
                            updateStmt.setDouble(2, galleryShare);
                            updateStmt.setString(3, "placed"); // Update status to 'placed'
                            updateStmt.setInt(4, oid);

                            updateStmt.executeUpdate();
                        }
                    }
                }
            }


            conn.commit();
            showAlert("Success", "Order placed successfully with proportional shares and status updated!", Alert.AlertType.INFORMATION);

            handleGenerateBill();
            Stage currentStage = (Stage) nicField.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while placing the order.", Alert.AlertType.ERROR);
        }
    }



    private void updateOrderStatus(String orderId) {
        String updateQuery = "UPDATE `order` SET status = 'placed' WHERE OrderID = ?";

        try (Connection conn = new DBConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            stmt.setString(1, orderId);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while updating the order status.", Alert.AlertType.ERROR);
        }
    }



    @FXML
    private void handleGenerateBill() {
        if (orderItems.isEmpty()) {
            showAlert("Error", "No order items to generate the bill.", Alert.AlertType.WARNING);
            return;
        }

        String orderId = orderIdField.getText();
        String nic = nicField.getText();


        double subtotal = Double.parseDouble(totalPriceField.getText());

        double discount = Double.parseDouble(discounttxt.getText());

        double totalAfterDiscount = Double.parseDouble(amounttxt.getText());

        try {
            JasperReportBuilder report = DynamicReports.report();


            report.columns(
                            Columns.column("Artwork ID", "artworkId", DataTypes.integerType())
                                    .setHorizontalAlignment(HorizontalAlignment.LEFT),
                            Columns.column("Artwork Name", "artworkName", DataTypes.stringType()),
                            Columns.column("Quantity", "quantity", DataTypes.integerType())
                                    .setHorizontalAlignment(HorizontalAlignment.RIGHT),
                            Columns.column("Price", "price", DataTypes.doubleType())
                                    .setHorizontalAlignment(HorizontalAlignment.RIGHT))


                    .title(
                            Components.text("ART HEAVEN STUDIO")
                                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                                    .setStyle(stl.style().bold().setFontSize(16))
                    )


                    .title(Components.text("\n"))


                    .title(
                            Components.text("Order ID: " + orderId)
                                    .setHorizontalAlignment(HorizontalAlignment.LEFT)
                                    .setStyle(stl.style().setFontSize(12)),
                            Components.text("NIC: " + nic)
                                    .setHorizontalAlignment(HorizontalAlignment.LEFT)
                                    .setStyle(stl.style().setFontSize(12))
                    )


                    .title(Components.text("\n"))

                    .pageFooter(Components.pageXofY())
                    .setDataSource(new OrderItemsDataSource(orderItems))


                    .setParameter("orderId", orderId)
                    .setParameter("nic", nic)
                    .setParameter("subtotal", subtotal)
                    .setParameter("discount", discount)
                    .setParameter("totalAfterDiscount", totalAfterDiscount);



            report.summary(
                    Components.text("\nSubtotal: " + subtotal).setHorizontalAlignment(HorizontalAlignment.RIGHT)
                            .setStyle(stl.style().setFontSize(12)),
                    Components.text("Discount: " + discount).setHorizontalAlignment(HorizontalAlignment.RIGHT)
                            .setStyle(stl.style().setFontSize(12)),
                    Components.text("Total (After Discount): " + totalAfterDiscount).setHorizontalAlignment(HorizontalAlignment.RIGHT)
                            .setStyle(stl.style().setFontSize(12)),
                    Components.text("\nThank You!").setHorizontalAlignment(HorizontalAlignment.CENTER)
                            .setStyle(stl.style().setFontSize(12))
            );


            String filePath = "C:/Users/ASUS/Desktop/pdffile/" + orderId + "_bill.pdf";
            System.out.println("Generating PDF at: " + filePath);

            // Ensure the directory exists
            File directory = new File("C:/Users/ASUS/Desktop/pdffile/");
            if (!directory.exists()) {
                boolean dirCreated = directory.mkdirs();
                if (dirCreated) {
                    System.out.println("Directory created: " + directory.getAbsolutePath());
                } else {
                    System.out.println("Failed to create directory: " + directory.getAbsolutePath());
                }
            }


            try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
                report.toPdf(fileOutputStream);
                System.out.println("PDF generated at: " + filePath);


                File pdfFile = new File(filePath);
                if (pdfFile.exists()) {
                    System.out.println("PDF file exists at: " + filePath);

                    sendBillByEmail(filePath);
                } else {
                    System.out.println("PDF file NOT found at: " + filePath);
                    showAlert("Error", "Failed to generate the PDF. Please try again.", Alert.AlertType.ERROR);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to generate the bill: File not found.", Alert.AlertType.ERROR);
            } catch (DRException e) {
                e.printStackTrace();
                showAlert("Error", "An error occurred while generating the bill.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred.", Alert.AlertType.ERROR);
        }
    }






    private void sendBillByEmail(String ticketPath) {

        String nic = nicField.getText();


        String recipientEmail = getCustomerEmailByNIC(nic);

        if (recipientEmail == null) {
            showAlert("Error", "No customer found with NIC: " + nic, Alert.AlertType.ERROR);
            return;
        }

        String fromEmail = "darshuabey@gmail.com";
        String password = "hjit uubq ilve clmd";
        String subject = "Your Art Gallery Bill";
        String body = "Thank you for your recent purchase at Art Heaven Studio! Please find your bill attached.";

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

            System.out.println("Attaching file from path: " + ticketPath);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body);

            MimeBodyPart attachmentPart = new MimeBodyPart();
            File pdfFile = new File(ticketPath);


            if (pdfFile.exists()) {
                attachmentPart.attachFile(pdfFile);
            } else {
                System.out.println("File not found at: " + ticketPath);
                showAlert("Error", "Failed to attach the PDF file. It could not be found at the specified path.", Alert.AlertType.ERROR);
                return;
            }

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            Transport.send(message);
            System.out.println("Bill sent to " + recipientEmail);

            // Alert to show successful sending
            showAlert("Success", "Bill successfully sent to " + recipientEmail, Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to send the bill via email.", Alert.AlertType.ERROR);
        }
    }






    private class OrderItemsDataSource extends net.sf.dynamicreports.report.datasource.DRDataSource {
        public OrderItemsDataSource(ObservableList<OrderItem> items) {
            super("artworkId", "artworkName", "quantity", "price");
            for (OrderItem item : items) {
                add(item.getArtworkId(), item.getArtworkName(), item.getQuantity(), item.getPrice());
            }
        }
    }

    private String getCustomerEmailByNIC(String nic) {
        String email = null;
        Connection connection = null;

        try {

            DBConnection dbConnection = new DBConnection();
            connection = dbConnection.getConnection();


            String query = "SELECT email FROM customer WHERE nic = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, nic);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                email = rs.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return email;
    }






}

