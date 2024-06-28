package rahulstech.jfx.balancesheet.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import rahulstech.jfx.balancesheet.concurrent.TransactionHistoryDeleteTask;
import rahulstech.jfx.balancesheet.concurrent.TransactionHistoryFilterTask;
import rahulstech.jfx.balancesheet.database.dao.HistoryFilterData;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.database.entity.TransactionHistory;
import rahulstech.jfx.balancesheet.database.type.TransactionType;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.Log;
import rahulstech.jfx.balancesheet.util.ViewLauncher;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class TransactionHistoryController extends Controller {

    private static final String TAG = TransactionHistoryController.class.getSimpleName();

    private static final DateTimeFormatter WHEN_FORMATER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    @FXML
    private TableColumn<TransactionHistory,String> categoriesColumn;

    @FXML
    private TextField searchBox;

    @FXML
    private TableView<TransactionHistory> transactionTable;

    @FXML
    private TableColumn<TransactionHistory, String> dateColumn;

    @FXML
    private TableColumn<TransactionHistory, Text> amountColumn;

    @FXML
    private TableColumn<TransactionHistory,String> taxColumn;

    @FXML
    private TableColumn<TransactionHistory, String> srcColumn;

    @FXML
    private TableColumn<TransactionHistory, String> destColumn;

    @FXML
    private TableColumn<TransactionHistory, String> typeColumn;

    @FXML
    private TableColumn<TransactionHistory, String> descriptionColumn;

    private List<TransactionHistory> histories;

    private Future<?> filterTask;

    private HistoryFilterData currentData;

    @Override
    protected void onInitialize(ResourceBundle res) {
        transactionTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getWhen().format(WHEN_FORMATER)));

        amountColumn.setCellValueFactory(cellData -> {
            TransactionHistory transaction = cellData.getValue();
            Text text = new Text(transaction.getAmount().toString());
            if (transaction.getType() == TransactionType.WITHDRAW) {
                text.setFill(Color.RED);
            } else if (transaction.getType() == TransactionType.DEPOSIT) {
                text.setFill(Color.GREEN);
            } else {
                text.setFill(Color.web("#00B4D8"));
            }
            return new SimpleObjectProperty<>(text);
        });

        taxColumn.setCellValueFactory(cellData -> {
            TransactionHistory transaction = cellData.getValue();
            String tax = null==transaction.getTax() ? null : transaction.getTax().toString();
            return new SimpleStringProperty(tax);
        });

        srcColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getSrc() != null ? cellData.getValue().getSrc().getName() : ""));

        destColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDest() != null ? cellData.getValue().getDest().getName() : ""));

        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType().toString()));
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        descriptionColumn.setCellFactory(cellData->new TableCell<TransactionHistory,String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || null==item || item.isEmpty()) {
                    setText(null);
                    setTooltip(null);
                }
                else {
                    setText(item);
                    Tooltip tooltip = new Tooltip(item);
                    setTooltip(tooltip);
                }
            }
        });

        // Inside the onInitialize method, after setting up other columns
        categoriesColumn.setCellValueFactory(cellData -> {
            List<Category> categories = cellData.getValue().getCategories();
            String categoryNames;
            if (null==categories || categories.isEmpty()) {
                categoryNames = null;
            }
            else {
                categoryNames = categories.stream()
                        .map(Category::getName)
                        .collect(Collectors.joining(", "));
            }
            return new SimpleStringProperty(categoryNames);
        });

        categoriesColumn.setCellFactory(column -> new TableCell<TransactionHistory,String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    Tooltip tooltip = new Tooltip(item);
                    setTooltip(tooltip);
                }
            }
        });
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> {
            if (null != filterTask) {
                filterTask.cancel(true);
            }
            TransactionHistoryFilterTask task = new TransactionHistoryFilterTask(histories,newValue);
            task.setOnSucceeded(event -> {
                setTransactionHistories(task.getValue());
            });
            task.setOnFailed(e-> Log.error(TAG,"filter-task",task.getException()));
            filterTask = getApp().getAppExecutor().submit(task);
        });
    }

    @FXML
    private void handleDeleteHistory() {
        List<TransactionHistory> histories = transactionTable.getSelectionModel().getSelectedItems();
        if (histories == null || histories.isEmpty()) {
            return;
        }
        DialogUtil.alertConfirmation(getWindow(),"Warning","Selected histories will be deleted permanently. Are you sure to proceed?",
                "Yes Delete",()->{
                    TransactionHistoryDeleteTask task = new TransactionHistoryDeleteTask(histories);
                    task.setOnSucceeded(e->{
                        transactionTable.getItems().removeAll(histories);
                    });
                    task.setOnFailed(e-> task.getException().printStackTrace());
                    getApp().getAppExecutor().submit(task);
                },
                "No Cancel",null);
    }

    @FXML
    private void handleEditHistory() {
        if (transactionTable.getSelectionModel().getSelectedItems().size() > 1) {
            return;
        }
        TransactionHistory item = transactionTable.getSelectionModel().getSelectedItem();
        if (null == item) {
            return;
        }
        ViewLauncher launcher = getViewLauncherBuilder()
                .setTitle("Edit History")
                .setFxml("input_transaction_history.fxml")
                .setStageModality(Modality.APPLICATION_MODAL)
                .build().load();
        InputTransactionHistoryController controller = launcher.getController();
        controller.getWindow().show();
        controller.setOldHistory(item);
    }

    @FXML
    private void handleSelectAll(ActionEvent event) {
        int itemCount = transactionTable.getItems().size();
        int selectionCount = transactionTable.getSelectionModel().getSelectedItems().size();
        if (itemCount != selectionCount) {
            transactionTable.getSelectionModel().selectAll();
        }
        else {
            transactionTable.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadHistories(currentData);
    }

    @FXML
    private void handleFilter(ActionEvent event) {
        ViewLauncher loader = new ViewLauncher.Builder()
                .setTitle("Filter")
                .setStageStyle(StageStyle.UTILITY)
                .setOwnerWindow(getWindow())
                .setFxml("history_filter_tabs.fxml")
                .build();
        loader.load();
        loader.getWindow().show();
        HistoryFilterController controller = loader.getController();
        controller.setOnFilterListener((newV)-> loadHistories(newV));
    }

    @FXML
    private void handleAddHistory(ActionEvent event) {
        ViewLauncher launcher = getViewLauncherBuilder().setFxml("input_transaction_history.fxml")
                .setTitle("Add New History")
                .setStageModality(Modality.APPLICATION_MODAL)
                .build().load();
        launcher.getWindow().show();
    }

    private void setTransactionHistories(List<TransactionHistory> value) {
        ObservableList<TransactionHistory> histories = FXCollections.observableList(value);
        transactionTable.setItems(histories);
    }

    private void loadHistories(final HistoryFilterData data) {
        this.currentData = data;
        if (null == data) {
            this.histories = Collections.emptyList();
            setTransactionHistories(null);
            return;
        }
        TransactionHistoryFilterTask task = new TransactionHistoryFilterTask(data);
        task.setOnSucceeded(e->{
            this.histories = task.getValue();
            setTransactionHistories(task.getValue());
        });
        task.setOnFailed(e-> Log.error(TAG,"loadHistories",task.getException()));
        getApp().getAppExecutor().submit(task);
    }
}
