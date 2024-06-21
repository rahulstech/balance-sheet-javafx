package rahulstech.jfx.balancesheet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import rahulstech.jfx.balancesheet.concurrent.InsertTask;
import rahulstech.jfx.balancesheet.json.model.Account;
import rahulstech.jfx.balancesheet.json.model.DataModel;
import rahulstech.jfx.balancesheet.json.model.Person;
import rahulstech.jfx.balancesheet.util.DialogUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@SuppressWarnings("ALL")
public class ImportPickerTabsController extends Controller {

    @FXML
    private Tab tabAccounts;

    @FXML
    private Tab tabPeople;

    @FXML
    private Tab tabMisc;

    private final DataModel model;

    private AccountImportPickerController accountsController;

    private PersonImportPickerController peopleController;

    private ImportMiscTabController miscTabController;

    private Future<?> insertTask;

    public ImportPickerTabsController(DataModel model) {
        this.model = model;
    }

    @Override
    protected void onInitialize(ResourceBundle res) {
        accountsController = new AccountImportPickerController(model.getAccounts());
        getViewLoader().setController(accountsController).setFxml("picker_list_layout.fxml").load();
        tabAccounts.setContent(accountsController.getRoot());

        this.peopleController = new PersonImportPickerController(model.getPeople());
        getViewLoader().setController(peopleController).setFxml("picker_list_layout.fxml").load();
        tabPeople.setContent(peopleController.getRoot());

        miscTabController = getViewLoader().setFxml("import_misc_tab.fxml").load().getController();
        tabMisc.setContent(miscTabController.getRoot());
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

        List<Account> accounts = accountsController.getAllSelectedAccounts();
        List<Person> people = peopleController.getAllSelectedPeople();
        LocalDate startDate = miscTabController.getStartDate();
        LocalDate endDate = miscTabController.getEndDate();
        List<ImportMiscTabController.ImportOptions> importOptions = miscTabController.getSelectedImportOptions();

        InsertTask.FilterData filterData = new InsertTask.FilterData();
        filterData.endDate = endDate;
        filterData.startDate = startDate;
        filterData.accounts = accounts;
        filterData.people = people;
        filterData.importAccounts = importOptions.contains(ImportMiscTabController.ImportOptions.Accounts);
        filterData.importCreditTransactions = importOptions.contains(ImportMiscTabController.ImportOptions.Credit);
        filterData.importDebitTransactions = importOptions.contains(ImportMiscTabController.ImportOptions.Debit);
        filterData.importTransfers = importOptions.contains(ImportMiscTabController.ImportOptions.Transfers);

        ExecutorService executor = getApp().getAppExecutor();
        InsertTask task = new InsertTask(model,filterData);
        task.setOnSucceeded(e->{
            stage.close();
            onInsertComplete(task.getValue(),null);
        });
        task.setOnFailed(e->{
            stage.close();
            onInsertComplete(false,task.getException());
        });
        insertTask = executor.submit(task);
    }

    private void onInsertComplete(boolean success, Throwable error) {
        if (success) {
            getWindow().close();
        }
        else {
            error.printStackTrace();
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
