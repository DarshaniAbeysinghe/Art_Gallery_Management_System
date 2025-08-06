package com.example.art_gallery_management_system;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

public class MemberController {

    @FXML
    private ComboBox<String> nicComboBox;
    @FXML
    private TextField validDateField;
    @FXML
    private TextField paymentField;
    @FXML
    private TableView<Object[]> memberTableView;
    @FXML
    private TableColumn<Object[], Integer> midColumn;
    @FXML
    private TableColumn<Object[], String> nicColumn;
    @FXML
    private TableColumn<Object[], String> dateColumn;
    @FXML
    private TableColumn<Object[], String> validDateColumn;
    @FXML
    private TableColumn<Object[], BigDecimal> paymentColumn;

    private Connection connection;

    public MemberController() {
        connection = new DBConnection().getConnection();
    }

    @FXML
    public void initialize() {
        setValidDate();
        validDateField.setEditable(false);
        loadNICs();
        enableSearchableComboBox();
        loadTableData();
    }

    private void loadTableData() {
        ObservableList<Object[]> memberList = FXCollections.observableArrayList();
        String query = "SELECT MID, NIC, date, valid_date, payment FROM member";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Object[] rowData = new Object[]{
                        resultSet.getInt("MID"),
                        resultSet.getString("NIC"),
                        resultSet.getDate("date"),
                        resultSet.getDate("valid_date"),
                        resultSet.getBigDecimal("payment")
                };
                memberList.add(rowData);
            }


            System.out.println("Loaded data:");
            for (Object[] row : memberList) {
                System.out.println("MID: " + row[0] + ", NIC: " + row[1] + ", Date: " + row[2] + ", Valid Date: " + row[3] + ", Payment: " + row[4]);
            }


            midColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleIntegerProperty((Integer) cellData.getValue()[0]).asObject());
            nicColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[1]));
            dateColumn.setCellValueFactory(cellData -> {
                Date sqlDate = (Date) cellData.getValue()[2];
                return new javafx.beans.property.SimpleStringProperty(
                        sqlDate != null ? sqlDate.toString() : ""
                );
            });
            validDateColumn.setCellValueFactory(cellData -> {
                Date sqlDate = (Date) cellData.getValue()[3];
                return new javafx.beans.property.SimpleStringProperty(
                        sqlDate != null ? sqlDate.toString() : ""
                );
            });
            paymentColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleObjectProperty<>((BigDecimal) cellData.getValue()[4]));


            memberTableView.setItems(memberList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Error loading data from the database.");
        }
    }

    private void enableSearchableComboBox() {
        nicComboBox.setEditable(true);


        FilteredList<String> filteredList = new FilteredList<>(nicComboBox.getItems(), p -> true);


        nicComboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                filteredList.setPredicate(nic -> nic.toLowerCase().contains(newValue.toLowerCase()));
            } else {
                filteredList.setPredicate(nic -> true);
            }
        });


        nicComboBox.setItems(filteredList);


        nicComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !filteredList.contains(newValue)) {
                nicComboBox.setValue(null);
            }
        });
    }

    private void loadNICs() {
        ObservableList<String> nicList = FXCollections.observableArrayList();
        try {
            String query = "SELECT NIC FROM customer";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                nicList.add(resultSet.getString("NIC"));
            }

            nicComboBox.setItems(nicList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Error loading NICs from the database.");
        }
    }

    private void setValidDate() {
        LocalDate validDate = LocalDate.now().plusMonths(6);
        validDateField.setText(validDate.toString());
    }

    @FXML
    public void registerMember() {
        String nic = nicComboBox.getValue();
        String validDateText = validDateField.getText();
        String payment = paymentField.getText();

        if (nic == null || nic.isEmpty() || payment.isEmpty() || validDateText.isEmpty()) {
            showAlert(AlertType.WARNING, "Form Incomplete", "Please fill all fields.");
            return;
        }


        LocalDate validDate = null;
        try {
            validDate = LocalDate.parse(validDateText);
        } catch (Exception e) {
            showAlert(AlertType.WARNING, "Invalid Date", "Please enter a valid date.");
            return;
        }


        BigDecimal paymentAmount = null;
        try {
            paymentAmount = new BigDecimal(payment);
        } catch (NumberFormatException e) {
            showAlert(AlertType.WARNING, "Invalid Payment", "Please enter a valid payment amount.");
            return;
        }


        try {
            String checkQuery = "SELECT COUNT(*) FROM member WHERE NIC = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setString(1, nic);
            ResultSet rs = checkStatement.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                showAlert(AlertType.WARNING, "NIC Exists", "This NIC is already registered.");
                return;
            }

            // Insert the member into the database
            String query = "INSERT INTO member (NIC, valid_date, payment) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, nic);
            statement.setDate(2, Date.valueOf(validDate));
            statement.setBigDecimal(3, paymentAmount);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(AlertType.INFORMATION, "Success", "Member registered successfully.");
                nicComboBox.setValue(null);
                setValidDate();
                loadTableData();
            } else {
                showAlert(AlertType.ERROR, "Error", "Failed to register member.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Error registering the member.");
        }
    }


    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
