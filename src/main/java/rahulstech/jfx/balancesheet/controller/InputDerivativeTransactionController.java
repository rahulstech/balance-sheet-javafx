package rahulstech.jfx.balancesheet.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import rahulstech.jfx.balancesheet.concurrent.DerivativeTasks;
import rahulstech.jfx.balancesheet.database.entity.Derivative;
import rahulstech.jfx.balancesheet.database.entity.DerivativeTransaction;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.DerivativeTType;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.Log;
import rahulstech.jfx.balancesheet.util.TextUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class InputDerivativeTransactionController extends Controller {

    private static final String TAG = InputDerivativeTransactionController.class.getSimpleName();

    @FXML private DatePicker whenDatePicker;

    @FXML private TextField volumeField;

    @FXML private TextField priceField;

    @FXML private TextField taxField;

    @FXML private TextArea descriptionArea;

    private DerivativeTransaction oldTransaction;

    private DerivativeTType type;

    private Derivative derivative;

    @SuppressWarnings("unchecked")
    public void setDerivativeTransaction(DerivativeTransaction transaction) {
        oldTransaction = transaction;
        setDerivativeTType(transaction.getType());
        setDerivative(transaction.getDerivative());
        whenDatePicker.setValue(transaction.getWhen());
        whenDatePicker.setConverter(TextUtil.getLocalDateStringConverter(TextUtil.DATE_PICK_FORMAT));
        ((TextFormatter<Currency>) priceField.getTextFormatter()).setValue(transaction.getPrice());
        ((TextFormatter<BigDecimal>) volumeField.getTextFormatter()).setValue(transaction.getVolume());
        ((TextFormatter<Currency>) taxField.getTextFormatter()).setValue(transaction.getTax());
        descriptionArea.setText(transaction.getDescription());
    }

    public void setDerivativeTType(DerivativeTType type) {
        this.type = type;
    }

    public void setDerivative(Derivative derivative) {
        this.derivative = derivative;
    }

    @Override
    protected void onInitialize(ResourceBundle res) {
        whenDatePicker.setConverter(TextUtil.getLocalDateStringConverter(TextUtil.DATE_PICK_FORMAT));
        whenDatePicker.setValue(LocalDate.now());
        priceField.setTextFormatter(TextUtil.createCurrencyTextFormater());
        volumeField.setTextFormatter(TextUtil.createBigDecimalTextFormater(4));
        taxField.setTextFormatter(TextUtil.createCurrencyTextFormater());
    }

    @FXML
    private void handleSaveButtonClicked() {
        LocalDate when = whenDatePicker.getValue();
        BigDecimal volume = (BigDecimal) volumeField.getTextFormatter().getValue();
        Currency price = (Currency) priceField.getTextFormatter().getValue();
        Currency tax = (Currency) taxField.getTextFormatter().getValue();
        String description = descriptionArea.getText();

        StringBuilder message = new StringBuilder();
        if (null==when) {
            message.append("no date selected").append("\r\n");
        }
        if (null==volume) {
            message.append("volume not provided").append("\r\n");
        }
        else if (volume.compareTo(BigDecimal.ZERO)<0) {
            message.append("volume must be a positive number").append("\r\n");
        }
        if (null==price) {
            message.append("price per unit not provided").append("\r\n");
        }
        else if (price.compareTo(Currency.ZERO)<0) {
            message.append("price must be a positive number").append("\r\n");
        }
        if (null!=tax && tax.compareTo(Currency.ZERO)<0) {
            message.append("tax must be either 0 or a positive number").append("\r\n");
        }
        if (message.length()>0) {
            DialogUtil.alertError(getWindow(),"Input Error",message.toString());
            return;
        }

        DerivativeTransaction transaction = null==oldTransaction ? new DerivativeTransaction() : oldTransaction;
        transaction.setType(type);
        transaction.setWhen(when);
        transaction.setVolume(volume);
        transaction.setPrice(price);
        transaction.setTax(tax);
        transaction.setDescription(description);
        transaction.setDerivative(derivative);

        Stage dialog = DialogUtil.showIndeterminateProgressDialog(getWindow(),"Save Progress",
                "Saving derivative transaction. Please wait");
        dialog.setOnCloseRequest(e->e.consume());
        dialog.show();

        Task<DerivativeTransaction> task = DerivativeTasks.saveDerivativeTransaction(transaction);
        task.setOnSucceeded(e->{
            dialog.close();
            getWindow().close();
        });
        task.setOnFailed(e->{
            dialog.close();
            DialogUtil.alertError(getWindow(),"Save Error","Fail to save derivative transaction. Please try again");
            Log.error(TAG,"handleSaveButtonClicked",task.getException());
        });
        getApp().getAppExecutor().submit(task);
    }

    private void setInputErrorAlert(String message) {

    }
}