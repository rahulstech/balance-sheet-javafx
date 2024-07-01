package rahulstech.jfx.balancesheet.controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import rahulstech.jfx.balancesheet.concurrent.TaskUtils;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.database.model.CategoryBudgetModel;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.Log;

import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Future;

@SuppressWarnings("ALL")
public class CategoryBudgetChartController extends BaseCategoryChartController {

    private static final String TAG = CategoryBudgetChartController.class.getSimpleName();

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private FlowPane selectedCategoriesPanel;

    @FXML
    private BarChart<String, Number> categoryBudgetChart;

    private Future<?> queryTask;

    @Override
    protected void onInitialize(ResourceBundle res) {
        prepareChart();
    }

    private void prepareChart() {
        categoryBudgetChart.setAnimated(false);
    }

    @FXML
    private void handleCreateChartButtonClick(ActionEvent event) {
        if (null!= queryTask) {
            queryTask.cancel(true);
            queryTask = null;
        }

        LocalDate start = getStartDate();
        LocalDate end = getEndDate();
        List<Category> categories = getSelectedCategories();
        if (null==start) {
            DialogUtil.alertError(getWindow(),"Input Error","Start date not provided");
            return;
        }
        if (null==end) {
            DialogUtil.alertError(getWindow(),"Input Error","No category selected. Choose at least one category");
            return;
        }
        if (!start.isBefore(end)) {
            DialogUtil.alertError(getWindow(),"Input Error","Start date must be before end date start="+start+" end="+end);
            return;
        }
        if (null==categories || categories.isEmpty()) {
            DialogUtil.alertError(getWindow(),"Input Error","End date not provided");
        }
        Task<List<CategoryBudgetModel>> task = TaskUtils.getCategoryBudgetChartData(start,end,categories, t->{
            setChartData(t.getValue());
        },t->{
            Log.error(TAG,"handleCreateChartButtonClick",t.getException());
        });
        queryTask = getApp().getAppExecutor().submit(task);
    }

    @SuppressWarnings("unchecked")
    private void setChartData(List<CategoryBudgetModel> items) {

        categoryBudgetChart.getData().clear();

        XYChart.Series<String,Number> seriesCategory = new XYChart.Series<>();
        XYChart.Series<String,Number> seriesBudget = new XYChart.Series<>();

        seriesCategory.setName("Category Total");
        seriesBudget.setName("Budget");

        categoryBudgetChart.getData().addAll(seriesCategory,seriesBudget);

        for (CategoryBudgetModel item : items) {
            XYChart.Data<String, Number> dataCatrgory = new XYChart.Data<>();
            dataCatrgory.setXValue(item.getCategoryName());
            dataCatrgory.setYValue(item.getCategoryTotalAmount().getValue());
            seriesCategory.getData().add(dataCatrgory);

            Tooltip tooltipCategory = new Tooltip(item.getCategoryTotalAmount().toString());
            Tooltip.install(dataCatrgory.getNode(),tooltipCategory);

            XYChart.Data<String,Number> dataBudget = new XYChart.Data<>();
            dataBudget.setXValue(item.getCategoryName());
            dataBudget.setYValue(item.getBudgetTotalAmount().getValue());
            seriesBudget.getData().add(dataBudget);

            Tooltip tooltipBudget = new Tooltip(item.getBudgetTotalAmount().toString());
            Tooltip.install(dataBudget.getNode(),tooltipBudget);
        }
    }

    private void setStartDate(LocalDate date) {
        startDatePicker.setValue(date);
    }

    private LocalDate getStartDate() {
        return startDatePicker.getValue();
    }

    private LocalDate getEndDate() {
        return endDatePicker.getValue();
    }

    private void setEndDate(LocalDate date) {
        endDatePicker.setValue(date);
    }
}
