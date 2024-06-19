package rahulstech.jfx.balancesheet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import rahulstech.jfx.balancesheet.json.model.Account;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class AccountImportPickerController extends Controller {

    @FXML
    private Button selectAllButton;

    @FXML
    private ListView<Account> listView;

    private final List<Account> accounts;

    public AccountImportPickerController(List<Account> accounts) {
        this.accounts = null == accounts ? Collections.emptyList() : accounts;
    }

    @Override
    protected void onInitialize(ResourceBundle resources) {
        initializeAccountList();
        configureSelectAllButton();
    }

    private void initializeAccountList() {

        listView.getItems().addAll(accounts);
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getAccount_name());
                }
            }
        });

        listView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
    }

    private void configureSelectAllButton() {
        selectAllButton.setOnAction(e->{
            if (listView.getSelectionModel().getSelectedItems().size() == accounts.size()) {
                listView.getSelectionModel().clearSelection();
            }
            else {
                listView.getSelectionModel().selectAll();
            }
        });
    }

    public List<Account> getAllSelectedAccounts() {
        return new ArrayList<>(listView.getSelectionModel().getSelectedItems());
    }
}
