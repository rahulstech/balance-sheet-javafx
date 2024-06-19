package rahulstech.jfx.balancesheet.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import rahulstech.jfx.balancesheet.database.dao.HistoryFilterData;
import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.database.type.TransactionType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HistoryFilterController extends Controller {

    @FXML
    private Tab tabAccounts;

    @FXML
    private Tab tabCategories;

    @FXML
    private Tab tabMisc;

    private AccountsListController controllerAccounts;

    private CategoryController categoryController;

    private HistoryFilterMiscController controllerMisc;

    private OnFilterListener onFilterListener;

    public void setOnFilterListener(OnFilterListener onFilterListener) {
        this.onFilterListener = onFilterListener;
    }

    @Override
    protected void onInitialize(ResourceBundle res) {
        addTabs();
    }

    private void addTabs() {
        Controller accounts = loadTabView("accounts_list.fxml");
        Controller categories = loadTabView("category.fxml");
        Controller misc = loadTabView("history_filter_misc.fxml");

        tabAccounts.setContent(accounts.getRoot());
        tabCategories.setContent(categories.getRoot());
        tabMisc.setContent(misc.getRoot());

        this.controllerAccounts = (AccountsListController) accounts;
        this.categoryController = (CategoryController) categories;
        this.controllerMisc = (HistoryFilterMiscController) misc;

        this.controllerAccounts.getAccountListView().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.categoryController.getCategoryList().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private Controller loadTabView(String fxml) {
        return getViewLoader().setFxml(fxml).load().getController();
    }

    @FXML
    private void handleReset() {
        setValueAndClose(null);
    }

    @FXML
    private void handleFilter() {
        List<Account> accounts = controllerAccounts.getAccountListView().getSelectionModel().getSelectedItems();
        List<Category> categories = categoryController.getCategoryList().getSelectionModel().getSelectedItems();
        LocalDate startDate = controllerMisc.getStartDate();
        LocalDate endDate = controllerMisc.getEndDate();
        TransactionType type = controllerMisc.getType();
        HistoryFilterMiscController.OrderBy orderBy = controllerMisc.getOrderBy();

        HistoryFilterData newData = new HistoryFilterData();
        if (null != accounts && !accounts.isEmpty()) newData.setAccounts(new ArrayList<>(accounts));
        if (null != categories && !categories.isEmpty()) newData.setCategories(new ArrayList<>(categories));
        newData.setDateStart(startDate);
        newData.setDateEnd(endDate);
        newData.setType(type);
        if (null != orderBy) {
            if (orderBy == HistoryFilterMiscController.OrderBy.WHEN_ASC) {
                newData.setOrderBy(HistoryFilterData.ORDER_BY_WHEN);
                newData.setOrderByAscending(true);
            }
            else if (orderBy == HistoryFilterMiscController.OrderBy.AMOUNT_ASC) {
                newData.setOrderBy(HistoryFilterData.ORDER_BY_AMOUNT);
                newData.setOrderByAscending(true);
            }
            if (orderBy == HistoryFilterMiscController.OrderBy.WHEN_DESC) {
                newData.setOrderBy(HistoryFilterData.ORDER_BY_WHEN);
                newData.setOrderByAscending(false);
            }
            else if (orderBy == HistoryFilterMiscController.OrderBy.AMOUNT_DESC) {
                newData.setOrderBy(HistoryFilterData.ORDER_BY_AMOUNT);
                newData.setOrderByAscending(false);
            }
        }
        setValueAndClose(newData);
    }

    private void setValueAndClose(final HistoryFilterData data) {
        Platform.runLater(()->{
            if (null != onFilterListener) {
                onFilterListener.filter(data);
            }
        });
        getWindow().close();
    }

    public interface OnFilterListener {
        void filter(HistoryFilterData data);
    }
}
