package rahulstech.jfx.balancesheet.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import rahulstech.jfx.balancesheet.concurrent.TaskUtils;
import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.Log;

import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

@SuppressWarnings("ALL")
public class AccountInputController extends Controller {

    private static final String TAG = AccountInputController.class.getSimpleName();

    @FXML
    private TextField nameField;

    @FXML
    private TextField balanceField;

    private Account oldAccount;

    public void setAccount(Account oldAccount) {
        this.oldAccount = oldAccount;
        nameField.setText(oldAccount.getName());
        balanceField.setText(oldAccount.getBalance().toString());
    }

    @Override
    protected void onInitialize(ResourceBundle res) {
        setupNumericTextFormater();
    }

    private void setupNumericTextFormater() {
        Pattern validEditingState = Pattern.compile("^-?\\d*(\\.\\d{0,2})?$");

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            if (validEditingState.matcher(text).matches()) {
                return change;
            } else {
                return null;
            }
        };

        StringConverter<Currency> converter = new StringConverter<Currency>() {
            @Override
            public String toString(Currency number) {
                return null == number ? "" : number.toString();
            }

            @Override
            public Currency fromString(String s) {
                return Currency.from(s);
            }
        };

        TextFormatter<Currency> textFormatter = new TextFormatter<>(converter, null, filter);

        balanceField.setTextFormatter(textFormatter);
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText();
        String balanceText = balanceField.getText();

        if (name == null || name.trim().isEmpty()) {
            DialogUtil.alertError(getWindow(),"Input Error","account name and/or balance are empty. both are required. " +
                    "Add valid values to name and balance");
            return;
        }

        Currency balance = Currency.from(balanceText);
        Account account = null == this.oldAccount ? new Account() : this.oldAccount;
        account.setName(name);
        account.setBalance(balance);

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
