package rahulstech.jfx.balancesheet.controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import rahulstech.jfx.balancesheet.concurrent.TaskUtils;
import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.Log;
import rahulstech.jfx.balancesheet.util.ViewLauncher;
import rahulstech.jfx.balancesheet.util.ViewLoader;

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
        accountListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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

    private void handleDeleteAccounts(List<Account> accounts) {
        DialogUtil.alertConfirmation(getWindow(),
                "Warning","Selected account will be deleted permanently. Are you sure to proceed?",
                "Delete",()->{
                    Task<Boolean> task = TaskUtils.deleteAccount(accounts,
                            t-> accountListView.getItems().removeAll(accounts),
                            t->{
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
                .setStageStyle(StageStyle.UTILITY)
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
        accountListView.getItems().clear();
        accountListView.getItems().addAll(values);
    }

    @FXML
    private void handleDeleteButtonClicked(ActionEvent event) {
        List<Account> accounts = accountListView.getSelectionModel().getSelectedItems();
        if (accounts.isEmpty()) {
            return;
        }
        handleDeleteAccounts(accounts);
    }

    @FXML
    private void handleEditButtonClicked() {
        if (accountListView.getSelectionModel().getSelectedItems().size()!=1) {
            Log.debug(TAG,"either multiple account or nothing selected, can not perform edit");
            return;
        }
        Account account = accountListView.getSelectionModel().getSelectedItem();
        handleEditAccount(account);
    }
}

