package rahulstech.jfx.balancesheet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import rahulstech.jfx.balancesheet.BalancesheetApp;
import rahulstech.jfx.balancesheet.concurrent.InsertTask;
import rahulstech.jfx.balancesheet.json.model.Account;
import rahulstech.jfx.balancesheet.json.model.DataModel;
import rahulstech.jfx.balancesheet.json.model.Person;
import rahulstech.jfx.balancesheet.util.DialogUtil;

import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class ImportPickerTabsController extends Controller {

    @FXML
    private Tab tabAccounts;

    @FXML
    private Tab tabPeople;

    private final DataModel model;

    private AccountImportPickerController accountsController;

    private PersonImportPickerController peopleController;

    private Future<?> insertTask;

    private Stage progressDialogWindow;

    public ImportPickerTabsController(DataModel model) {
        this.model = model;
    }

    @Override
    protected void onInitialize(ResourceBundle res) {
        this.accountsController = (AccountImportPickerController) loadAccountsPickerView(model.getAccounts());
        this.peopleController = (PersonImportPickerController) loadPeoplePickerView(model.getPeople());
        tabAccounts.setContent(accountsController.getRoot());
        tabPeople.setContent(peopleController.getRoot());
    }

    private Controller loadAccountsPickerView(List<Account> accounts) {
        AccountImportPickerController controller = new AccountImportPickerController(accounts);
        return loadPickerView(controller);
    }

    private Controller loadPeoplePickerView(List<Person> people) {
        PersonImportPickerController controller = new PersonImportPickerController(people);
        return loadPickerView(controller);
    }

    private Controller loadPickerView(Controller controller) {
        return getViewLoader().setController(controller).setFxml("picker_list_layout.fxml").load().getController();
    }

    @FXML
    private void handleCancel() {
        DialogUtil.alertConfirmation(getWindow(),"Caution!",
                "Changes are not saved. If you leave then changes will be lost permanently. Are you sure you want to cancel?",
                "Yes, Cancel",()->close(),"No, Continue",null);
    }

    @FXML
    private void handleAddSelected() {
        Stage stage = DialogUtil.showIndeterminateProgressDialog(getWindow(),"Message",
                "Adding imported values into database in progress. please wait till finish.");
        stage.setOnCloseRequest(e->e.consume());
        stage.show();
        this.progressDialogWindow = stage;

        List<Account> accounts = accountsController.getAllSelectedAccounts();
        List<Person> people = peopleController.getAllSelectedPeople();

        ExecutorService executor = BalancesheetApp.getAppExecutor();
        InsertTask task = new InsertTask(model,accounts,people);
        task.setOnSucceeded(e->onInsertComplete(task.getValue(),null));
        task.setOnFailed(e->onInsertComplete(false,task.getException()));
        insertTask = executor.submit(task);
    }

    private void onInsertComplete(boolean success, Throwable error) {
        if (success) {
            progressDialogWindow.close();
            getWindow().close();
        }
        else {
            error.printStackTrace();
            progressDialogWindow.close();
            DialogUtil.alertError(getWindow(),"Error","Fail to insert imported data into database. Please try again.");
        }
    }

    private void close() {
        if (null != insertTask) {
            insertTask.cancel(true);
        }
        getWindow().close();
    }
}
