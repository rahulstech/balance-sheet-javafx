package rahulstech.jfx.balancesheet.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;
import rahulstech.jfx.balancesheet.database.dao.BudgetFilterData;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.util.ViewLoader;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@SuppressWarnings("ALL")
public class BudgetFilterController extends Controller {

    private static final String TAG = BudgetFilterController.class.getSimpleName();

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private VBox containerCategory;

    private CategoryController categoryController;

    private OnFilterListener onFilterListener;

    public void setOnFilterListener(OnFilterListener onFilterListener) {
        this.onFilterListener = onFilterListener;
    }

    private void setInitialData(BudgetFilterData data) {

    }

    @Override
    protected void onInitialize(ResourceBundle res) {
        addCategoryListView();
        setStartDate(LocalDate.now());
        setEndDate(LocalDate.now());
    }

    private void setStartDate(LocalDate date) {
        startDatePicker.setValue(date);
    }

    private LocalDate getStartDate() {
        return startDatePicker.getValue();
    }

    private LocalDate getEndDate() {
        return endDatePicker.getValue();
    }

    private void setEndDate(LocalDate date) {
        endDatePicker.setValue(date);
    }

    private void addCategoryListView() {
        ViewLoader loader = getViewLoader()
                .setFxml("category.fxml")
                .load();
        categoryController = loader.getController();
        categoryController.getCategoryList().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        containerCategory.getChildren().add(categoryController.getRoot());
    }

    private List<Category> getSelectedCategories() {
         if (categoryController.getCategoryList().getSelectionModel().isEmpty()) {
             return null;
         }
         return new ArrayList<>(categoryController.getCategoryList().getSelectionModel().getSelectedItems());
    }

    @FXML
    private void handleReset(ActionEvent event) {
        setValueAndClose(null);
    }

    @FXML
    private void handleFilter(ActionEvent event) {
        LocalDate startDate = getStartDate();
        LocalDate endDate = getEndDate();
        List<Category> categories = getSelectedCategories();

        BudgetFilterData data = new BudgetFilterData()
                .setStart(startDate).setEnd(endDate)
                .setCategories(categories);
        setValueAndClose(data);
    }

    private void setValueAndClose(final BudgetFilterData data) {
        Platform.runLater(()->{
            if (null != onFilterListener) {
                onFilterListener.filter(data);
            }
        });
        getWindow().close();
    }

    public interface OnFilterListener {
        void filter(BudgetFilterData data);
    }
}
