package rahulstech.jfx.balancesheet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.util.TextUtil;

public class AccountListItemController extends Controller {

    @FXML
    private Label nameLabel;

    @FXML
    private Label balanceLabel;

    public void setAccount(Account account) {
        nameLabel.setText(account.getName());
        balanceLabel.setText(TextUtil.prettyPrintCurrency(account.getBalance()));
    }
}

