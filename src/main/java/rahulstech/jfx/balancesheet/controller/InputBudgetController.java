package rahulstech.jfx.balancesheet.controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import rahulstech.jfx.balancesheet.concurrent.TaskUtils;
import rahulstech.jfx.balancesheet.database.entity.Budget;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.Log;
import rahulstech.jfx.balancesheet.util.TextUtil;
import rahulstech.jfx.balancesheet.util.ViewLauncher;
import rahulstech.jfx.balancesheet.view.Chip;

import java.time.LocalDate;
import java.util.ResourceBundle;

@SuppressWarnings({"ALL","unchecked"})
public class InputBudgetController extends Controller {

    private static final String TAG = InputBudgetController.class.getSimpleName();

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private AnchorPane pickedCategory;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField amountField;

    @FXML
    private Button chooseCategoryButton;

    private CategoryController categoryController;

    private Budget oldBudget;

    public void setBudget(Budget selected) {
        this.oldBudget = selected;
        startDatePicker.setValue(selected.getStartDate());
        startDatePicker.setConverter(TextUtil.getLocalDateStringConverter(TextUtil.DATE_PICK_FORMAT));
        endDatePicker.setValue(selected.getEndDate());
        endDatePicker.setConverter(TextUtil.getLocalDateStringConverter(TextUtil.DATE_PICK_FORMAT));
        ((TextFormatter<Currency>)amountField.getTextFormatter()).setValue(selected.getAmount());
        descriptionField.setText(selected.getDescription());
        addCategory(selected.getCategory());
    }

    @Override
    public void onInitialize(ResourceBundle resources) {
        // Initialization logic, if needed
        setupNumericTextFormater();

        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());
    }

    private void setupNumericTextFormater() {
        amountField.setTextFormatter(TextUtil.createCurrencyTextFormater());
    }

    @FXML
    private void handleChooseCategory() {
        if (null==categoryController) {
            ViewLauncher launcher = getViewLauncherBuilder()
                    .setFxml("category.fxml")
                    .setTitle("Choose Category")
                    .build();
            launcher.load();
            categoryController = launcher.getController();
            categoryController.getCategoryList().getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            categoryController.getWindow().setOnCloseRequest(e->addCategory(categoryController.getCategoryList().getSelectionModel().getSelectedItem()));
        }
        categoryController.getWindow().show();
    }

    private void addCategory(Category category) {
        pickedCategory.getChildren().clear();
        if (null==category) {
            return;
        }
        Chip chip = new Chip(category.getName());
        chip.setUserData(category);
        pickedCategory.getChildren().add(chip);
    }

    private Category getSelectedCategory() {
        if (pickedCategory.getChildren().isEmpty()) {
            return null;
        }
        Chip chip = (Chip) pickedCategory.getChildren().get(0);
        return (Category) chip.getUserData();
    }

    private LocalDate getStartDate() {
        return startDatePicker.getValue();
    }

    private LocalDate getEndDate() {
        return endDatePicker.getValue();
    }

    @FXML
    private void handleSave(ActionEvent event) {
        LocalDate startDate = getStartDate();
        LocalDate endDate = getEndDate();
        Currency amount = ((TextFormatter<Currency>) amountField.getTextFormatter()).getValue();
        String description = descriptionField.getText();
        Category category = getSelectedCategory();

        if (null==category) {
            DialogUtil.alertError(getWindow(),"Input Error","No category selected");
            return;
        }
        if (!startDate.isBefore(endDate)) {
            DialogUtil.alertError(getWindow(),"Input Error","Start date must be a date before end date");
            return;
        }
        Budget budget = null==oldBudget ? new Budget() : oldBudget;
        budget.setAmount(amount);
        budget.setDescription(description);
        budget.setStartDate(startDate);
        budget.setEndDate(endDate);
        budget.setCategory(category);

        Stage dialog = DialogUtil.showIndeterminateProgressDialog(getWindow(),"Save Progress","Saving Budget. Please Wait.");
        dialog.show();

        Task<Budget> task = TaskUtils.saveBudget(budget,t->{
            dialog.close();
            getWindow().close();
        },t->{
            Log.error(TAG,"save-budget",t.getException());
            dialog.close();
            DialogUtil.alertError(getWindow(),"Save Error","Fail to save budget");
        });
        getApp().getAppExecutor().submit(task);
    }
}
