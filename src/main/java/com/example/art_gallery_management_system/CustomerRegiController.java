package com.example.art_gallery_management_system;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class CustomerRegiController {

    @FXML
    private TextField nicField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField tpNoField;

    @FXML
    private TextArea addressField;

    @FXML
    private RadioButton maleRadioButton;

    @FXML
    private RadioButton femaleRadioButton;

    @FXML
    private Button registerBtn;

    @FXML
    private Button updateCustomerBtn;

    @FXML
    private Button clearBtn;

    @FXML
    private TextField searchTxt;

    @FXML
    private TableView<Customer> customerTable;

    @FXML
    private TableColumn<Customer, Integer> colId;

    @FXML
    private TableColumn<Customer, String> colNic;

    @FXML
    private TableColumn<Customer, String> colFirstName;

    @FXML
    private TableColumn<Customer, String> colLastName;

    @FXML
    private TableColumn<Customer, String> colEmail;

    @FXML
    private TableColumn<Customer, String> colTelephone;

    @FXML
    private TableColumn<Customer, String> colGender;

    @FXML
    private TableColumn<Customer, String> colAddress;

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    public void initialize() {

        colId.setCellValueFactory(cellData -> cellData.getValue().colIDProperty().asObject());
        colNic.setCellValueFactory(cellData -> cellData.getValue().nicProperty());
        colFirstName.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        colLastName.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        colTelephone.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        colEmail.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        colGender.setCellValueFactory(cellData -> cellData.getValue().genderProperty());
        colAddress.setCellValueFactory(cellData -> cellData.getValue().addressProperty());

        loadCustomerData();
    }



    @FXML
    private void handleAddCustomer(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        String nic = nicField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String phone = tpNoField.getText();
        String address = addressField.getText();
        String gender = maleRadioButton.isSelected() ? "Male" : (femaleRadioButton.isSelected() ? "Female" : "");

        if (nic.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || gender.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all fields.");
            return;
        }

        try (Connection connection = new DBConnection().getConnection()) {
            String sql = "INSERT INTO customer (nic, first_name, last_name, email, phone, address, gender) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, nic);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setString(4, email);
            statement.setString(5, phone);
            statement.setString(6, address);
            statement.setString(7, gender);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer added successfully!");
                loadCustomerData();
                clearFields();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while adding the customer.");
        }
    }

    @FXML
    private void handleUpdateCustomer(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        int colID = getSelectedCustomerId();
        if (colID == -1) return;

        String nic = nicField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String phone = tpNoField.getText();
        String address = addressField.getText();
        String gender = maleRadioButton.isSelected() ? "Male" : (femaleRadioButton.isSelected() ? "Female" : "");

        if (nic.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || gender.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all fields.");
            return;
        }

        try (Connection connection = new DBConnection().getConnection()) {
            String sql = "UPDATE customer SET nic = ?, first_name = ?, last_name = ?, email = ?, phone = ?, address = ?, gender = ? WHERE colID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, nic);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setString(4, email);
            statement.setString(5, phone);
            statement.setString(6, address);
            statement.setString(7, gender);
            statement.setInt(8, colID);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer updated successfully!");
                loadCustomerData();
                clearFields();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while updating the customer.");
        }
    }


    @FXML
    private void handleDeleteCustomer(ActionEvent event) {
        int colID = getSelectedCustomerId();
        if (colID == -1) return;

        try (Connection connection = new DBConnection().getConnection()) {
            String sql = "DELETE FROM customer WHERE colID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, colID);

            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer deleted successfully!");
                loadCustomerData();
                clearFields();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while deleting the customer.");
        }
    }


    private void loadCustomerData() {
        customerList.clear();

        try (Connection connection = new DBConnection().getConnection()) {
            String sql = "SELECT * FROM customer";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                int colID = resultSet.getInt("colID");
                String nic = resultSet.getString("nic");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String email = resultSet.getString("email");
                String phone = resultSet.getString("phone");
                String address = resultSet.getString("address");
                String gender = resultSet.getString("gender");

                customerList.add(new Customer(colID, nic, firstName, lastName, email, phone, address, gender));
            }

            customerTable.setItems(customerList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while loading customer data.");
        }
    }


    @FXML
    private void handleSearch(ActionEvent event) {
        String nicSearchQuery = searchTxt.getText().trim();

        if (nicSearchQuery.isEmpty()) {

            loadCustomerData();
        } else {
            filterCustomerData(nicSearchQuery);
        }
    }


    private void filterCustomerData(String nicSearchQuery) {
        customerList.clear();

        try (Connection connection = new DBConnection().getConnection()) {

            String sql = "SELECT * FROM customer WHERE nic LIKE ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, "%" + nicSearchQuery + "%");

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int colID = resultSet.getInt("colID");
                String nic = resultSet.getString("nic");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String email = resultSet.getString("email");
                String phone = resultSet.getString("phone");
                String address = resultSet.getString("address");
                String gender = resultSet.getString("gender");

                customerList.add(new Customer(colID, nic, firstName, lastName, email, phone, address, gender));
            }

            customerTable.setItems(customerList);


            if (!customerList.isEmpty()) {
                Customer selectedCustomer = customerList.get(0);
                populateFormFields(selectedCustomer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while searching the customer.");
        }
    }


    private void populateFormFields(Customer customer) {
        nicField.setText(customer.getNic());
        firstNameField.setText(customer.getFirstName());
        lastNameField.setText(customer.getLastName());
        emailField.setText(customer.getEmail());
        tpNoField.setText(customer.getPhone());
        addressField.setText(customer.getAddress());

        if ("Male".equalsIgnoreCase(customer.getGender())) {
            maleRadioButton.setSelected(true);
            femaleRadioButton.setSelected(false);
        } else if ("Female".equalsIgnoreCase(customer.getGender())) {
            femaleRadioButton.setSelected(true);
            maleRadioButton.setSelected(false);
        }
    }



    private void clearFields() {
        nicField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        tpNoField.clear();
        addressField.clear();
        maleRadioButton.setSelected(false);
        femaleRadioButton.setSelected(false);
    }

    private int getSelectedCustomerId() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a customer from the table.");
            return -1;
        }
        return selectedCustomer.getColID();
    }

    private boolean validateInputs() {


        if (nicField.getText().isBlank() || !nicField.getText().matches("\\d{9}[VvXx]|\\d{12}")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid NIC");
            nicField.requestFocus();
            return false;
        }


        if (firstNameField.getText().isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter First Name.");
            firstNameField.requestFocus();
            return false;
        }


        if (lastNameField.getText().isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter Last Name.");
            lastNameField.requestFocus();
            return false;
        }


        if (!maleRadioButton.isSelected() && !femaleRadioButton.isSelected()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select Gender.");
            maleRadioButton.requestFocus();
            return false;
        }


        if (emailField.getText().isBlank() || !emailField.getText().matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid Email address.");
            emailField.requestFocus();
            return false;
        }


        if (addressField.getText().isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter Address.");
            addressField.requestFocus();
            return false;
        }


        if (tpNoField.getText().isBlank() || !tpNoField.getText().matches("\\d{10}")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid 10-digit Telephone Number.");
            tpNoField.requestFocus();
            return false;
        }



        return true;
    }
    @FXML
    private void clearFieldsAction() {
        nicField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        tpNoField.clear();
        addressField.clear();
        maleRadioButton.setSelected(false);
        femaleRadioButton.setSelected(false);
    }


    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
