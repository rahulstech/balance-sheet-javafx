package rahulstech.jfx.balancesheet.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;
import org.kordamp.ikonli.javafx.FontIcon;
import rahulstech.jfx.balancesheet.concurrent.TaskUtils;
import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.Log;
import rahulstech.jfx.balancesheet.util.ViewLauncher;
import rahulstech.jfx.balancesheet.util.ViewLoader;

import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@SuppressWarnings("ALL")
public class AccountsListController extends Controller {

    private static final String TAG = AccountsListController.class.getSimpleName();

    @FXML
    private TextField searchField;

    @FXML
    private ListView<Account> accountListView;

    private List<Account> accounts;
    private Future<?> currentTask;
    private Future<?> queryTask;

    @Override
    protected void onInitialize(ResourceBundle res) {
        accountListView.setCellFactory(listView->{
            return new ListCell<Account>() {
                    @Override
                    protected void updateItem(Account account, boolean empty) {
                        super.updateItem(account, empty);
                        if (empty || account == null) {
                            setGraphic(null);
                            setContextMenu(null);
                        } else {
                            ViewLoader loader = getViewLoader().setFxml("account_list_item.fxml").load();
                            setGraphic(loader.getRoot());
                            AccountListItemController controller = loader.getController();
                            controller.setAccount(account);
                            setContextMenu(createContextMenu(account));
                        }
                    }
                };
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (currentTask != null) {
                currentTask.cancel(true);  // Cancel the currently running task
            }
            ExecutorService executor = getApp().getAppExecutor();
            Task<List<Account>> task = TaskUtils.filterAccount(accounts, newValue, t-> setAccounts(t.getValue()),
                    t-> Log.error(TAG,"filter account",t.getException()));
            currentTask = executor.submit(task);  // Submit the new task
        });
        loadAccounts();
    }

    public ListView<Account> getAccountListView() {
        return accountListView;
    }

    private ContextMenu createContextMenu(Account account) {
        ContextMenu menu = new ContextMenu();

        MenuItem edit = new MenuItem("Edit");
        edit.setGraphic(new FontIcon("mdi-pencil"));
        edit.setOnAction(e->handleEditAccount(account));

        MenuItem delete = new MenuItem("Delete");
        delete.setGraphic(new FontIcon("mdi-delete"));
        delete.setOnAction(e->handleDeleteAccount(account));

        menu.getItems().addAll(edit,delete);
        return menu;
    }

    private void handleEditAccount(Account account) {
        ViewLauncher launcher = getViewLauncherBuilder()
                .setFxml("account_input.fxml")
                .setTitle("Edit Account")
                .setWidth(400)
                .setHeight(200)
                .setStageModality(Modality.APPLICATION_MODAL)
                .build().load();
        AccountInputController controller = launcher.getController();
        launcher.getWindow().show();
        controller.setAccount(account);
    }

    private void handleDeleteAccount(Account account) {
        DialogUtil.alertConfirmation(getWindow(),
                "Warning","Selected account will be deleted permanently. Are you sure to proceed?",
                "Delete",()->{
                    Task<Boolean> task = TaskUtils.deleteAccount(Collections.singletonList(account),
                            t-> accountListView.getItems().remove(account), t->{
                        Log.error(TAG,"delete accounts",t.getException());
                        DialogUtil.alertError(getWindow(),"Error","Account not deleted.");
                    });
                    getApp().getAppExecutor().
                            execute(task);
                },"Cancel",null);

    }

    @FXML
    private void handleAddAccount(ActionEvent e) {
        ViewLauncher launcher = getViewLauncherBuilder()
                .setFxml("account_input.fxml")
                .setTitle("New Account")
                .setWidth(400)
                .setHeight(200)
                .setStageModality(Modality.APPLICATION_MODAL)
                .build().load();
        launcher.getWindow().show();
    }

    @FXML
    private void handleRefresh(ActionEvent a) {
        loadAccounts();
    }

    private void loadAccounts() {
        if (null != queryTask) {
            queryTask.cancel(true);
        }
        Task<List<Account>> task = TaskUtils.getAllAccounts(t->{
            this.accounts = t.getValue();
            setAccounts(this.accounts);
        },t->Log.error(TAG,"load accounts",t.getException()));
        queryTask = getApp().getAppExecutor().submit(task);
    }

    private void setAccounts(List<Account> values) {
        ObservableList<Account> accounts = FXCollections.observableList(values);
        accountListView.setItems(accounts);
    }
}

