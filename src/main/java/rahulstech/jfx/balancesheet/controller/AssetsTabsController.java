package rahulstech.jfx.balancesheet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.ResourceBundle;

@SuppressWarnings("ALL")
public class AssetsTabsController extends Controller {

    private static final String TAG = AssetsTabsController.class.getSimpleName();

    @FXML
    private TabPane assetsTabPane;

    @FXML
    private Tab derivatiesTab;

    @FXML
    private Tab accountsTab;

    private AccountsListController accountsListController;

    private DerivativeTableController derivativeTableController;

    @Override
    protected void onInitialize(ResourceBundle res) {
        assetsTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue==accountsTab) {
                loadAccountsTabContent();
            }
            else if (newValue==derivatiesTab) {
                loadDerivativeTabContent();
            }
        });

        windowProperty().addListener((observable, oldValue, newValue) -> {
            if (null!=newValue) {
                newValue.setOnShown(e->{
                    assetsTabPane.getSelectionModel().clearSelection();
                    assetsTabPane.getSelectionModel().select(accountsTab);
                });
            }
        });
    }

    private void loadTabs() {
        loadAccountsTabContent();
        loadDerivativeTabContent();
    }

    private void loadDerivativeTabContent() {
        if (derivativeTableController ==null) {
            derivativeTableController = getViewLoader().setFxml("derivatives_table.fxml").load().getController();
            derivatiesTab.setContent(derivativeTableController.getRoot());
        }
    }

    private void loadAccountsTabContent() {
        if (accountsListController==null) {
            accountsListController = getViewLoader().setFxml("accounts_list.fxml").load().getController();
            accountsTab.setContent(accountsListController.getRoot());
        }
    }
}
