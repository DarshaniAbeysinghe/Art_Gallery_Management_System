package com.example.art_gallery_management_system;

import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

public class OrderCRUDController {

    private Connection connection;

    @FXML
    private TableView<ObservableList<String>> ordersTable;

    @FXML
    private TextField searchField;

    public void initialize() {
        loadOrders();
    }


    public OrderCRUDController() {
        this.connection = new DBConnection().getConnection();
    }


    private void loadOrders() {
        ObservableList<ObservableList<String>> orders = getAllOrders();
        ordersTable.setItems(orders);

        ordersTable.getColumns().clear();

        if (!orders.isEmpty()) {
            String[] columnNames = {"Order ID", "NIC", "Artwork Name", "Quantity", "Price", "Status",
                    "Total Amount", "Share Percentage", "Artist Share", "Gallery Share"};

            for (int i = 0; i < orders.get(0).size(); i++) {
                TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnNames[i]);
                final int colIndex = i;
                column.setCellValueFactory(cellData ->
                        new javafx.beans.property.SimpleStringProperty(cellData.getValue().get(colIndex))
                );
                ordersTable.getColumns().add(column);
            }
        }
    }


    public ObservableList<ObservableList<String>> getAllOrders() {
        ObservableList<ObservableList<String>> orders = FXCollections.observableArrayList();
        String query = "SELECT o.OrderID, o.NIC, o.ArtworkName, o.Quantity, o.Price, o.Status, " +
                "p.total_amount, o.share_percentage, o.artist_share, o.gallery_share " +
                "FROM `order` o " +
                "LEFT JOIN placeorder p ON o.OrderID = p.OrderID";


        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData rsMeta = rs.getMetaData();
            int columnCount = rsMeta.getColumnCount();

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getString(i));
                }
                orders.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }


    @FXML
    private void onSearch() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {

            loadOrders();
        } else {
            ObservableList<ObservableList<String>> filteredList = FXCollections.observableArrayList();
            for (ObservableList<String> row : ordersTable.getItems()) {

                String nic = row.get(1).toLowerCase();
                if (nic.contains(searchText)) {
                    filteredList.add(row);
                }
            }
            ordersTable.setItems(filteredList);
        }
    }



    @FXML
    private void handleDelete() {

        ObservableList<String> selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {

            String orderId = selectedOrder.get(0);


            boolean confirmation = showConfirmationAlert("Are you sure you want to delete this order?");
            if (confirmation) {

                deleteOrder(orderId);


                loadOrders();
            }
        } else {
            showAlert(AlertType.WARNING, "No Order Selected", "Please select an order to delete.");
        }
    }


    private void deleteOrder(String orderId) {
        String deleteQuery = "DELETE FROM `order` WHERE OrderID = ?";
        try (Connection connection = new DBConnection().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
            preparedStatement.setString(1, orderId);
            int result = preparedStatement.executeUpdate();
            if (result > 0) {
                showAlert(AlertType.INFORMATION, "Success", "Order deleted successfully.");
            } else {
                showAlert(AlertType.ERROR, "Deletion Failed", "Order not found or could not be deleted.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Error deleting order: " + e.getMessage());
        }
    }


    private boolean showConfirmationAlert(String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }




    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
