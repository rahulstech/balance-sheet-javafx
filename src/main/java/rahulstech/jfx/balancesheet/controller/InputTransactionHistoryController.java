package rahulstech.jfx.balancesheet.controller;

import javafx.animation.*;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import rahulstech.jfx.balancesheet.concurrent.TransactionHistorySaveTask;
import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.database.entity.TransactionHistory;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.TransactionType;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.Log;
import rahulstech.jfx.balancesheet.util.TextUtil;
import rahulstech.jfx.balancesheet.util.ViewLauncher;
import rahulstech.jfx.balancesheet.view.Chip;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


@SuppressWarnings({"ALL","unchecked"})
public class InputTransactionHistoryController extends Controller {

    private static final String TAG = InputTransactionHistoryController.class.getSimpleName();

    private static final int MAX_CATEGORIES = 5;

    private static final long SHORT_ANIMATON_DURATION = 200;

    private static final long LONG_ANIMATION_DURATION = 500;

    @FXML
    private AnchorPane srcAccountPanel;

    @FXML
    private AnchorPane destAccountPanel;

    @FXML
    private FontIcon caretIcon;

    @FXML
    private GridPane additionalFields;

    @FXML
    private RadioButton taxSrc;

    @FXML
    private RadioButton taxDest;

    @FXML
    private ToggleButton buttonAddMore;

    @FXML
    private Button buttonAddSrcAccount;

    @FXML
    private Button buttonAddDestAccount;

    @FXML
    private HBox paneDestAccount;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField amountField;

    @FXML
    private TextField taxField;

    @FXML
    private ComboBox<TransactionType> transactionTypeCombo;

    @FXML
    private TextArea descriptionField;

    @FXML
    private Label destAccountLabel;

    @FXML
    private FlowPane categoriesFlowPane;

    private ToggleGroup taxGroup;

    private TransactionHistory oldHistory;

    private CategoryController categoryController;

    private AccountsListController srcAccountController;

    private AccountsListController destAccountController;

    public void setOldHistory(TransactionHistory oldHistory) {
        this.oldHistory = oldHistory;
        TransactionType type = oldHistory.getType();
        datePicker.setValue(oldHistory.getWhen());
        datePicker.setConverter(TextUtil.getLocalDateStringConverter(TextUtil.DATE_PICK_FORMAT));
        ((TextFormatter<Currency>) amountField.getTextFormatter()).setValue(oldHistory.getAmount());
        transactionTypeCombo.getSelectionModel().select(oldHistory.getType());
        if (type==TransactionType.DEPOSIT) {
            // set in destAccount input
            setDestAccount(oldHistory.getSrc());
        }
        else if (type==TransactionType.WITHDRAW) {
            // set in srcAccount input
            setSrcAccount(oldHistory.getSrc());
        }
        else {
            setSrcAccount(oldHistory.getSrc());
            setDestAccount(oldHistory.getDest());
        }
        descriptionField.setText(oldHistory.getDescription());
        addCategories(oldHistory.getCategories());
        if (null!=oldHistory.getTax()) {
            ((TextFormatter<Currency>) taxField.getTextFormatter()).setValue(oldHistory.getTax());
            setTaxChoiceForType(type);
        }

        setSrcAccountDisabled(true);
        setDestAccountDisabled(true);
        transactionTypeCombo.setDisable(true);

        if (null!=oldHistory.getSrc() || null!=oldHistory.getDest() || null!=oldHistory.getTax()) {
            toggleAddtionalSection(true);
        }
    }

    @Override
    protected void onInitialize(ResourceBundle res) {

        setupNumericTextFormater();

        taxGroup = setupTaxGroup();

        datePicker.setValue(LocalDate.now());
        datePicker.setConverter(TextUtil.getLocalDateStringConverter(TextUtil.DATE_PICK_FORMAT));

        // Initialize the combo boxes with data
        transactionTypeCombo.getItems().setAll(TransactionType.values());

        // Add a listener to the transactionTypeCombo to show/hide destAccountCombo and its label
        transactionTypeCombo.valueProperty().addListener( e -> {
            onChangeTransactionType(transactionTypeCombo.getSelectionModel().getSelectedItem());
        });

        // Set initial visibility based on the default selection
        transactionTypeCombo.getSelectionModel().selectFirst();

        buttonAddMore.selectedProperty().addListener((observable, oldValue, newValue) -> toggleAddtionalSection(newValue));
    }

    private void onChangeTransactionType(TransactionType newType) {
        if (newType==TransactionType.DEPOSIT) {
            // DEPOSITE only destAccount enabled
            // and taxDest checked and both tax radio ddisbaled,
            setSrcAccountDisabled(true);
            setDestAccountDisabled(false);
        }
        else if (newType==TransactionType.WITHDRAW) {
            // WITHDRAW only srcAccount enabled
            // and taxSrc checked and both tax radios disabled,
            setSrcAccountDisabled(false);
            setDestAccountDisabled(true);
        }
        else {
            // TRANSFER both srcAccount and destAccount enabled
            // and taxSrc and taxDest enable and taxSrc is checked
            setSrcAccountDisabled(false);
            setDestAccountDisabled(false);
        }
        setTaxChoiceForType(newType);
    }

    private ToggleGroup setupTaxGroup() {
        ToggleGroup taxGroup = new ToggleGroup();
        taxGroup.getToggles().addAll(taxSrc,taxDest);
        return taxGroup;
    }

    private void setSrcAccountDisabled(boolean disabled) {
        srcAccountPanel.setDisable(disabled);
        buttonAddSrcAccount.setDisable(disabled);
    }

    private void setDestAccountDisabled(boolean disabled) {
        destAccountPanel.setDisable(disabled);
        buttonAddDestAccount.setDisable(disabled);
    }

    private void setTaxChoiceForType(TransactionType type) {
        if (type==TransactionType.DEPOSIT) {
            // DEPOSITE taxDest checked and both tax radio ddisbaled,
            taxGroup.selectToggle(taxDest);
            taxSrc.setDisable(true);
            taxDest.setDisable(true);
        }
        else if (type==TransactionType.WITHDRAW) {
            // WITHDRAW taxSrc checked and both tax radios disabled,
            taxGroup.selectToggle(taxSrc);
            taxSrc.setDisable(true);
            taxDest.setDisable(true);
        }
        else {
            // TRANSFER taxSrc and taxDest enable and taxSrc is checked
            taxGroup.selectToggle(taxSrc);
            taxSrc.setDisable(false);
            taxDest.setDisable(false);
        }
    }

    private void setupNumericTextFormater() {
        amountField.setTextFormatter(TextUtil.createCurrencyTextFormater());
        taxField.setTextFormatter(TextUtil.createCurrencyTextFormater());
    }

    private Account getSelectedSrcAccount() {
        if (srcAccountPanel.getChildren().isEmpty()) {
            return null;
        }
        Chip chip = (Chip) srcAccountPanel.getChildren().get(0);
        return (Account) chip.getUserData();
    }

    private Account getSelectedDestAccount() {
        if (destAccountPanel.getChildren().isEmpty()) {
            return null;
        }
        Chip chip = (Chip) destAccountPanel.getChildren().get(0);
        return (Account) chip.getUserData();
    }

    @FXML
    private void handleSaveAction() {
        LocalDate date = datePicker.getValue();
        Currency amount = (Currency) amountField.getTextFormatter().getValue();
        Currency tax = (Currency) taxField.getTextFormatter().getValue();
        boolean isTaxSrc = taxGroup.getSelectedToggle()==taxSrc;
        Account srcAccount = (Account) getSelectedSrcAccount();
        Account destAccount = (Account) getSelectedDestAccount();
        TransactionType transactionType = transactionTypeCombo.getValue();
        String description = descriptionField.getText();
        List<Category> categories = getSelectedCategories();

        if (categories.size() > MAX_CATEGORIES) {
            DialogUtil.alertError(getWindow(),"Error","Maximum "+MAX_CATEGORIES+" categories are allowed");
            return;
        }
        if (date != null && amount != null && transactionType != null) {
            TransactionHistory transactionHistory = null == this.oldHistory ? new  TransactionHistory() : this.oldHistory;
            transactionHistory.setWhen(date);
            transactionHistory.setAmount(amount);
            transactionHistory.setType(transactionType);
            transactionHistory.setDescription(description);
            transactionHistory.setCategories(categories);
            transactionHistory.setTax(tax);
            // the input account and entity account have different naming
            // Note: in case of DEPOsITE AND WITHDRAW taxAtSrc = true always
            // becuase only one account involved in those cases. So, if tax is
            // provided then tax will be charges from that account obviously.
            // for TRANSFER both accounts are available therefore it depends on user's choice
            if (transactionType==TransactionType.DEPOSIT) {
                // DEPOSITE taks input destAccount
                transactionHistory.setSrc(destAccount);
                transactionHistory.setTaxSrc(null!=tax);
            }
            else if (transactionType==TransactionType.WITHDRAW) {
                // WITDRAW takes input srcAccount
                transactionHistory.setSrc(srcAccount);
                transactionHistory.setTaxSrc(null!=tax);
            }
            else {
                // TRANSFER taskes both as it is
                transactionHistory.setSrc(srcAccount);
                transactionHistory.setDest(destAccount);
                transactionHistory.setTaxSrc(isTaxSrc);
            }
            saveHistory(transactionHistory);
        } else {
            DialogUtil.alertError(getWindow(),"Error","Some inputs contain error.");
        }
    }

    private void saveHistory(TransactionHistory history) {
        Stage dialog = DialogUtil.showIndeterminateProgressDialog(getWindow(),"Save Progress","Saving history...");
        dialog.show();
        TransactionHistorySaveTask task = new TransactionHistorySaveTask(history);
        task.setOnSucceeded(e->{
            dialog.close();
            getWindow().close();
        });
        task.setOnFailed(e->{
            Log.error(TAG,"save-history", task.getException());
            dialog.close();
            DialogUtil.alertError(getWindow(),"Error","History not saved");
        });
        getApp().getAppExecutor().submit(task);
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
                    .setStageStyle(StageStyle.UTILITY)
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
        srcAccountPanel.getChildren().clear();
        if (null==account) {
            return;
        }
        Chip chip = new Chip(account.getName());
        chip.setUserData(account);
        srcAccountPanel.getChildren().add(chip);
    }

    @FXML
    private void handleSelectDestAccount() {
        if (destAccountController == null) {
            ViewLauncher launcher = getViewLauncherBuilder()
                    .setFxml("accounts_list.fxml")
                    .setTitle("Choose Destination Account")
                    .setStageStyle(StageStyle.UTILITY)
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
        destAccountPanel.getChildren().clear();
        if (null==account) {
            return;
        }
        Chip chip = new Chip(account.getName());
        chip.setUserData(account);
        destAccountPanel.getChildren().add(chip);
    }

    @FXML
    private void handleAddCategoryAction() {
        if (null == categoryController) {
            ViewLauncher launcher = getViewLauncherBuilder().setFxml("category.fxml")
                    .setTitle("Choose Categories")
                    .build().load();
            categoryController = launcher.getController();
            categoryController.getWindow().setOnCloseRequest(e->addCategories(categoryController.getCategoryList().getSelectionModel().getSelectedItems()));
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

    private void toggleAddtionalSection(boolean visible) {
        // stop the ongoing animation first
        Animation old_animation = (Animation) additionalFields.getUserData();
        if (null != old_animation) {
            old_animation.stop();
        }

        ParallelTransition animation = new ParallelTransition();

        // Fade Animation
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(LONG_ANIMATION_DURATION), additionalFields);

        // Ratate animation for caret icon
        RotateTransition rotateCaret = new RotateTransition(Duration.millis(SHORT_ANIMATON_DURATION), caretIcon);

        additionalFields.setVisible(true);

        if (visible) {
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
            rotateCaret.setByAngle(180);
            animation.setInterpolator(Interpolator.EASE_IN);
        }
        else {
            fadeTransition.setFromValue(1);
            fadeTransition.setToValue(0);
            rotateCaret.setByAngle(-180);
            animation.setInterpolator(Interpolator.EASE_OUT);
        }

        // Parallel Animation
        animation.getChildren().addAll(rotateCaret,fadeTransition);
        animation.setOnFinished(e->{
            additionalFields.setVisible(visible);
            buttonAddMore.setUserData(visible);
        });
        animation.play();
    }
}
