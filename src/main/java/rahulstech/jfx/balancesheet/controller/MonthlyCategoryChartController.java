package rahulstech.jfx.balancesheet.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.FlowPane;
import rahulstech.jfx.balancesheet.concurrent.TaskUtils;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.database.model.MonthlyCategoryModel;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.Log;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Future;

@SuppressWarnings("ALL")
public class MonthlyCategoryChartController extends BaseCategoryChartController {

    private static final String TAG = MonthlyCategoryChartController.class.getSimpleName();

    private final DateTimeFormatter YEAR_MONTH_FORMAT_CHART = DateTimeFormatter.ofPattern("MMM-yy");

    private final DateTimeFormatter YEAR_MONTH_FORMAT_DROPDOWN = DateTimeFormatter.ofPattern("MMMM-yyyy");

    @FXML
    private ComboBox<YearMonth> startMonthComboBox;

    @FXML
    private ComboBox<YearMonth> endMonthComboBox;

    @FXML
    private FlowPane selectedCategoriesPanel;

    @FXML
    private BarChart<String, Number> monthlyCategoryChart;

    private Future<?> chartDataTask;

    @Override
    protected void onInitialize(ResourceBundle res) {
        prepareChart();
        populateComboBoxes();
    }

    private void setMonthlyCategoryData(List<MonthlyCategoryModel> models) {

        monthlyCategoryChart.getData().clear();

        Map<String,Currency> category_sum = new HashMap<>();
        Map<String,Long> category_count = new HashMap<>();

        for (MonthlyCategoryModel model : models) {
            String category = model.getCategory().getName();
            String month = model.getYearMonth().format(YEAR_MONTH_FORMAT_CHART);
            Currency amount = model.getTotalAmount();

            XYChart.Series<String,Number> series = monthlyCategoryChart.getData()
                    .stream().filter(d->category.equals(d.getName())).findFirst()
                    .orElseGet(()->{
                        XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
                        newSeries.setName(category);
                        monthlyCategoryChart.getData().add(newSeries);
                        return newSeries;
                    });
            XYChart.Data<String,Number> data = new XYChart.Data<>();
            data.setXValue(month);
            data.setYValue(amount.getValue());
            series.getData().add(data);

            Tooltip tooltip = new Tooltip(amount.toString());
            Tooltip.install(data.getNode(),tooltip);

            Currency sum = category_sum.getOrDefault(category,Currency.ZERO);
            Long count = category_count.getOrDefault(category,0L);
            sum = sum.add(amount);
            count++;
            category_sum.put(category,sum);
            category_count.put(category,count);
        }

        for (String category : category_sum.keySet()) {
            Currency sum = category_sum.get(category);
            Long count = category_count.get(category);
            Currency average = sum.divide(Currency.from(count));

            XYChart.Series<String,Number> series = monthlyCategoryChart.getData()
                    .stream().filter(d->category.equals(d.getName())).findFirst().get();
            XYChart.Data<String,Number> data = new XYChart.Data<>();
            data.setXValue("Average");
            data.setYValue(average.getValue());
            series.getData().add(data);

            Tooltip tooltip = new Tooltip(average.toString());
            Tooltip.install(data.getNode(),tooltip);
        }
    }

    @FXML
    private void handleCreateChartButtonClick(ActionEvent event) {
        if (null != chartDataTask) {
            chartDataTask.cancel(true);
        }
        YearMonth startMonth = startMonthComboBox.getValue();
        YearMonth endMonth = endMonthComboBox.getValue();
        List<Category> categories = getSelectedCategories();
        if (categories.isEmpty()) {
            DialogUtil.alertError(getWindow(),"Chart Error","Choose at least one category to create chart");
            return;
        }
        else if (endMonth.compareTo(startMonth) < 0) {
            DialogUtil.alertError(getWindow(),"Chart Error","End month must be the same or a later month");
            return;
        }
        Task<List<MonthlyCategoryModel>> task = TaskUtils.getMonthlyCategoroyChartQueryTask(startMonth,endMonth,categories,
                t-> setMonthlyCategoryData(t.getValue()),
                t->Log.error(TAG,"monthly category chart data query task",t.getException()));
        chartDataTask = getApp().getAppExecutor().submit(task);
    }

    private void populateComboBoxes() {
        startMonthComboBox.setCellFactory(yearMonthListView -> newComboBoxListCellForYearMonth());
        startMonthComboBox.setButtonCell(newComboBoxListCellForYearMonth());
        endMonthComboBox.setCellFactory(yearMonthListView -> newComboBoxListCellForYearMonth());
        endMonthComboBox.setButtonCell(newComboBoxListCellForYearMonth());

        // Populate month start and month end combo-boxes with sample data
        getApp().getAppExecutor().submit(TaskUtils.getYearMonthsListBetweenMinAndMaxHistoryDate(false,
                task -> setComboBoxItems(task.getValue()),
                task -> Log.error(TAG,"get min max month task",task.getException())));
    }

    private void setComboBoxItems(List<YearMonth> months) {
        startMonthComboBox.setItems(FXCollections.observableArrayList(months));
        endMonthComboBox.setItems(FXCollections.observableArrayList(months));
        startMonthComboBox.getSelectionModel().selectFirst();
        endMonthComboBox.getSelectionModel().selectFirst();
    }

    private ComboBoxListCell<YearMonth> newComboBoxListCellForYearMonth() {
        return new ComboBoxListCell<YearMonth>() {
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

    private void prepareChart() {
        Axis<Number> yAxis = monthlyCategoryChart.getYAxis();
        Axis<String> xAxis = monthlyCategoryChart.getXAxis();
        yAxis.setLabel("Total Amount");
        xAxis.setLabel("Month-Year");
        monthlyCategoryChart.setAnimated(false);
    }
}
