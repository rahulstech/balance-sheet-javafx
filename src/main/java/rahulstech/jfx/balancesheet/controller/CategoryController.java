package rahulstech.jfx.balancesheet.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import rahulstech.jfx.balancesheet.BalancesheetApp;
import rahulstech.jfx.balancesheet.concurrent.TaskUtils;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.util.DialogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Future;

public class CategoryController extends Controller {

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
            ListCell<Category> cell = new ListCell<>() {
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
                    t-> t.getException().printStackTrace());
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
                t -> t.getException().printStackTrace());
        queryTask = getApp().getAppExecutor().submit(task);
    }

    private void setCategories(List<Category> value) {
        ObservableList<Category> categories = FXCollections.observableList(value);
        categoryList.setItems(categories);
    }

    private ContextMenu createContextMenu(Category item) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(event -> handleEditCategory(item));

        MenuItem deleteItem = new MenuItem("Delete");
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
                categoryList.getItems().add(category);
                    },
                    task -> {
                        task.getException().printStackTrace();
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
                        DialogUtil.alertError(getWindow(),"Error","Fail to delete category. Please try again.");
                                System.err.println(t.getException());
                            });
                    getApp().getAppExecutor().
                            execute(task);
                },"Cancel",null);
    }
}
