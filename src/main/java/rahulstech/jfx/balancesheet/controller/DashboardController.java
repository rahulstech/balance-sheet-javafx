package rahulstech.jfx.balancesheet.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import rahulstech.jfx.balancesheet.concurrent.ImportTask;
import rahulstech.jfx.balancesheet.database.BalancesheetDb;
import rahulstech.jfx.balancesheet.json.model.DataModel;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.Log;
import rahulstech.jfx.balancesheet.util.ViewLauncher;

import java.io.File;
import java.util.ResourceBundle;
import java.util.concurrent.Future;

@SuppressWarnings("ALL")
public class DashboardController extends Controller {

    private static final String TAG = DashboardController.class.getSimpleName();

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
    protected void onInitialize(ResourceBundle res) {}

    @FXML
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
            DialogUtil.alertError(getWindow(),"Error","No JSON backup file selected");
        }
    }

    private void handleImportJson(File json) {
        // show the progress dialog
        Stage dialog = DialogUtil.showIndeterminateProgressDialog(getWindow(), "Message","Json import in progress please wait");
        dialog.show();

        // create and set up the import task
        ImportTask task = new ImportTask(json);
        task.setOnSucceeded(e -> {
            importTask = null;
            dialog.close();
            onImportComplete(true,task.getValue(),null);
        });
        task.setOnFailed(e -> {
            importTask = null;
            dialog.close();
            onImportComplete(false,null,task.getException());
        });
        importTask = getApp().getAppExecutor().submit(task);

        dialog.setOnCloseRequest(e->{
            // cancel the ongoing task
            if (null != importTask && !importTask.isDone()) {
                importTask.cancel(true);
            }
        });
    }

    private void onImportComplete(boolean successful, DataModel result, Throwable exception) {
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
            Log.error(TAG,"import",exception);
            DialogUtil.alertError(getWindow(),"Error","json file is not imported");
        }
    }

    @FXML
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

    @FXML
    private void handleHistoriesButtonClick() {
        ViewLauncher loader = getViewLauncherBuilder()
                .setTitle("Histories")
                .setFxml("transaction_histories.fxml")
                .build()
                .load();
        loader.getWindow().setMaximized(true);
        loader.getWindow().show();
    }

    @FXML
    private void handleCharts() {
        ViewLauncher loader = getViewLauncherBuilder()
                .setTitle("Charts")
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

    @FXML
    private void handleBudgetsButtonAction(ActionEvent event) {
        ViewLauncher viewLauncher = getViewLauncherBuilder()
                .setTitle("Budgets")
                .setFxml("budget.fxml")
                .build();
        viewLauncher.load();
        viewLauncher.getWindow().setMaximized(true);
        viewLauncher.getWindow().show();
    }
}
