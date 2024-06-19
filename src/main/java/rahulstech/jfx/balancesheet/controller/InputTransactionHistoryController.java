package rahulstech.jfx.balancesheet.controller;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import rahulstech.jfx.balancesheet.BalancesheetApp;
import rahulstech.jfx.balancesheet.concurrent.TransactionHistorySaveTask;
import rahulstech.jfx.balancesheet.database.BalancesheetDb;
import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.database.entity.TransactionHistory;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.TransactionType;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.ViewLauncher;
import rahulstech.jfx.balancesheet.view.Chip;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class InputTransactionHistoryController extends Controller {

    private static final int MAX_CATEGORIES = 5;

    @FXML
    private Button buttonAddSrcAccount;

    @FXML
    private Button buttonAddDestAccount;

    @FXML
    private TextField destAccountField;

    @FXML
    private TextField srcAccountField;

    @FXML
    private HBox paneDestAccount;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField amountField;

    @FXML
    private ComboBox<TransactionType> transactionTypeCombo;

    @FXML
    private TextArea descriptionField;

    @FXML
    private Label destAccountLabel;

    @FXML
    private FlowPane categoriesFlowPane;

    private TransactionHistory oldHistory;

    private CategoryController categoryController;

    private AccountsListController srcAccountController;

    private AccountsListController destAccountController;

    public void setOldHistory(TransactionHistory oldHistory) {
        this.oldHistory = oldHistory;
        datePicker.setValue(oldHistory.getWhen());
        amountField.setText(oldHistory.getAmount().toString());
        transactionTypeCombo.getSelectionModel().select(oldHistory.getType());
        setSrcAccount(oldHistory.getSrc());
        setDestAccount(oldHistory.getDest());
        descriptionField.setText(oldHistory.getDescription());
        addCategories(oldHistory.getCategories());

        buttonAddSrcAccount.setDisable(true);
        buttonAddDestAccount.setDisable(true);
        transactionTypeCombo.setDisable(true);
    }

    @Override
    protected void onInitialize(ResourceBundle res) {

        setupNumericTextFormater();

        datePicker.setValue(LocalDate.now());
        // Initialize the combo boxes with data
        transactionTypeCombo.getItems().setAll(TransactionType.values());

        // Add a listener to the transactionTypeCombo to show/hide destAccountCombo and its label
        transactionTypeCombo.valueProperty().addListener( e -> {
            boolean isTransfer = transactionTypeCombo.getSelectionModel().getSelectedItem() == TransactionType.TRANSFER;
            //destAccountCombo.setVisible(isTransfer);
            setDestAccountVisibility(isTransfer);
        });

        // Set initial visibility based on the default selection
        setDestAccountVisibility(false);
        transactionTypeCombo.getSelectionModel().selectFirst();
    }

    private void setDestAccountVisibility(boolean visible) {
        destAccountLabel.setVisible(visible);
        paneDestAccount.setVisible(visible);
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

        StringConverter<Currency> converter = new StringConverter<>() {
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

        amountField.setTextFormatter(textFormatter);
    }

    @FXML
    private void handleSaveAction() {
        LocalDate date = datePicker.getValue();
        Currency amount = Currency.from(amountField.getText());
        Account srcAccount = (Account) srcAccountField.getUserData();
        Account destAccount = (Account) destAccountField.getUserData();
        TransactionType transactionType = transactionTypeCombo.getValue();
        String description = descriptionField.getText();
        List<Category> categories = getSelectedCategories();

        if (categories.size() > MAX_CATEGORIES) {
            DialogUtil.alertError(getWindow(),"Error","Maximum "+MAX_CATEGORIES+" categories are allowed");
            return;
        }
        if (date != null && amount != null && srcAccount != null && transactionType != null) {
            TransactionHistory transactionHistory = null == this.oldHistory ? new  TransactionHistory() : this.oldHistory;
            transactionHistory.setWhen(date);
            transactionHistory.setAmount(amount);
            transactionHistory.setSrc(srcAccount);
            if (transactionType == TransactionType.TRANSFER) {
                transactionHistory.setDest(destAccount);
            }
            transactionHistory.setType(transactionType);
            transactionHistory.setDescription(description);
            transactionHistory.setCategories(categories);
            saveHistory(transactionHistory);
        } else {
            DialogUtil.alertError(getWindow(),"Error","Some inputs contain error.");
        }
    }

    private void saveHistory(TransactionHistory history) {
        Stage dialog = DialogUtil.showIndeterminateProgressDialog(getWindow(),"Save Progress","Saving history...");
        dialog.setOnCloseRequest(e->e.consume());
        dialog.show();
        TransactionHistorySaveTask task = new TransactionHistorySaveTask(history);
        task.setOnSucceeded(e->{
            dialog.close();
            getWindow().close();
        });
        task.setOnFailed(e->{
            task.getException().printStackTrace();
            System.err.println(task.getException());
            dialog.close();
            DialogUtil.alertError(getWindow(),"Error","History not saved");
        });
        BalancesheetApp.getAppExecutor().submit(task);
    }

    private List<Category> getSelectedCategories() {
        ArrayList<Category> categories = new ArrayList<>();
        List children = categoriesFlowPane.getChildren();
        for (Object child : children) {
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                Category category = (Category) chip.getUserData();
                categories.add(category);
            }
        }
        return categories;
    }

    @FXML
    private void handleSelectSrcAccount() {
        if (null == srcAccountController) {
            ViewLauncher launcher = getViewLauncherBuilder()
                    .setFxml("accounts_list.fxml")
                    .setTitle("Choose Source Account")
                    .setStageModality(Modality.APPLICATION_MODAL)
                    .build().load();
            AccountsListController controller = launcher.getController();
            controller.getAccountListView().getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            controller.getAccountListView().getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super Account>) change -> {
                Account account = controller.getAccountListView().getSelectionModel().getSelectedItem();
                setSrcAccount(account);
            });
            this.srcAccountController = controller;
        }
        this.srcAccountController.getWindow().show();
    }

    private void setSrcAccount(Account account) {
        if (null != account) {
            srcAccountField.setUserData(account);
            srcAccountField.setText(account.getName());
        } else {
            srcAccountField.setUserData(null);
            srcAccountField.setText(null);
        }
    }

    @FXML
    private void handleSelectDestAccount() {
        if (destAccountController == null) {
            ViewLauncher launcher = getViewLauncherBuilder()
                    .setFxml("accounts_list.fxml")
                    .setTitle("Choose Destination Account")
                    .setStageModality(Modality.APPLICATION_MODAL)
                    .build().load();
            AccountsListController controller = launcher.getController();
            controller.getAccountListView().getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            controller.getAccountListView().getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super Account>) change -> {
                Account account = controller.getAccountListView().getSelectionModel().getSelectedItem();
                setDestAccount(account);
            });
            this.destAccountController = controller;
        }
        this.destAccountController.getWindow().show();
    }

    private void setDestAccount(Account account) {
        if (null != account) {
            destAccountField.setUserData(account);
            destAccountField.setText(account.getName());
        }
        else {
            destAccountField.setUserData(null);
            destAccountField.setText(null);
        }
    }

    @FXML
    private void handleAddCategoryAction() {
        if (null == categoryController) {
            ViewLauncher launcher = getViewLauncherBuilder().setFxml("category.fxml")
                    .setTitle("Choose Categories")
                    .build().load();
            categoryController = launcher.getController();
            ObservableList<Category> selections = categoryController.getCategoryList().getSelectionModel().getSelectedItems();
            selections.addListener((ListChangeListener<? super Category>) change -> addCategories(new ArrayList<>(selections)));
        }
        categoryController.getWindow().show();
    }

    public void addCategories(List<Category> categories) {
        categoriesFlowPane.getChildren().clear();
        for (Category category : categories) {
            Chip chip = new Chip(category.getName());
            chip.setUserData(category);
            categoriesFlowPane.getChildren().add(chip);
        }
    }
}
