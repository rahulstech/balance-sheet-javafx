package rahulstech.jfx.balancesheet.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;
import rahulstech.jfx.balancesheet.concurrent.DerivativeTasks;
import rahulstech.jfx.balancesheet.database.entity.Derivative;
import rahulstech.jfx.balancesheet.database.model.DerivativeReportModel;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.Log;
import rahulstech.jfx.balancesheet.util.TextUtil;
import rahulstech.jfx.balancesheet.util.ViewLauncher;
import rahulstech.jfx.balancesheet.view.TextAreaPopupTableCell;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class DerivativeTableController extends Controller {

    private static final String TAG = DerivativeTableController.class.getSimpleName();

    @FXML private Label totalInvestedValueLabel;

    @FXML private Label realProfitLabel;

    @FXML private Label totalUnrealizedPLLabel;

    @FXML private Button buttonDeleteSelected;

    @FXML private Button buttonSaveChanged;

    @FXML private Button buttonInfo;

    @FXML private TextField searchBox;

    @FXML private TableView<Derivative> derivativesTable;

    @FXML private TableColumn<Derivative,String> nameColumn;

    @FXML private TableColumn<Derivative, BigDecimal> volumeColumn;

    @FXML private TableColumn<Derivative,Currency> avgBuyPriceColumn;

    @FXML private TableColumn<Derivative, Currency> currentUnitPriceColumn;

    @FXML private TableColumn<Derivative,Currency> totalRealizedPLColumn;

    @FXML private TableColumn<Derivative,Currency> totalUnrealizedPLColumn;

    @FXML private TableColumn<Derivative,String> descriptionColumn;

    @FXML private TableColumn<Derivative,Currency> currentValueColumn;

    @FXML private TableColumn<Derivative,Double> netChangeColumn;

    private Future<?> queryTask;

    private Future<?> filterTask;

    private Future<?> summaryTask;

    private List<Derivative> derivatives;

    private Map<Long,Derivative> editedDerivates = new HashMap<>();

    private DerivativeTransactionTableController derivativeTransactionTableController;

    private CreateDerivativeController createDerivativeController;

    @Override
    protected void onInitialize(ResourceBundle res) {
        // cell value factories
        setCellValueFactories();
        // cell factories
        setCellFactories();
        // cell editing
        setCellEditorHandler();

        derivativesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        searchBox.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filterTask!=null) {
                filterTask.cancel(true);
            }
            if (null==this.derivatives || this.derivatives.isEmpty()) {
                return;
            }
            if (null==newValue || newValue.isEmpty()) {
                setTableItems(this.derivatives);
                return;
            }
            Task<List<Derivative>> task = new Task<>() {
                @Override
                protected List<Derivative> call() throws Exception {
                    String phrase = newValue.toLowerCase();
                    List<Derivative> derivatives = new ArrayList<>(DerivativeTableController.this.derivatives);
                    return derivatives.stream().filter(d -> {
                        String description = d.getDescription();
                        String name = d.getName();
                        boolean mathcedName = name.toLowerCase().contains(phrase);
                        boolean mathedDescription = null != description && !description.isEmpty() && description.toLowerCase().contains(phrase);
                        return mathcedName || mathedDescription;
                    }).collect(Collectors.toList());
                }
            };
            task.setOnSucceeded(e-> setTableItems(task.getValue()));
            filterTask = getApp().getAppExecutor().submit(task);
        });
        // set initial summary to all zero
        setSummary(new DerivativeReportModel());

        loadDerivatives();
    }

    private void setCellValueFactories() {
        nameColumn.setCellValueFactory(column->{
            Derivative derivative = column.getValue();
            String cell_value;
            if (null==derivative){
                cell_value = null;
            }
            else {
                cell_value = derivative.getName();
            }
            return new SimpleStringProperty(cell_value);
        });
        volumeColumn.setCellValueFactory(column->{
            Derivative derivative = column.getValue();
            BigDecimal cell_value;
            if (null==derivative){
                cell_value = null;
            }
            else {
                cell_value = derivative.getVolume();
            }
            return new SimpleObjectProperty<>(cell_value);
        });
        avgBuyPriceColumn.setCellValueFactory(column->{
            Derivative derivative = column.getValue();
            Currency cell_value;
            if (null==derivative){
                cell_value = null;
            }
            else {
                cell_value = derivative.getAvgBuyPrice();
            }
            return new SimpleObjectProperty<>(cell_value);
        });
        currentUnitPriceColumn.setCellValueFactory(column->{
            Derivative derivative = column.getValue();
            Currency cell_value;
            if (null==derivative){
                cell_value = null;
            }
            else {
                cell_value = derivative.getCurrentUnitPrice();
            }
            return new SimpleObjectProperty<>(cell_value);
        });
        totalUnrealizedPLColumn.setCellValueFactory(column->{
            Derivative derivative = column.getValue();
            Currency cell_value;
            if (null==derivative){
                cell_value = null;
            }
            else {
                cell_value = derivative.getUnrealizedPL();
            }
            return new SimpleObjectProperty<>(cell_value);
        });
        descriptionColumn.setCellValueFactory(column->{
            Derivative derivative = column.getValue();
            String cell_value;
            if (null==derivative){
                cell_value = null;
            }
            else {
                cell_value = derivative.getDescription();
            }
            return new SimpleStringProperty(cell_value);
        });
        currentValueColumn.setCellValueFactory(column->{
            Derivative derivative = column.getValue();
            Currency cell_value;
            if (null==derivative){
                cell_value = null;
            }
            else {
                cell_value = derivative.getCurrentValue();
            }
            return new SimpleObjectProperty<>(cell_value);
        });
        netChangeColumn.setCellValueFactory(column->{
            Derivative derivative = column.getValue();
            double cell_value;
            if (null==derivative){
                cell_value = 0;
            }
            else {
                cell_value = derivative.getNetChange();
            }
            return new SimpleObjectProperty<>(cell_value);
        });
        totalRealizedPLColumn.setCellValueFactory(column->{
            Derivative derivative = column.getValue();
            Currency cell_value;
            if (null==derivative){
                cell_value = null;
            }
            else {
                cell_value = derivative.getTotalRealizedPL();
            }
            return new SimpleObjectProperty<>(cell_value);
        });
    }

    private void setCellFactories() {
        nameColumn.setCellFactory(column->new TextFieldTableCell<>(new DefaultStringConverter()){
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setTooltip(null);
                }
                else {
                    Tooltip tooltip = new Tooltip(item);
                    setTooltip(tooltip);
                }
            }
        });
        descriptionColumn.setCellFactory(column->new TextAreaPopupTableCell<>(new DefaultStringConverter(),"Edit Description"){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || null==item || item.isEmpty()) {
                    setText(null);
                    setTooltip(null);
                }
                else {
                    Tooltip tooltip = new Tooltip(item);
                    setTooltip(tooltip);
                }
            }
        });
        currentUnitPriceColumn.setCellFactory(column->new TextFieldTableCell<>(TextUtil.getCurrencyStringConverter()){
            @Override
            public void updateItem(Currency item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                }
                else {
                    setText(TextUtil.prettyPrintCurrency(item));
                }
            }
        });
        totalUnrealizedPLColumn.setCellFactory(col->new TableCell<>(){
            @Override
            protected void updateItem(Currency item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                }
                else {
                    updateTableCellTextStyleForNumberSign(this,item.getValue());
                    setText(TextUtil.prettyPrintCurrency(item));
                }
            }
        });
        netChangeColumn.setCellFactory(col->new TableCell<>(){
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                }
                else {
                    updateTableCellTextStyleForNumberSign(this,item.compareTo(0.0));
                    setText(TextUtil.prettyPrintPercentageChange(item));
                }
            }
        });
        avgBuyPriceColumn.setCellFactory(column->new TextFieldTableCell<>(TextUtil.getCurrencyStringConverter()){
            @Override
            public void updateItem(Currency item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                }
                else {
                    setText(TextUtil.prettyPrintCurrency(item));
                }
            }
        });
        currentValueColumn.setCellFactory(column->new TableCell<>(){
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
        totalRealizedPLColumn.setCellFactory(col->new TextFieldTableCell<>(TextUtil.getCurrencyStringConverter()){
            @Override
            public void updateItem(Currency item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                }
                else {
                    updateTableCellTextStyleForNumberSign(this,item.compareTo(Currency.ZERO));
                    setText(TextUtil.prettyPrintCurrency(item));
                }
            }
        });
        volumeColumn.setCellFactory(column->new TextFieldTableCell<>(TextUtil.getBigDecimalStringConverter(4)));
    }

    private void updateTableCellTextStyleForNumberSign(TableCell<Derivative,?> cell, Number number ) {
        TextUtil.setValueBasedTextStyleClass(number,cell);
    }

    private void setCellEditorHandler() {
        nameColumn.setOnEditCommit(e->handleEditName(e.getRowValue(),e.getNewValue()));
        descriptionColumn.setOnEditCommit(e->handleEditDescription(e.getRowValue(),e.getNewValue()));
        currentUnitPriceColumn.setOnEditCommit(e->handleEditCurrentUnitPrice(e.getRowValue(),e.getNewValue()));
        avgBuyPriceColumn.setOnEditCommit(e->handleEditAvgBuyPrice(e.getRowValue(),e.getNewValue()));
        volumeColumn.setOnEditCommit(e->handleEditVolume(e.getRowValue(),e.getNewValue()));
        totalRealizedPLColumn.setOnEditCommit(e->handleEditRealizedPl(e.getRowValue(),e.getNewValue()));
    }

    private void handleEditRealizedPl(Derivative derivative, Currency newValue) {
        Derivative edited = editedDerivates.getOrDefault(derivative.getId(),derivative);
        edited.setTotalRealizedPL(newValue);
        Log.debug(TAG,"handleEditRealizedPl: derivative: id="+derivative.getId()+" newValue=\""+newValue+"\"");
        editedDerivates.put(derivative.getId(),edited);
    }

    private void handleEditVolume(Derivative derivative, BigDecimal newValue) {
        Derivative edited = editedDerivates.getOrDefault(derivative.getId(),derivative);
        edited.setVolume(newValue);
        Log.debug(TAG,"handleEditVolume: derivative: id="+derivative.getId()+" newValue=\""+newValue+"\"");
        editedDerivates.put(derivative.getId(),edited);
    }

    private void handleEditName(Derivative derivative, String newValue) {
        Derivative edited = editedDerivates.getOrDefault(derivative.getId(),derivative);
        edited.setName(newValue);
        Log.debug(TAG,"handleEditName: derivative: id="+derivative.getId()+" newValue=\""+newValue+"\"");
        editedDerivates.put(derivative.getId(),derivative);
    }

    private void handleEditDescription(Derivative derivative, String newValue) {
        Derivative edited = editedDerivates.getOrDefault(derivative.getId(),derivative);
        edited.setDescription(newValue);
        Log.debug(TAG,"handleEditDescription: derivative: id="+derivative.getId()+" newValue=\""+newValue+"\"");
        editedDerivates.put(derivative.getId(),edited);
    }

    private void handleEditCurrentUnitPrice(Derivative derivative, Currency newValue) {
        Derivative edited = editedDerivates.getOrDefault(derivative.getId(),derivative);
        edited.setCurrentUnitPrice(newValue);
        Log.debug(TAG,"handleEditCurrentUnitPrice: derivative: id="+derivative.getId()+" newValue=\""+newValue+"\"");
        editedDerivates.put(derivative.getId(),edited);
    }

    private void handleEditAvgBuyPrice(Derivative derivative, Currency newValue) {
        Derivative edited = editedDerivates.getOrDefault(derivative.getId(),derivative);
        edited.setAvgBuyPrice(newValue);
        Log.debug(TAG,"handleEditAvgBuyPrice: derivative: id="+derivative.getId()+" newValue=\""+newValue+"\"");
        editedDerivates.put(derivative.getId(),edited);
    }

    private void handleDoubleClickRow(Derivative derivative) {
        if (null==derivativeTransactionTableController) {
            ViewLauncher launcher = getViewLauncherBuilder()
                    .setTitle("Derivative Transaction")
                    .setFxml("derivative_transaction_table.fxml")
                    .build()
                    .load();
            derivativeTransactionTableController = launcher.getController();
            derivativeTransactionTableController.getWindow().setMaximized(true);
        }
        derivativeTransactionTableController.getWindow().show();
        derivativeTransactionTableController.setDerivative(derivative);
    }

    @FXML private void handleDeleteDerivativesButtonClicked() {
        List<Derivative> derivatives = derivativesTable.getSelectionModel().getSelectedItems();
        if (derivatives == null || derivatives.isEmpty()) {
            return;
        }
        DialogUtil.alertConfirmation(getWindow(),"Warning","Selected derivates will be deleted permanently. Are you sure to proceed?",
                "Yes Delete",()->{
                    cancelRunningTasks();

                    Task<Void> task = DerivativeTasks.deleteDerivatives(derivatives);
                    task.setOnSucceeded(e-> derivativesTable.getItems().removeAll(derivatives));
                    task.setOnFailed(e-> Log.error(TAG,"handleDeleteDerivativesButtonClicked",task.getException()));
                    getApp().getAppExecutor().submit(task);
                },
                "No Cancel",null);
    }

    @FXML private void handleSaveChangesButtonClicked() {
        if (editedDerivates.isEmpty()) {
            Log.debug(TAG,"handleSaveChangesButtonClicked: nothing edited");
            return;
        }
        Stage dialog = DialogUtil.showIndeterminateProgressDialog(getWindow(),"Save Progress","Saving changes. Please wait");
        dialog.setOnCloseRequest(Event::consume);
        dialog.show();

        Collection<Derivative> edited = editedDerivates.values();
        Task<Void> task = DerivativeTasks.updateDerivatives(edited);
        task.setOnSucceeded(e->{
            dialog.close();
            // clear it so that it get saved again next time event if not changed
            editedDerivates.clear();
        });
        task.setOnFailed(e->{
            dialog.close();
            DialogUtil.alertError(getWindow(),"Error Save","Fail to save changes. Please try again.");
            Log.error(TAG,"handleSaveChangesButtonClicked",task.getException());
        });
        getApp().getAppExecutor().submit(task);
    }

    @FXML private void handleBuyDerivativeButtonClicked() {
        if (createDerivativeController!=null && createDerivativeController.getWindow().isShowing()) {
            return;
        }
        ViewLauncher launcher = getViewLauncherBuilder()
                .setFxml("create_derivative.fxml")
                .setTitle("Buy New Derivative")
                .build().load();
        createDerivativeController = launcher.getController();
        createDerivativeController.getWindow().show();
    }

    @FXML private void handleRefreshButtonClicked() {
        loadDerivatives();
    }

    @FXML private void handleInfoButtonClicked() {
        if (derivativesTable.getSelectionModel().getSelectedItems().size()!=1) {
            Log.debug(TAG,"more than one item seleted, can not handle show info");
            return;
        }
        Derivative derivative = derivativesTable.getSelectionModel().getSelectedItem();
        handleDoubleClickRow(derivative);
    }

    private void loadDerivatives() {
        cancelRunningTasks();
        Task<List<Derivative>> task = DerivativeTasks.getAllDerivatives();
        task.setOnSucceeded(e->onLoadFinished(task.getValue()));
        task.setOnFailed(e->Log.error(TAG,"loadDerivatives",task.getException()));
        queryTask = getApp().getAppExecutor().submit(task);
    }

    private void onLoadFinished(List<Derivative> derivatives) {
        this.derivatives = derivatives;
        setTableItems(derivatives);
        // prepare summary
        prepareSummary(derivatives);
    }

    private void prepareSummary(final List<Derivative> derivatives) {
        if (null!=summaryTask) {
            summaryTask.cancel(true);
        }
        Task<DerivativeReportModel> task = DerivativeTasks.getOverallDervitiveReport();
        task.setOnSucceeded(e->setSummary(task.getValue()));
        task.setOnFailed(e->Log.error(TAG,"prepareSummary",task.getException()));
        summaryTask = getApp().getAppExecutor().submit(task);
    }

    private void setSummary(DerivativeReportModel summary) {
        totalInvestedValueLabel.setText(TextUtil.prettyPrintCurrency(summary.getTotalInvestedValue()));
        realProfitLabel.setText(TextUtil.prettyPrintCurrency(summary.getProfitAfterTax()));
        String text_unrealized = TextUtil.prettyPrintCurrency(summary.getTotalUnrealizedPL());
        String text_change = TextUtil.prettyPrintPercentageChange(summary.getPercentageChange());
        totalUnrealizedPLLabel.setText(text_unrealized+" ("+text_change+")");
        TextUtil.setValueBasedTextStyleClass(summary.getPercentageChange(),totalUnrealizedPLLabel);
        TextUtil.setValueBasedTextStyleClass(summary.getProfitAfterTax().getValue(),realProfitLabel);
    }

    private void setTableItems(List<Derivative> derivatives) {
        derivativesTable.getItems().clear();
        derivativesTable.getItems().setAll(derivatives);
    }

    private void cancelRunningTasks() {
        if (null!=queryTask) {
            queryTask.cancel(true);
        }
        if (null!=filterTask) {
            filterTask.cancel(true);
        }
        if (null!=summaryTask) {
            summaryTask.cancel(true);
        }
    }
}
