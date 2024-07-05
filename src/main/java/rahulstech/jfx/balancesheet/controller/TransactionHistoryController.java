package rahulstech.jfx.balancesheet.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import rahulstech.jfx.balancesheet.concurrent.TransactionHistoryDeleteTask;
import rahulstech.jfx.balancesheet.concurrent.TransactionHistoryFilterTask;
import rahulstech.jfx.balancesheet.database.dao.HistoryFilterData;
import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.database.entity.TransactionHistory;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.TransactionType;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.Log;
import rahulstech.jfx.balancesheet.util.TextUtil;
import rahulstech.jfx.balancesheet.util.ViewLauncher;

import java.time.LocalDate;
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
    private TableColumn<TransactionHistory, LocalDate> dateColumn;

    @FXML
    private TableColumn<TransactionHistory, Currency> amountColumn;

    @FXML
    private TableColumn<TransactionHistory,Currency> taxColumn;

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

        dateColumn.setCellValueFactory(cellData -> {
            TransactionHistory history = cellData.getValue();
            LocalDate cell_value = null==history ? null : history.getWhen();
            return new SimpleObjectProperty<>(cell_value);
        });
        dateColumn.setCellFactory(param -> new TableCell<>(){
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                }
                else {
                    setText(item.format(WHEN_FORMATER));
                }
            }
        });

        amountColumn.setCellValueFactory(cellData -> {
            TransactionHistory data = cellData.getValue();
            Currency cell_value;
            if (null==data) {
                cell_value = null;
            }
            else {
                cell_value = data.getAmount();
            }
            return new SimpleObjectProperty<>(cell_value);
        });
        amountColumn.setCellFactory(column->new TableCell<>(){
            @Override
            protected void updateItem(Currency item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                }
                else {
                    TransactionHistory history = getTableRow().getItem();
                    getStyleClass().removeAll("text-debit","text-credit","text-transfer");
                    if (history.getType() == TransactionType.WITHDRAW) {
                        getStyleClass().add("text-debit");

                    } else if (history.getType() == TransactionType.DEPOSIT) {
                        getStyleClass().add("text-credit");
                    } else {
                        getStyleClass().add("text-transfer");
                    }
                    setText(TextUtil.prettyPrintCurrency(item));
                }
            }
        });

        taxColumn.setCellValueFactory(cellData -> {
            TransactionHistory transaction = cellData.getValue();
            Currency tax = null==transaction.getTax() ? null : transaction.getTax();
            return new SimpleObjectProperty<>(tax);
        });
        taxColumn.setCellFactory(param->new TableCell<>(){
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

        srcColumn.setCellValueFactory(cellData ->{
            TransactionHistory history = cellData.getValue();
            String cell_value = null;
            if (null!=history) {
                TransactionType type = history.getType();
                // only for WITHDRAW and TRANSFER set srcAccount name
                if (type!=TransactionType.DEPOSIT) {
                    cell_value = null!=history.getSrc() ? history.getSrc().getName() : null;
                }
            }
            return new SimpleStringProperty(cell_value);
        });

        destColumn.setCellValueFactory(cellData -> {
            TransactionHistory history = cellData.getValue();
            String cell_value = null;
            if (null!=history) {
                TransactionType type = history.getType();
                Account account;
                // for DEPOSITE set srcAccount name for TRANSFER set destAccount name
                if (type==TransactionType.DEPOSIT) {
                    account = history.getSrc();
                }
                else if (type==TransactionType.TRANSFER) {
                    account = history.getDest();
                }
                else {
                    account = null;
                }
                cell_value = null!=account ? account.getName() : null;
            }
            return new SimpleStringProperty(cell_value);
        });

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
                    task.setOnFailed(e-> Log.error(TAG,"handleDeleteHistory",task.getException()));
                    getApp().getAppExecutor().submit(task);
                },
                "No Cancel",null);
    }

    private void handleTableRowDoubleClicked(TransactionHistory history) {
        ViewLauncher launcher = getViewLauncherBuilder()
                .setTitle("Edit History")
                .setFxml("input_transaction_history.fxml")
                .setStageStyle(StageStyle.UTILITY)
                .build().load();
        InputTransactionHistoryController controller = launcher.getController();
        controller.getWindow().show();
        controller.setOldHistory(history);
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadHistories(currentData);
    }

    @FXML
    private void handleFilter(ActionEvent event) {
        ViewLauncher loader = getViewLauncherBuilder()
                .setTitle("Filter")
                .setStageStyle(StageStyle.UTILITY)
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

    @FXML
    private void handleEditHistory(ActionEvent event) {
        if (transactionTable.getSelectionModel().getSelectedItems().size()!=1) {
            Log.debug(TAG,"can handle edit history when exactly one item is selected");
            return;
        }
        TransactionHistory history = transactionTable.getSelectionModel().getSelectedItem();
        handleTableRowDoubleClicked(history);
    }

    private void setTransactionHistories(List<TransactionHistory> value) {
        transactionTable.getItems().clear();
        transactionTable.getItems().addAll(value);
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
