package rahulstech.jfx.balancesheet.controller;

import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import rahulstech.jfx.balancesheet.concurrent.DerivativeTasks;
import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.database.entity.Derivative;
import rahulstech.jfx.balancesheet.database.entity.DerivativeTransaction;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.DerivativeTType;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.Log;
import rahulstech.jfx.balancesheet.util.TextUtil;
import rahulstech.jfx.balancesheet.util.ViewLauncher;
import rahulstech.jfx.balancesheet.view.Chip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Future;

public class CreateDerivativeController extends Controller{

    private static final String TAG = CreateDerivativeController.class.getSimpleName();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d/M/yyyy", Locale.ENGLISH);

    @FXML private TextField nameField;

    @FXML private FlowPane accountFlowPane;

    @FXML private TextArea descriptionField;

    @FXML private DatePicker dateDatePicker;

    @FXML private TextField volumnField;

    @FXML private TextField unitPriceField;

    @FXML private TextField taxField;

    private AccountsListController accountsListController;

    private Future<?> createTask;

    @Override
    protected void onInitialize(ResourceBundle res) {

        dateDatePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return null == date ? LocalDate.now().format(FORMATTER) : date.format(FORMATTER);
            }

            @Override
            public LocalDate fromString(String string) {
                return null == string || string.isEmpty() ? LocalDate.now() : LocalDate.parse(string, FORMATTER);
            }
        });

        dateDatePicker.setValue(LocalDate.now());

        setUpTextFormater();
    }

    private void setUpTextFormater() {
        volumnField.setTextFormatter(TextUtil.createBigDecimalTextFormater(4));
        unitPriceField.setTextFormatter(TextUtil.createCurrencyTextFormater());
        taxField.setTextFormatter(TextUtil.createCurrencyTextFormater());

    }

    @FXML
    private void saveButtonClicked() {
        String name = nameField.getText();
        String description = descriptionField.getText();
        Account dematAccount = getDematAccount();
        BigDecimal volume = (BigDecimal) volumnField.getTextFormatter().getValue();
        Currency unitPrice = (Currency) unitPriceField.getTextFormatter().getValue();
        Currency taxPrice = (Currency) taxField.getTextFormatter().getValue();
        LocalDate when = dateDatePicker.getValue();

        StringBuilder message = new StringBuilder();
        if (null==name || name.isEmpty()) {
            message.append("no name provided").append("\r\n") ;
        }
        if (null==dematAccount) {
            message.append("no account selected").append("\r\n");
        }
        if (volume.compareTo(BigDecimal.ZERO)<=0) {
            message.append("volume must be a number more than zero").append("\r\n");
        }
        if (unitPrice.compareTo(Currency.ZERO)<=0) {
            message.append("unit price must be a number more than zero").append("\r\n");
        }
        if (taxPrice.compareTo(Currency.ZERO)<0) {
            message.append("tax must be a non negative number").append("\r\n");
        }
        if (null==when) {
            message.append("date not selected");
        }
        if (message.length()>0) {
            DialogUtil.alertError(getWindow(),"Input Error",message.toString());
            return;
        }

        Derivative derivative = new Derivative();
        derivative.setName(name);
        derivative.setDescription(description);
        derivative.setVolume(volume);
        derivative.setAvgBuyPrice(unitPrice);
        derivative.setCurrentUnitPrice(unitPrice);
        derivative.setDematAccount(dematAccount);

        DerivativeTransaction transaction = new DerivativeTransaction();
        transaction.setDerivative(derivative);
        transaction.setPrice(unitPrice);
        transaction.setVolume(volume);
        transaction.setType(DerivativeTType.BUY);
        transaction.setTax(taxPrice);
        transaction.setWhen(when);
        transaction.setDescription(description);

        saveDerivativeWithTransaction(derivative,transaction);
    }

    private void saveDerivativeWithTransaction(Derivative derivative, DerivativeTransaction transaction) {
        if (createTask!=null) {
            createTask.cancel(true);
        }
        Stage dialog = DialogUtil.showIndeterminateProgressDialog(getWindow(),"Save Progress","Saving derivative. Please wait");
        dialog.setOnCloseRequest(Event::consume);
        dialog.show();

        Task<Derivative> task = DerivativeTasks.createDerivative(derivative,transaction);
        task.setOnSucceeded(e->{
            dialog.close();
            getWindow().close();
        });
        task.setOnFailed(e->{
            dialog.close();
            DialogUtil.alertError(getWindow(),"Error Save","Fail to save derivative");
            Log.error(TAG,"saveDerivativeWithTransaction",task.getException());
        });
        createTask = getApp().getAppExecutor().submit(task);
    }

    @FXML
    private void addAccountButtonClicked() {
        if (null==accountsListController) {
            ViewLauncher launcher = getViewLauncherBuilder()
                    .setTitle("Choose Demat Account")
                    .setFxml("accounts_list.fxml")
                    .setStageStyle(StageStyle.UTILITY)
                    .build()
                    .load();
            accountsListController = launcher.getController();
            accountsListController.getAccountListView().getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            accountsListController.getWindow().setOnCloseRequest(e->
                    setDematAccount(accountsListController.getAccountListView().getSelectionModel().getSelectedItem())
            );
        }
        accountsListController.getWindow().show();
    }

    private void setDematAccount(Account account) {
        accountFlowPane.getChildren().clear();
        if (null!=account) {
            Chip chip = new Chip(account.getName());
            chip.setUserData(account);
            accountFlowPane.getChildren().add(chip);
        }
    }

    private Account getDematAccount() {
        Node child = accountFlowPane.getChildren().get(0);
        if (null!=child) {
            return (Account) child.getUserData();
        }
        return null;
    }
}
