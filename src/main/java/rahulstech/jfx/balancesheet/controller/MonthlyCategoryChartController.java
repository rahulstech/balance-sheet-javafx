package rahulstech.jfx.balancesheet.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import rahulstech.jfx.balancesheet.concurrent.TaskUtils;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.database.model.MonthlyCategoryModel;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.ViewLauncher;
import rahulstech.jfx.balancesheet.view.Chip;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Future;

import static rahulstech.jfx.balancesheet.concurrent.TaskUtils.createTask;

@SuppressWarnings("ALL")
public class MonthlyCategoryChartController extends Controller {

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

    private HashSet<Category> selectedCategories = new HashSet<>();

    private Future<?> chartDataTask;

    @Override
    protected void onInitialize(ResourceBundle res) {
        prepareChart();
        populateComboBoxes();
    }

    private void setMonthlyCategoryData(List<MonthlyCategoryModel> data) {
        Task<Map<String, Map<String, Currency>>> task = createTask(()->{
                Map<String, Map<String, Currency>> monthCategoryAmount = new HashMap<>();

                for (MonthlyCategoryModel model : data) {
                    String month = model.getYearMonth().format(YEAR_MONTH_FORMAT_CHART);
                    String category = model.getCategory().getName();
                    Currency amount = model.getTotalAmount();

                    monthCategoryAmount
                            .computeIfAbsent(month, k -> new HashMap<>())
                            .put(category, amount);
                }

                return monthCategoryAmount;
            },t->{

            Map<String, Map<String, Currency>> monthCategoryAmount = t.getValue();

            monthlyCategoryChart.getData().clear();
            for (Map.Entry<String, Map<String, Currency>> entry : monthCategoryAmount.entrySet()) {
                String month = entry.getKey();
                Map<String, Currency> categories = entry.getValue();

                for (Map.Entry<String, Currency> categoryEntry : categories.entrySet()) {
                    String category = categoryEntry.getKey();
                    Currency amount = categoryEntry.getValue();

                    XYChart.Series<String, Number> series = monthlyCategoryChart.getData()
                            .stream()
                            .filter(s -> s.getName().equals(category))
                            .findFirst()
                            .orElseGet(() -> {
                                XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
                                newSeries.setName(category);
                                monthlyCategoryChart.getData().add(newSeries);
                                return newSeries;
                            });

                    XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(month, amount.getValue());
                    series.getData().add(dataPoint);

                    Tooltip tooltip = new Tooltip(amount.toString());
                    Tooltip.install(dataPoint.getNode(), tooltip);
                }
            }

            // Calculate and add average series
            Map<String, Currency> categoryTotals = new HashMap<>();
            Map<String, Integer> categoryCounts = new HashMap<>();

            for (Map<String, Currency> categories : monthCategoryAmount.values()) {
                for (Map.Entry<String, Currency> categoryEntry : categories.entrySet()) {
                    categoryTotals.merge(categoryEntry.getKey(), categoryEntry.getValue(), Currency::add);
                    categoryCounts.merge(categoryEntry.getKey(), 1, Integer::sum);
                }
            }

            for (Map.Entry<String, Currency> totalEntry : categoryTotals.entrySet()) {
                String category = totalEntry.getKey();
                Currency total = totalEntry.getValue();
                int count = categoryCounts.get(category);
                String seriesName = "Average "+category;

                XYChart.Series<String, Number> averageSeries = new XYChart.Series<>();
                averageSeries.setName(seriesName);
                monthlyCategoryChart.getData().add(averageSeries);

                Currency average = total.divide(Currency.from(count));
                XYChart.Data<String, Number> avgDataPoint = new XYChart.Data<>("Average", average.getValue());
                averageSeries.getData().add(avgDataPoint);

                Tooltip avgTooltip = new Tooltip(average.toString());
                Tooltip.install(avgDataPoint.getNode(), avgTooltip);
            }
        },t->t.getException().printStackTrace());
        getApp().getAppExecutor().submit(task);
    }

    @FXML
    private void handleAddCategoryButtonClick() {
        // For now, just add a new chip with sample text
        ViewLauncher launcher = getViewLauncherBuilder()
                .setFxml("category.fxml")
                .setTitle("Choose Category")
                .build().load();
        CategoryController controller = launcher.getController();
        controller.getCategoryList().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Stage window = controller.getWindow();
        window.show();
        window.setOnCloseRequest(e->{
            ObservableList<Category> selections = controller.getCategoryList().getSelectionModel().getSelectedItems();
            addCategories(new ArrayList<>(selections));
        });
    }

    private void addCategories(List<Category> categories) {
        for (Category category : categories) {
            if (selectedCategories.add(category)) {
                addCategoryChip(category);
            }
        }
    }

    private void addCategoryChip(Category category) {
        Chip chip = new Chip(category.getName());
        chip.setCloseListener(v->{
            selectedCategories.remove(category);
        });
        selectedCategoriesPanel.getChildren().add(chip);
    }

    @FXML
    private void handleCreateChartButtonClick(ActionEvent event) {
        if (null != chartDataTask) {
            chartDataTask.cancel(true);
        }
        YearMonth startMonth = startMonthComboBox.getValue();
        YearMonth endMonth = endMonthComboBox.getValue();
        List<Category> categories = selectedCategories.stream().toList();
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
                t->t.getException().printStackTrace());
        chartDataTask = getApp().getAppExecutor().submit(task);
    }

    private void populateComboBoxes() {
        startMonthComboBox.setCellFactory(yearMonthListView -> newComboBoxListCellForYearMonth());
        startMonthComboBox.setButtonCell(newComboBoxListCellForYearMonth());
        endMonthComboBox.setCellFactory(yearMonthListView -> newComboBoxListCellForYearMonth());
        endMonthComboBox.setButtonCell(newComboBoxListCellForYearMonth());

        // Populate month start and month end combo-boxes with sample data
        getApp().getAppExecutor().submit(TaskUtils.getMinMaxHistoryDateQueryTask(task -> {
                    YearMonth[] result = task.getValue();
                    YearMonth minMonth = result[0];
                    YearMonth maxMonth = result[1];
                    List<YearMonth> months = new ArrayList<>();
                    // Loop from start to end and add each YearMonth to the list
                    for (YearMonth yearMonth = minMonth; !yearMonth.isAfter(maxMonth); yearMonth = yearMonth.plusMonths(1)) {
                        months.add(yearMonth);
                    }
                    startMonthComboBox.setItems(FXCollections.observableArrayList(months));
                    endMonthComboBox.setItems(FXCollections.observableArrayList(months));
                    startMonthComboBox.getSelectionModel().selectFirst();
                    endMonthComboBox.getSelectionModel().selectFirst();
                },
                task -> task.getException().printStackTrace()));
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

    private void prepareChart() {
        Axis<Number> yAxis = monthlyCategoryChart.getYAxis();
        Axis<String> xAxis = monthlyCategoryChart.getXAxis();
        yAxis.setLabel("Total Amount");
        xAxis.setLabel("Month-Year");
        monthlyCategoryChart.setAnimated(false);
    }
}
