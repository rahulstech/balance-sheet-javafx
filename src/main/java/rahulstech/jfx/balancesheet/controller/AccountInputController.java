package rahulstech.jfx.balancesheet.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import rahulstech.jfx.balancesheet.concurrent.TaskUtils;
import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.Log;
import rahulstech.jfx.balancesheet.util.TextUtil;

import java.util.ResourceBundle;

@SuppressWarnings("ALL")
public class AccountInputController extends Controller {

    private static final String TAG = AccountInputController.class.getSimpleName();

    @FXML
    private TextField nameField;

    @FXML
    private TextField balanceField;

    private Account oldAccount;

    @SuppressWarnings("unchecked")
    public void setAccount(Account oldAccount) {
        this.oldAccount = oldAccount;
        nameField.setText(oldAccount.getName());
        ((TextFormatter<Currency>) balanceField.getTextFormatter()).setValue(oldAccount.getBalance());
    }

    @Override
    protected void onInitialize(ResourceBundle res) {
        setupNumericTextFormater();
    }

    private void setupNumericTextFormater() {
        balanceField.setTextFormatter(TextUtil.createCurrencyTextFormater());
    }

    @SuppressWarnings("unchecked")
    @FXML
    private void handleSave() {
        String name = nameField.getText();
        Currency balance = ((TextFormatter<Currency>) balanceField.getTextFormatter()).getValue();

        if (name == null || name.trim().isEmpty()) {
            DialogUtil.alertError(getWindow(),"Input Error","account name and/or balance are empty. both are required. " +
                    "Add valid values to name and balance");
            return;
        }

        Account account = null == this.oldAccount ? new Account() : this.oldAccount;
        account.setName(name);
        account.setBalance(balance);

        save(account);
    }

    private void save(Account account) {
        Stage dialog = DialogUtil.showIndeterminateProgressDialog(getWindow(),"Progress","Saving account, please wait");
        dialog.show();
        Task<Account> task = TaskUtils.saveAccount(account,t->{
            dialog.close();
            getWindow().close();
        },t->{
            Log.error(TAG,"save account", t.getException());
            dialog.close();
            DialogUtil.alertError(getWindow(),"Save Error","Account not saved");
        });
        getApp().getAppExecutor().submit(task);
    }
}
