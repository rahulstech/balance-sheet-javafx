package rahulstech.jfx.balancesheet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import rahulstech.jfx.balancesheet.database.entity.Account;

public class AccountListItemController {

    @FXML
    private Label nameLabel;

    @FXML
    private Label balanceLabel;

    public void setAccount(Account account) {
        nameLabel.setText(account.getName());
        balanceLabel.setText(account.getBalance().toString());
    }
}

