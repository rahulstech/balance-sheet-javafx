package rahulstech.jfx.balancesheet.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.FlowPane;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.util.ViewLauncher;
import rahulstech.jfx.balancesheet.view.Chip;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class BaseCategoryChartController extends Controller {

    @FXML
    private FlowPane selectedCategoriesPanel;

    private CategoryController categoryController;

    public BaseCategoryChartController() {
    }

    @FXML
    private void handleAddCategoryButtonClick() {
        if (null==categoryController) {
            ViewLauncher launcher = getViewLauncherBuilder()
                    .setFxml("category.fxml")
                    .setTitle("Choose Category")
                    .build().load();
            categoryController = launcher.getController();
            categoryController.getCategoryList().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            categoryController.getWindow().setOnCloseRequest(e->{
                addCategories(categoryController.getCategoryList().getSelectionModel().getSelectedItems());
            });
        }
        categoryController.getWindow().show();
    }

    public void addCategories(List<Category> categories) {
        // issue #2 solved: old selections not cleared on fresh new selections
        selectedCategoriesPanel.getChildren().clear();
        for (Category category : categories) {
            Chip chip = new Chip(category.getName());
            chip.setUserData(category);
            selectedCategoriesPanel.getChildren().add(chip);
        }
    }

    public List<Category> getSelectedCategories() {
        List<Category> categories = new ArrayList<>();
        for (Node child : selectedCategoriesPanel.getChildren()) {
            if (child instanceof Chip) {
                categories.add((Category) child.getUserData());
            }
        }
        return categories;
    }

    @FXML
    private void handleClearCategoriesButtonClick(ActionEvent event) {
        categoryController.getCategoryList().getSelectionModel().clearSelection();
        selectedCategoriesPanel.getChildren().clear();
    }
}
