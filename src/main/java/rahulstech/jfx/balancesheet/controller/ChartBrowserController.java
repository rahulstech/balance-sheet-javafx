package rahulstech.jfx.balancesheet.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.scene.layout.VBox;
import rahulstech.jfx.balancesheet.util.ViewLoader;

import java.util.ResourceBundle;

@SuppressWarnings("ALL")
public class ChartBrowserController extends Controller {

    private ObservableList<String> CHARTS = FXCollections.observableArrayList(
            "Chart by Type Monthly",
            "Chart by Category Monthly",
            "Chart by Category Budget"
    );

    @FXML
    private ListView<String> chartListView;

    @FXML
    private VBox chartPane;

    private MonthlyTypeChartController monthlyTypeChartController;

    private MonthlyCategoryChartController monthlyCategoryChartController;

    private CategoryBudgetChartController categoryBudgetChartController;

    @Override
    protected void onInitialize(ResourceBundle res) {
        chartListView.setItems(CHARTS);
        chartListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            displayChart(chartListView.getSelectionModel().getSelectedIndex());
        });
    }

    private void displayChart(int selection) {
        if (selection == 0) {
            createMonthlyTypeChart();
        }
        else if (selection == 1) {
            createMonthlyCategoryChart();
        }
        else if (selection == 2) {
            createCategoryBudgetChart();
        }
    }

    private void createMonthlyTypeChart() {
        if (null == monthlyTypeChartController) {
            ViewLoader loader = getViewLoader().setFxml("monthly_type_chart.fxml").load();
            this.monthlyTypeChartController = loader.getController();
        }
        Parent root = this.monthlyTypeChartController.getRoot();
        chartPane.getChildren().clear();
        chartPane.getChildren().add(root);
    }

    private void createMonthlyCategoryChart() {
        if (null == monthlyCategoryChartController) {
            ViewLoader loader = getViewLoader().setFxml("monthly_category_chart.fxml").load();
            this.monthlyCategoryChartController = loader.getController();
        }
        Parent root = this.monthlyCategoryChartController.getRoot();
        chartPane.getChildren().clear();
        chartPane.getChildren().add(root);
    }

    private void createCategoryBudgetChart() {
        if (null == categoryBudgetChartController) {
            ViewLoader loader = getViewLoader().setFxml("category_budget_chart.fxml").load();
            this.categoryBudgetChartController = loader.getController();
        }
        Parent root = this.categoryBudgetChartController.getRoot();
        chartPane.getChildren().clear();
        chartPane.getChildren().add(root);
    }}
