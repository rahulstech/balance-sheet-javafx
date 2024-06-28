package rahulstech.jfx.balancesheet.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.kordamp.ikonli.javafx.FontIcon;
import rahulstech.jfx.balancesheet.concurrent.TaskUtils;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.util.DialogUtil;
import rahulstech.jfx.balancesheet.util.Log;

import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
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

    public ListView<Category> getCategoryList() {
        return categoryList;
    }

    @Override
    protected void onInitialize(ResourceBundle res) {
        categoryList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        categoryList.setCellFactory(listView -> {
            ListCell<Category> cell = new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setContextMenu(null);
                    } else {
                        setText(item.getName());
                        setContextMenu(createContextMenu(item));
                    }
                }
            };
            return cell;
        });
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
        ObservableList<Category> categories = FXCollections.observableList(value);
        categoryList.setItems(categories);
    }

    private ContextMenu createContextMenu(Category item) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("Edit");
        editItem.setGraphic(new FontIcon("mdi-pencil"));
        editItem.setOnAction(event -> handleEditCategory(item));

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setGraphic(new FontIcon("mdi-delete"));
        deleteItem.setOnAction(event -> handleDeleteCategory(item));

        contextMenu.getItems().addAll(editItem, deleteItem);
        return contextMenu;
    }

    @FXML
    private void handleAddCategory() {
        Category oldCategory = (Category) nameField.getUserData();
        String name = nameField.getText();
        nameField.setUserData(null);
        if (name != null && !name.trim().isEmpty()) {
            Category newCategory = null == oldCategory ? new Category() : oldCategory;
            newCategory.setName(name);
            nameField.clear();
            Task<Category> createTask = TaskUtils.saveCategory(newCategory,
                    task -> {
                Category category = task.getValue();
                int index = categoryList.getItems().indexOf(category);
                if (index>=0) {
                    categoryList.getItems().set(index,category);
                }
                else {
                    categoryList.getItems().add(category);
                }
                    },
                    task -> {
                        Log.error(TAG,"add-category", task.getException());
                        DialogUtil.alertError(getWindow(),"Save Error","Category not create");
                    });
            getApp().getAppExecutor().execute(createTask);
        }
    }

    @FXML
    private void handleRefresh() {
        loadCategories();
    }

    private void handleEditCategory(Category category) {
        nameField.setText(category.getName());
        nameField.setUserData(category);
    }

    private void handleDeleteCategory(Category category) {
        DialogUtil.alertConfirmation(getWindow(),
                "Warning","Selected category will be deleted permanently. Are you sure to proceed?",
                "Delete",()->{
                    Task<Boolean> task = TaskUtils.deleteCategories(Collections.singletonList(category),
                            t->{
                        categoryList.getItems().remove(category);
                            },t->{
                                Log.error(TAG,"delete-category", t.getException());
                                DialogUtil.alertError(getWindow(),"Error","Fail to delete category. Please try again.");
                            });
                    getApp().getAppExecutor().
                            execute(task);
                },"Cancel",null);
    }
}
