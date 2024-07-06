package rahulstech.jfx.balancesheet.controller;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import rahulstech.jfx.balancesheet.concurrent.DerivativeTasks;
import rahulstech.jfx.balancesheet.database.entity.Derivative;
import rahulstech.jfx.balancesheet.database.entity.DerivativeTransaction;
import rahulstech.jfx.balancesheet.database.type.DerivativeTType;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.Log;
import rahulstech.jfx.balancesheet.util.TextUtil;
import rahulstech.jfx.balancesheet.util.ViewLauncher;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class DerivativeTransactionTableController extends Controller {

    private static final String TAG = DerivativeTableController.class.getSimpleName();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    @FXML
    private Label investedValue;

    @FXML
    private Label realizedPL;

    @FXML
    private TextField searchBox;

    @FXML
    private TableView<DerivativeTransaction> transactionTable;

    @FXML
    private TableColumn<DerivativeTransaction,String> whenColumn;

    @FXML
    private TableColumn<DerivativeTransaction,String> typeColumn;

    @FXML
    private TableColumn<DerivativeTransaction,Number> volumeColumn;

    @FXML
    private TableColumn<DerivativeTransaction,Number> priceColumn;

    @FXML
    private TableColumn<DerivativeTransaction,Number> taxColumn;

    @FXML
    private TableColumn<DerivativeTransaction,String> descriptionColumn;

    private ObjectProperty<Derivative> derivativeProperty = new SimpleObjectProperty<>();

    private Future<?> queryTask;

    private Future<?> filterTask;

    private List<DerivativeTransaction> transactions;

    public Derivative derivativeProperty() {
        return derivativeProperty.get();
    }

    public Derivative getDerivative() {
        return derivativeProperty.getValue();
    }

    public void setDerivative(Derivative derivative) {
        derivativeProperty.setValue(derivative);
    }

    @Override
    protected void onInitialize(ResourceBundle res) {
        setUpCellValueFactories();
        setUpCellFactories();
        transactionTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filterTask!=null) {
                filterTask.cancel(true);
            }
            if (null==transactions || transactions.isEmpty()) {
                return;
            }
            if (null==newValue || newValue.isEmpty()) {
                setTableItems(this.transactions);
                return;
            }
            Task<List<DerivativeTransaction>> task = new Task<>() {
                @Override
                protected List<DerivativeTransaction> call() throws Exception {
                    String phrase = newValue.toLowerCase();
                    List<DerivativeTransaction> transactions = new ArrayList<>(DerivativeTransactionTableController.this.transactions);
                    return transactions.stream().filter(d -> {
                        String description = d.getDescription();
                        return null != description && !description.isEmpty() && description.toLowerCase().contains(phrase);
                    }).collect(Collectors.toList());
                }
            };
            task.setOnSucceeded(e-> setTableItems(task.getValue()));
            filterTask = getApp().getAppExecutor().submit(task);
        });
        derivativeProperty.addListener((observable, oldValue, newValue) -> {
            Log.debug(TAG,"dervative changed: old="+oldValue+" new="+newValue);
            updateDerivativeDetails(newValue);
            loadDerivativeTransactions(newValue);
        });
    }

    private void setUpCellValueFactories() {
        whenColumn.setCellValueFactory(column->{
            DerivativeTransaction value = column.getValue();
            String cell_value;
            if (null==value) {
                cell_value = null;
            }
            else {
                cell_value = value.getWhen().format(FORMATTER);
            }
            return new SimpleStringProperty(cell_value);
        });
        typeColumn.setCellValueFactory(column->{
            DerivativeTransaction value = column.getValue();
            String cell_value;
            if (null==value) {
                cell_value = null;
            }
            else {
                cell_value = value.getType().name();
            }
            return new SimpleStringProperty(cell_value);
        });
        volumeColumn.setCellValueFactory(column->{
            DerivativeTransaction value = column.getValue();
            Number cell_value;
            if (null==value) {
                cell_value = null;
            }
            else {
                cell_value = value.getVolume();
            }
            return new SimpleObjectProperty<>(cell_value);
        });
        priceColumn.setCellValueFactory(column->{
            DerivativeTransaction value = column.getValue();
            Number cell_value;
            if (null==value) {
                cell_value = null;
            }
            else {
                cell_value = value.getPrice().getValue();
            }
            return new SimpleObjectProperty<>(cell_value);
        });
        taxColumn.setCellValueFactory(column->{
            DerivativeTransaction value = column.getValue();
            Number cell_value;
            if (null==value) {
                cell_value = null;
            }
            else {
                cell_value = value.getTax().getValue();
            }
            return new SimpleObjectProperty<>(cell_value);
        });
        descriptionColumn.setCellValueFactory(column->{
            DerivativeTransaction value = column.getValue();
            String cell_value;
            if (null==value) {
                cell_value = null;
            }
            else {
                cell_value = value.getDescription();
            }
            return new SimpleStringProperty(cell_value);
        });
    }

    private void setUpCellFactories() {
        descriptionColumn.setCellFactory(col->new TableCell<>(){
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
    }

    private void updateDerivativeDetails(Derivative derivative) {
        Log.debug(TAG,"updateDerivativeDetails: derivative="+derivative);
        getWindow().setTitle(derivative.getName());
        // set the current invested value and total realized profit / loss
        investedValue.setText(TextUtil.prettyPrintCurrency(derivative.getCurrentInvestedValue()));
        realizedPL.setText(TextUtil.prettyPrintCurrency(derivative.getTotalRealizedPL().getValue()));
        TextUtil.setValueBasedTextStyleClass(derivative.getTotalRealizedPL().getValue(),realizedPL);
    }

    @FXML
    private void handleDeleteButtonClicked() {
        List<DerivativeTransaction> transactions = transactionTable.getSelectionModel().getSelectedItems();
        if (transactions == null || transactions.isEmpty()) {
            return;
        }
        DialogUtil.alertConfirmation(getWindow(),"Warning","Selected transactions will be deleted permanently. Are you sure to proceed?",
                "Yes Delete",()->{
                    if (null!=queryTask) {
                        queryTask.cancel(true);
                    }
                    if (filterTask!=null) {
                        filterTask.cancel(true);
                    }

                    Task<Void> task = DerivativeTasks.deleteDerivativeTransactions(transactions);
                    task.setOnSucceeded(e-> transactionTable.getItems().removeAll(transactions));
                    task.setOnFailed(e-> Log.error(TAG,"handleDeleteDerivativesButtonClicked",task.getException()));
                    getApp().getAppExecutor().submit(task);
                },
                "No Cancel",null);
    }

    @FXML
    private void handleRefreshButtonClicked() {
        //loadDerivativeTransactions(getDerivative());
        reloadDerivative();
    }

    @FXML
    private void handleBuyButtonClicked() {
        openInputDerivativeTransactionForNewTransaction("Buy Unit(s)",DerivativeTType.BUY);
    }

    @FXML
    private void handleSellButtonClicked() {
        openInputDerivativeTransactionForNewTransaction("Sell Unit(s)",DerivativeTType.SELL);
    }

    @FXML
    private void handleRewardButtonClicked() {
        openInputDerivativeTransactionForNewTransaction("Reward Unit(s)",DerivativeTType.REWARD);
    }

    @FXML
    private void handleEditButtonClicked() {
        if (transactionTable.getSelectionModel().getSelectedItems().size()!=1) {
            Log.debug(TAG,"exactly one item not selected, can not handle edit");
            return;
        }
        DerivativeTransaction transaction = transactionTable.getSelectionModel().getSelectedItem();
        handleTableRowDoubleClicked(transaction);
    }

    private void openInputDerivativeTransactionForNewTransaction(String title, DerivativeTType type) {
        ViewLauncher launcher = getViewLauncherBuilder()
                .setTitle(title)
                .setFxml("input_derivative_transaction.fxml")
                .build().load();
        InputDerivativeTransactionController controller = launcher.getController();
        controller.getWindow().show();
        controller.setDerivative(getDerivative());
        controller.setDerivativeTType(type);
    }

    private void handleTableRowDoubleClicked(DerivativeTransaction transaction) {
        ViewLauncher launcher = getViewLauncherBuilder()
                .setTitle("Edit Derivative Transaction")
                .setFxml("input_derivative_transaction.fxml")
                .build().load();
        InputDerivativeTransactionController controller = launcher.getController();
        controller.getWindow().show();
        controller.setDerivativeTransaction(transaction);
    }

    private void loadDerivativeTransactions(Derivative derivative) {
        Log.debug(TAG,"loadDerivative: derivative="+derivative);
        if (null!=queryTask) {
            queryTask.cancel(true);
        }
        if (null==derivative) {
            setTableItems(null);
            return;
        }
        Task<List<DerivativeTransaction>> task = DerivativeTasks.getAllDerivativesTransactionFor(derivative);
        task.setOnSucceeded(e->{
            this.transactions = task.getValue();
            setTableItems(task.getValue());
        });
        task.setOnFailed(e->Log.error(TAG,"loadDerivativeTransactions",task.getException()));
        queryTask = getApp().getAppExecutor().submit(task);
    }

    private void setTableItems(List<DerivativeTransaction> items) {
        transactionTable.getItems().clear();
        transactionTable.getItems().addAll(items);
    }

    private void reloadDerivative() {
        Task<Derivative> task = DerivativeTasks.reloadDerivative(getDerivative());
        task.setOnSucceeded(e->setDerivative(task.getValue()));
        task.setOnFailed(e->Log.error(TAG,"reloadDerivative",task.getException()));
        getApp().getAppExecutor().submit(task);
    }
}
