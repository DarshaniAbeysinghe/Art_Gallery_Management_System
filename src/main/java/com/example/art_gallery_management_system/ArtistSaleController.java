package com.example.art_gallery_management_system;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;

import java.sql.*;
import java.time.LocalDate;
import java.time.Month;

public class ArtistSaleController {

    @FXML
    private ComboBox<String> monthComboBox;

    @FXML
    private ComboBox<Integer> yearComboBox;

    @FXML
    private PieChart sharePieChart;

    private DBConnection dbConnection;

    public void initialize() {
        dbConnection = new DBConnection();
        populateMonthComboBox();
        populateYearComboBox();

        String currentMonth = LocalDate.now().getMonth().toString();
        monthComboBox.setValue(currentMonth.substring(0, 1).toUpperCase() + currentMonth.substring(1).toLowerCase());

        int currentYear = LocalDate.now().getYear();
        yearComboBox.setValue(currentYear);

        loadShareDistributionData();
    }

    private void populateMonthComboBox() {
        ObservableList<String> months = FXCollections.observableArrayList(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        );
        monthComboBox.setItems(months);
    }

    private void populateYearComboBox() {
        ObservableList<Integer> years = FXCollections.observableArrayList();
        int currentYear = LocalDate.now().getYear();

        for (int year = currentYear; year >= currentYear - 5; year--) {
            years.add(year);
        }
        yearComboBox.setItems(years);
    }

    @FXML
    private void loadShareDistributionData() {
        String selectedMonth = monthComboBox.getValue();
        Integer selectedYear = yearComboBox.getValue();
        if (selectedMonth != null && selectedYear != null) {
            int monthNumber = Month.valueOf(selectedMonth.toUpperCase()).getValue();

            fetchShareDataForYearAndMonth(selectedYear, monthNumber);
        }
    }

    private void fetchShareDataForYearAndMonth(int year, int monthNumber) {

        String query = "SELECT o.artist_share, o.gallery_share FROM placeorder p " +
                "JOIN `order` o ON p.OrderID = o.OrderID " +
                "WHERE YEAR(p.order_date) = ? AND MONTH(p.order_date) = ?";
        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, year);
            statement.setInt(2, monthNumber);
            ResultSet resultSet = statement.executeQuery();

            double artistTotalShare = 0.0;
            double galleryTotalShare = 0.0;

            while (resultSet.next()) {
                artistTotalShare += resultSet.getDouble("artist_share");
                galleryTotalShare += resultSet.getDouble("gallery_share");
            }

            updatePieChart(artistTotalShare, galleryTotalShare, year);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void updatePieChart(double artistShare, double galleryShare, int year) {

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Artist Share " + year, artistShare),
                new PieChart.Data("Gallery Share " + year, galleryShare)
        );
        sharePieChart.setData(pieChartData);

        for (PieChart.Data data : pieChartData) {
            data.setName(data.getName() + "\n" + String.format("%.2f", data.getPieValue()));
        }
    }
}
