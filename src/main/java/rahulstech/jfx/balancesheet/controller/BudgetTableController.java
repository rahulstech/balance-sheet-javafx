package rahulstech.jfx.balancesheet.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import rahulstech.jfx.balancesheet.concurrent.TaskUtils;
import rahulstech.jfx.balancesheet.database.dao.BudgetFilterData;
import rahulstech.jfx.balancesheet.database.entity.Budget;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.Log;
import rahulstech.jfx.balancesheet.util.TextUtil;
import rahulstech.jfx.balancesheet.util.ViewLauncher;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Future;

@SuppressWarnings("ALL")
public class BudgetTableController extends Controller {

    private static final String TAG = BudgetFilterController.class.getSimpleName();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    @FXML private TableView<Budget> budgetTableView;
    @FXML private TableColumn<Budget, String> categoryColumn;
    @FXML private TableColumn<Budget, LocalDate> startDateColumn;
    @FXML private TableColumn<Budget, LocalDate> endDateColumn;
    @FXML private TableColumn<Budget, String> descriptionColumn;
    @FXML private TableColumn<Budget, Currency> amountColumn;

    private BudgetFilterController budgetFilterController;

    private Future<?> filterTask;

    private BudgetFilterData oldData;

    @Override
    protected void onInitialize(ResourceBundle resources) {
        // Initialize columns
        // Set cell value factories manually
        categoryColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCategory().getName()));

        startDateColumn.setCellValueFactory(param ->{
            Budget budget = param.getValue();
            LocalDate cell_value = null==budget ? null : budget.getStartDate();
            return new SimpleObjectProperty<>(cell_value);
        });
        startDateColumn.setCellFactory(param -> new TableCell<>(){
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                }
                else {
                    setText(item.format(DATE_FORMATTER));
                }
            }
        });

        endDateColumn.setCellValueFactory(param -> {
            Budget budget = param.getValue();
            LocalDate cell_value = null==budget ? null : budget.getEndDate();
            return new SimpleObjectProperty<>(cell_value);
        });
        endDateColumn.setCellFactory(param -> new TableCell<>(){
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                }
                else {
                    setText(item.format(DATE_FORMATTER));
                }
            }
        });

        descriptionColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDescription()));

        descriptionColumn.setCellFactory(param -> new TableCell<Budget,String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item==null || item.isEmpty()) {
                    setText(null);
                    setTooltip(null);
                }
                else {
                    Tooltip tooltip = new Tooltip(item);
                    setText(item);
                    setTooltip(tooltip);
                }
            }
        });

        amountColumn.setCellValueFactory(param -> {
            Budget budget = param.getValue();
            Currency cell_value = null==budget ? Currency.ZERO : budget.getAmount();
            return new SimpleObjectProperty<>(cell_value);
        });
        amountColumn.setCellFactory(param -> new TableCell<>(){
            @Override
            protected void updateItem(Currency item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                }
                else {
                    setText(TextUtil.prettyPrintCurrency(item));
                }
            }
        });
    }

    @FXML
    private void handleAddButtonAction() {
        ViewLauncher launcher = getViewLauncherBuilder()
                .setFxml("input_budget.fxml")
                .setTitle("Add Budget")
                .build();
        launcher.load();
        launcher.getWindow().show();
    }

    @FXML
    private void handleDeleteSelectedButtonAction(ActionEvent event) {
        List<Budget> budgets = budgetTableView.getSelectionModel().getSelectedItems();
        if (budgets==null || budgets.isEmpty()) {
            return;
        }
        DialogUtil.alertConfirmation(getWindow(),"Warning","Selected budget(s) will be deleted permanently. Are you sure to proceed?",
                "Yes Delete",()->{
                    Task<Boolean> task = TaskUtils.deleteBudgets(budgets,t->{
                        if (null!=t.getValue() && t.getValue()) {
                            budgetTableView.getItems().removeAll(budgets);
                        }
                        else {
                            Log.info(TAG,"unable to delete budets");
                        }
                    },t->Log.error(TAG,"delete budgets",t.getException()));
                    getApp().getAppExecutor().submit(task);
                },
                "No Cancel",null);
    }

    @FXML
    private void handleEditButtonAction(ActionEvent event) {
        if (budgetTableView.getSelectionModel().getSelectedItems().size()!=1) {
            return;
        }
        Budget selected = budgetTableView.getSelectionModel().getSelectedItem();
        ViewLauncher launcher = getViewLauncherBuilder()
                .setFxml("input_budget.fxml")
                .setTitle("Add Budget")
                .setHeight(400)
                .setWidth(600)
                .build();
        launcher.load();
        launcher.getWindow().show();
        InputBudgetController controller = launcher.getController();
        controller.setBudget(selected);
    }

    @FXML
    private void handleFilterButtonAction(ActionEvent event) {
        if (budgetFilterController==null) {
            ViewLauncher launcher = getViewLauncherBuilder()
                    .setFxml("budget_filter.fxml")
                    .setTitle("Filter Budget")
                    .build();
            launcher.load();
            budgetFilterController = launcher.getController();
            budgetFilterController.setOnFilterListener(data -> loadBudgets(data));
        }
        budgetFilterController.getWindow().show();

    }

    private void loadBudgets(BudgetFilterData data) {
        if (null!=filterTask) {
            filterTask.cancel(true);
            filterTask = null;
        }
        this.oldData = data;
        if (null==data) {
            setTableItems(null);
            return;
        }
        Task<List<Budget>> task = TaskUtils.filterBudget(data,t->{
            setTableItems(t.getValue());
        },t->{
            Log.error(TAG,"onChangeFilterData",t.getException());
        });
        filterTask = getApp().getAppExecutor().submit(task);
    }

    private void setTableItems(List<Budget> items) {
        budgetTableView.getItems().clear();
        budgetTableView.getItems().addAll(items);
    }

    @FXML
    private void handleRefreshButtonAction(ActionEvent event) {
        loadBudgets(oldData);
    }
}
