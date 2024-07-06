package rahulstech.jfx.balancesheet.controller;

import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import rahulstech.jfx.balancesheet.concurrent.TaskUtils;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.Log;

import java.util.*;
import java.util.concurrent.Future;

@SuppressWarnings("ALL")
public class CategoryController extends Controller {

    private static final String TAG = CategoryController.class.getSimpleName();

    @FXML
    private TextField nameField;

    @FXML
    private ListView<Category> categoryList;

    private List<Category> categories;

    private Future<?> queryTask;

    private Future<?> filterTask;

    private Map<Long,Category> editedCategories = new HashMap<>();

    public ListView<Category> getCategoryList() {
        return categoryList;
    }

    @Override
    protected void onInitialize(ResourceBundle res) {
        categoryList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//        categoryList.setCellFactory(listView -> new ListCell<>() {
//                @Override
//                protected void updateItem(Category item, boolean empty) {
//                    super.updateItem(item, empty);
//                    if (empty || item == null) {
//                        setText(null);
//                    } else {
//                        setText(item.getName());
//                    }
//                }
//        });
        //categoryList.setCellFactory(listView->TextFieldListCell.forListView());
        //.categoryList.setOnEditCommit(e->handleEditCategoryName(e.));
        categoryList.setCellFactory(listView->new TextFieldListCell<>(new StringConverter<Category>() {
            @Override
            public String toString(Category object) {
                if (null==object) {
                    return null;
                }
                return object.getName();
            }

            @Override
            public Category fromString(String string) {
                if (null==string || string.isEmpty()) {
                    return null;
                }
                // edit commit will use this value to update ListView
                // NOTE: the returned object is not added to the items list
                //      so no need to worry about returning a fake object
                Category category = new Category();
                category.setName(string);
                return category;
            }
        }));
        categoryList.setOnEditCommit(e->handleEditCategory(e.getSource().getItems().get(e.getIndex()),e.getNewValue()));

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filterTask != null) {
                filterTask.cancel(true);  // Cancel the currently running task
            }
            Task<List<Category>> task = TaskUtils.filterCategory(categories,newValue,
                    t->setCategories(t.getValue()),
                    t-> Log.error(TAG,"filter-category",t.getException()));
            filterTask = getApp().getAppExecutor().submit(task);  // Submit the new task
        });
        loadCategories();
    }

    private void loadCategories() {
        if (null != queryTask) {
            queryTask.cancel(true);
        }
        Task<List<Category>> task = TaskUtils.getAllCategories(t -> {
            this.categories = t.getValue();
            setCategories(this.categories);
                },
                t -> Log.error(TAG,"loadCatergories",t.getException()));
        queryTask = getApp().getAppExecutor().submit(task);
    }

    private void setCategories(List<Category> value) {
        categoryList.getItems().clear();
        categoryList.getItems().addAll(value);
    }

    @FXML
    private void handleAddCategory() {
        String name = nameField.getText();
        nameField.clear();
        if (name != null && !name.trim().isEmpty()) {
            Category newCategory = new Category();
            newCategory.setName(name);
            create(newCategory);
        }
    }

    @FXML
    private void handleRefresh() {
        loadCategories();
    }

    @FXML
    private void handleSaveButtonClicked() {
        if (editedCategories.isEmpty()) {
            Log.info(TAG,"no categories edited, nothing to save");
            return;
        }
        update(editedCategories.values());
    }

    @FXML
    private void handleDeleteButtonClicked() {
        if (categoryList.getSelectionModel().getSelectedItems().isEmpty()) {
            return;
        }
        List<Category> categories = categoryList.getSelectionModel().getSelectedItems();
        handleDeleteCategories(categories);
    }

    private void handleEditCategory(Category orinal, Category newValue) {
        orinal.setName(newValue.getName());
        editedCategories.put(orinal.getId(),orinal);
    }

    private void handleDeleteCategories(List<Category> categories) {
        DialogUtil.alertConfirmation(getWindow(),
                "Warning","Selected category(s) will be deleted permanently. Are you sure to proceed?",
                "Delete",()->{
                    Task<Boolean> task = TaskUtils.deleteCategories(categories,
                            t->{
                                categoryList.getItems().removeAll(categories);
                            },t->{
                                Log.error(TAG,"delete-category", t.getException());
                                DialogUtil.alertError(getWindow(),"Error","Fail to delete category. Please try again.");
                            });
                    getApp().getAppExecutor().
                            execute(task);
                },"Cancel",null);
    }

    private void create(Category newCategory) {
        Task<Category> createTask = TaskUtils.saveCategory(newCategory,
                task -> {
                    Category category = task.getValue();
                    categoryList.getItems().add(category);
                },
                task -> {
                    Log.error(TAG,"create", task.getException());
                    DialogUtil.alertError(getWindow(),"Save Error","Category not saved");
                });
        getApp().getAppExecutor().execute(createTask);
    }

    private void update(Collection<Category> categories) {
        Stage dialog = DialogUtil.showIndeterminateProgressDialog(getWindow(),"Save Progress","Saving changes. Please wait");
        dialog.setOnCloseRequest(Event::consume);
        dialog.show();

        Task<Void> updateTask = TaskUtils.updateCategories(categories,
                t->{
            dialog.hide();
            editedCategories.clear();
                },
                t->{
            dialog.hide();
            Log.error(TAG,"udpate", t.getException());
            DialogUtil.alertError(getWindow(),"Save Error","Category(s) not saved");
        });
        getApp().getAppExecutor().execute(updateTask);
    }
}
