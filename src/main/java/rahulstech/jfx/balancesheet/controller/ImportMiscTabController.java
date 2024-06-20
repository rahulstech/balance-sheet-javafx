package rahulstech.jfx.balancesheet.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.FlowPane;
import rahulstech.jfx.balancesheet.view.Chip;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class ImportMiscTabController extends Controller {

    public static enum ImportOptions {
        Accounts,
        Transfers,
        Credit,
        Debit,
    }

    @FXML
    private FlowPane importOptionsFlowPane;

    @FXML
    private  DatePicker startDatePicker;

    @FXML
    private  DatePicker endDatePicker;

    @Override
    protected void onInitialize(ResourceBundle res) {
        resetImportOptions();
    }

    @FXML
    private void handleResetButtonClick(ActionEvent event) {
        resetImportOptions();
    }

    private void resetImportOptions() {
        importOptionsFlowPane.getChildren().clear();
        for (ImportOptions op : ImportOptions.values()) {
            Chip chip = new Chip(op.name());
            chip.setUserData(op);
            importOptionsFlowPane.getChildren().add(chip);
        }
    }

    public LocalDate getStartDate() {
        return startDatePicker.getValue();
    }

    public LocalDate getEndDate() {
        return endDatePicker.getValue();
    }

    public List<ImportOptions> getSelectedImportOptions() {
        ArrayList<ImportOptions> selections = new ArrayList<>();
        for (Node child : importOptionsFlowPane.getChildren()) {
            if (child instanceof Chip) {
                selections.add((ImportOptions) child.getUserData());
            }
        }
        return selections;
    }
}
