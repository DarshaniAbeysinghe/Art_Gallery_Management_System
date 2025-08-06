package com.example.art_gallery_management_system;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PaintingSaleController {

    @FXML
    private LineChart<String, Number> salesLineChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private Text titleText;

    @FXML
    private ComboBox<String> weekComboBox;

    @FXML
    private ComboBox<String> monthComboBox;

    private DBConnection dbConnection;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void initialize() {
        dbConnection = new DBConnection();

        populateWeekComboBox();
        populateMonthComboBox();

        weekComboBox.setValue("This Week");
        filterByWeek("This Week");

        weekComboBox.setOnAction(event -> filterByWeek(weekComboBox.getValue()));
        monthComboBox.setOnAction(event -> filterByMonth(monthComboBox.getValue()));
    }

    private void populateWeekComboBox() {
        ObservableList<String> weeks = FXCollections.observableArrayList(
                "This Week", "Previous Week", "Week 1", "Week 2", "Week 3", "Week 4"
        );
        weekComboBox.setItems(weeks);
    }

    private void populateMonthComboBox() {
        ObservableList<String> months = FXCollections.observableArrayList(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        );
        monthComboBox.setItems(months);
    }

    private void filterByWeek(String selectedWeek) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek;
        LocalDate endOfWeek;

        switch (selectedWeek) {
            case "This Week":

                startOfWeek = today.minusDays(today.getDayOfWeek().getValue() % 7);
                endOfWeek = startOfWeek.plusDays(6);
                break;
            case "Previous Week":

                startOfWeek = today.minusWeeks(1).minusDays(today.getDayOfWeek().getValue() % 7);
                endOfWeek = startOfWeek.plusDays(6);
                break;
            case "Week 1":
                startOfWeek = today.withDayOfMonth(1);
                endOfWeek = startOfWeek.plusDays(6);
                break;
            case "Week 2":
                startOfWeek = today.withDayOfMonth(1).plusWeeks(1);
                endOfWeek = startOfWeek.plusDays(6);
                break;
            case "Week 3":
                startOfWeek = today.withDayOfMonth(1).plusWeeks(2);
                endOfWeek = startOfWeek.plusDays(6);
                break;
            case "Week 4":
                startOfWeek = today.withDayOfMonth(1).plusWeeks(3);
                endOfWeek = startOfWeek.plusDays(6);
                break;
            default:
                return;
        }


        loadChartData(startOfWeek, endOfWeek);
    }

    private void filterByMonth(String selectedMonth) {
        int monthNumber = java.time.Month.valueOf(selectedMonth.toUpperCase()).getValue();
        LocalDate startOfMonth = LocalDate.now().withMonth(monthNumber).withDayOfMonth(1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        loadChartData(startOfMonth, endOfMonth);
    }

    private void loadChartData(LocalDate startDate, LocalDate endDate) {
        String query = "SELECT DATE(order_date) AS sale_date, SUM(total_amount) AS total_sales " +
                "FROM placeorder " +
                "WHERE order_date BETWEEN ? AND ? " +
                "GROUP BY DATE(order_date)";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setDate(1, java.sql.Date.valueOf(startDate));
            statement.setDate(2, java.sql.Date.valueOf(endDate));

            ResultSet resultSet = statement.executeQuery();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            List<String> allDates = getAllDatesInRange(startDate, endDate);


            while (resultSet.next()) {
                String date = resultSet.getString("sale_date");
                double totalSales = resultSet.getDouble("total_sales");
                series.getData().add(new XYChart.Data<>(date, totalSales));
                allDates.remove(date);
            }


            for (String missingDate : allDates) {
                series.getData().add(new XYChart.Data<>(missingDate, 0));
            }

            // Sort data by date
            series.getData().sort((d1, d2) -> LocalDate.parse(d1.getXValue(), DATE_FORMAT)
                    .compareTo(LocalDate.parse(d2.getXValue(), DATE_FORMAT)));

            salesLineChart.getData().clear();
            salesLineChart.getData().add(series);
            xAxis.setLabel("Date");
            yAxis.setLabel("Total Sales");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<String> getAllDatesInRange(LocalDate startDate, LocalDate endDate) {
        List<String> dates = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            dates.add(current.format(DATE_FORMAT));
            current = current.plusDays(1);
        }

        return dates;
    }

    @FXML
    private void onWeekSelectionChanged() {

    }

    @FXML
    private void onMonthSelectionChanged() {

    }
}
