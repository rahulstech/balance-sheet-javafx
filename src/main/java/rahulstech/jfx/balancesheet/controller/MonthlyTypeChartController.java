package rahulstech.jfx.balancesheet.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ComboBoxListCell;
import rahulstech.jfx.balancesheet.BalancesheetApp;
import rahulstech.jfx.balancesheet.concurrent.TaskUtils;
import rahulstech.jfx.balancesheet.database.model.MonthlyTypeModel;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.TransactionType;
import rahulstech.jfx.balancesheet.util.DialogUtil;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Future;

public class MonthlyTypeChartController extends Controller {

    private final DateTimeFormatter YEAR_MONTH_FORMAT_CHART = DateTimeFormatter.ofPattern("MMM-yy");
    private final DateTimeFormatter YEAR_MONTH_FORMAT_DROPDOWN = DateTimeFormatter.ofPattern("MMMM-yyyy");

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private ComboBox<YearMonth> monthStartComboBox;

    @FXML
    private ComboBox<YearMonth> monthEndComboBox;

    @FXML
    private ComboBox<TransactionType> transactionTypeComboBox;

    @FXML
    private Button createChartButton;

    private Future<?> currentTask;

    public MonthlyTypeChartController() {}

    @Override
    protected void onInitialize(ResourceBundle resources) {
        prepareChart();
        // Populate the dropdowns
        populateComboBoxes();
        // Set event handler for create chart button
        createChartButton.setOnAction(event -> createChart());
    }

    private void prepareChart() {
        yAxis.setLabel("Total Amount");
        xAxis.setLabel("Month-Year");
        barChart.setAnimated(false);
    }

    private ComboBoxListCell<YearMonth> newComboBoxListCellForYearMonth() {
        return new ComboBoxListCell<>() {
            @Override
            public void updateItem(YearMonth yearMonth, boolean empty) {
                super.updateItem(yearMonth, empty);
                if (empty) {
                    setText(null);
                }
                else {
                    setText(yearMonth.format(YEAR_MONTH_FORMAT_DROPDOWN));
                }
            }
        };
    }

    private void populateComboBoxes() {
        monthStartComboBox.setCellFactory(yearMonthListView -> newComboBoxListCellForYearMonth());
        monthStartComboBox.setButtonCell(newComboBoxListCellForYearMonth());
        monthEndComboBox.setCellFactory(yearMonthListView -> newComboBoxListCellForYearMonth());
        monthEndComboBox.setButtonCell(newComboBoxListCellForYearMonth());

        // Populate month start and month end combo-boxes with sample data
        BalancesheetApp.getAppExecutor().submit(TaskUtils.getMinMaxHistoryDateQueryTask(task -> {
            YearMonth[] result = task.getValue();
            YearMonth minMonth = result[0];
            YearMonth maxMonth = result[1];
            List<YearMonth> months = new ArrayList<>();
            // Loop from start to end and add each YearMonth to the list
            for (YearMonth yearMonth = minMonth; !yearMonth.isAfter(maxMonth); yearMonth = yearMonth.plusMonths(1)) {
                months.add(yearMonth);
            }
            monthStartComboBox.setItems(FXCollections.observableArrayList(months));
            monthEndComboBox.setItems(FXCollections.observableArrayList(months));
            monthStartComboBox.getSelectionModel().selectFirst();
            monthEndComboBox.getSelectionModel().selectFirst();
        },
                task -> System.err.println(task.getException())));

        // Populate transaction type combo-box
        transactionTypeComboBox.setItems(FXCollections.observableArrayList(TransactionType.values()));
        transactionTypeComboBox.getSelectionModel().selectFirst();
    }

    private void createChart() {
        // Get selected values from dropdowns
        YearMonth monthStart = monthStartComboBox.getValue();
        YearMonth monthEnd = monthEndComboBox.getValue();
        TransactionType transactionType = transactionTypeComboBox.getValue();

        // Check if monthStart is less than or equal to monthEnd
        if (!isMonthStartBeforeOrEqualMonthEnd(monthStart, monthEnd)) {
            showAlert("Invalid selection", "Month Start should be before or equal to Month End.");
            return;
        }

        // Filter data based on selected values
        filterData(monthStart, monthEnd, transactionType);
    }

    private boolean isMonthStartBeforeOrEqualMonthEnd(YearMonth monthStart, YearMonth monthEnd) {
        // Implement logic to check if monthStart is before or equal to monthEnd
        return monthStart.isBefore(monthEnd) || monthStart.equals(monthEnd);
    }

    private void filterData(YearMonth monthStart, YearMonth monthEnd, TransactionType type) {
        if (null != currentTask) {
            currentTask.cancel(true);
        }
        currentTask = BalancesheetApp.getAppExecutor()
                .submit(TaskUtils.getMonthlyTypeChartQueryTask(monthStart,monthEnd,type,
                        task->prepareChartData(type,task.getValue()),
                        task -> {
                            System.err.println(task.getException());
                            showAlert("Error!","Unable to fetch chart data. Please try again");
                        }));
    }

    @SuppressWarnings("unchecked")
    private void prepareChartData(TransactionType type, List<MonthlyTypeModel> monthlyData) {
        barChart.getData().clear();
        if (null == monthlyData || monthlyData.isEmpty()) {
            return;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        XYChart.Series<String,Number> avgSeries = new XYChart.Series<>();
        series.setName(type.name());
        avgSeries.setName("Average "+type.name());

        barChart.getData().addAll(series,avgSeries);

        ObservableList<XYChart.Data<String, Number>> data = FXCollections.observableArrayList();
        ObservableList<XYChart.Data<String,Number>> avgData = FXCollections.observableArrayList();

        Currency total = Currency.ZERO;
        for (MonthlyTypeModel model : monthlyData) {
            Currency amount = model.getTotalAmount();
            total = total.add(model.getTotalAmount());
            XYChart.Data<String,Number> dataPoint = new XYChart.Data<>(model.getYearMonth().format(YEAR_MONTH_FORMAT_CHART), amount.getValue());
            data.add(dataPoint);
            series.getData().add(dataPoint);
            Tooltip tooltip = new Tooltip(amount.toString());
            Tooltip.install(dataPoint.getNode(),tooltip);
        }
        Currency average = total.divide(Currency.from(monthlyData.size()));
        XYChart.Data<String,Number> avgDataPoint = new XYChart.Data<>("Average",average.getValue());
        avgData.add(avgDataPoint);
        avgSeries.getData().add(avgDataPoint);
        Tooltip tooltip = new Tooltip(average.toString());
        Tooltip.install(avgDataPoint.getNode(),tooltip);
    }

    private void showAlert(String title, String message) {
        DialogUtil.alertError(getWindow(),title,message);
    }
}
