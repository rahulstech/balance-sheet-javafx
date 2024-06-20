package rahulstech.jfx.balancesheet.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import rahulstech.jfx.balancesheet.BalancesheetApp;
import rahulstech.jfx.balancesheet.concurrent.ImportTask;
import rahulstech.jfx.balancesheet.database.BalancesheetDb;
import rahulstech.jfx.balancesheet.json.model.DataModel;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.ViewLauncher;

import java.io.File;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class DashboardController extends Controller {

    @FXML
    private Button importButton;

    @FXML
    private Button accountsButton;

    @FXML
    private Button historiesButton;

    @FXML
    private Button chartsButton;

    private Future<?> importTask;

    @Override
    protected void onInitialize(ResourceBundle res) {
        importButton.setOnAction(event -> handleImportButtonClick());
        accountsButton.setOnAction(event -> handleAccountsButtonClick());
        historiesButton.setOnAction(event -> handleHistoriesButtonClick());
        chartsButton.setOnAction(event -> handleCharts());
    }

    private void handleImportButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        fileChooser.setTitle("Select JSON File");

        // Set initial directory to the last used directory
        fileChooser.setInitialDirectory(lastUsedDirectory);

        File selectedFile = fileChooser.showOpenDialog(importButton.getScene().getWindow());

        if (selectedFile != null) {
            // Store the last used directory
            lastUsedDirectory = selectedFile.getParentFile();
            handleImportJson(selectedFile);
        } else {
            DialogUtil.alertError(BalancesheetApp.getAppWindow(),"Error","No JSON backup file selected");
        }
    }

    private void handleImportJson(File json) {
        ExecutorService executor = BalancesheetApp.getAppExecutor();
        Stage appWindow = BalancesheetApp.getAppWindow();

        // show the progress dialog
        Stage dialog = DialogUtil.showIndeterminateProgressDialog(appWindow, "Message","Json import in progress please wait");
        dialog.show();

        // create and set up the import task
        ImportTask task = new ImportTask(json);
        task.setOnSucceeded(e -> {
            DataModel result = task.getValue();
            importTask = null;
            dialog.close();
            onImportComplete(true,result,null);
        });
        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            importTask = null;
            dialog.close();
            onImportComplete(false,null,exception);
        });
        importTask = executor.submit(task);

        dialog.setOnCloseRequest(e->{
            // cancel the ongoing task
            if (null != importTask && !importTask.isDone()) {
                importTask.cancel(true);
            }
        });
    }

    private void onImportComplete(boolean successful, DataModel result, Throwable exception) {
        Stage appWindow = BalancesheetApp.getAppWindow();
        if (successful) {
            ImportPickerTabsController controller = new ImportPickerTabsController(result);
            ViewLauncher loader = getViewLauncherBuilder()
                    .setTitle("Chose What To Add")
                    .setFxml("import_picker_tabs.fxml")
                    .setController(controller)
                    .build()
                    .load();
            loader.getWindow().show();
        }
        else {
            DialogUtil.alertError(appWindow,"Error","json file is not imported");
        }
    }

    private void handleAccountsButtonClick() {
        ViewLauncher loader = getViewLauncherBuilder()
                .setTitle("Account")
                .setStageStyle(StageStyle.UTILITY)
                .setFxml("accounts_list.fxml")
                .setHeight(500)
                .setWidth(600)
                .build()
                .load();
        loader.getWindow().show();
    }

    private void handleHistoriesButtonClick() {
        ViewLauncher loader = getViewLauncherBuilder()
                .setTitle("Histories")
                .setFxml("transaction_histories.fxml")
                .build()
                .load();
        loader.getWindow().setMaximized(true);
        loader.getWindow().show();
    }

    private void handleCharts() {
        ViewLauncher loader = new ViewLauncher.Builder()
                .setTitle("Charts")
                .setOwnerWindow(BalancesheetApp.getAppWindow())
                .setFxml("chart_browser.fxml")
                .build();
        loader.load();
        loader.getWindow().setMaximized(true);
        loader.getWindow().show();
    }

    private File lastUsedDirectory;

    @FXML
    private void handleDeleteDatabaseButtonClick(ActionEvent event) {
        DialogUtil.alertConfirmation(getWindow(),
                "Warning Delete","This action will cause permanent loss of data. " +
                        "This action is not undoable. Are you sure?",
                "Yes Delete", BalancesheetDb::deleteDatabase,
                "No Cancel",null);
    }
}
