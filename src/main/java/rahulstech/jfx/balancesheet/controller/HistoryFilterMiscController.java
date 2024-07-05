package rahulstech.jfx.balancesheet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import rahulstech.jfx.balancesheet.database.type.TransactionType;
import rahulstech.jfx.balancesheet.util.TextUtil;

import java.time.LocalDate;
import java.util.ResourceBundle;

@SuppressWarnings("ALL")
public class HistoryFilterMiscController extends Controller {

    public enum OrderBy {
        AMOUNT_ASC("Amount Ascending"),
        AMOUNT_DESC("Amount Descending"),
        WHEN_ASC("Date Ascending"),
        WHEN_DESC("Date Descending");

        String value;
        OrderBy(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @FXML
    private ComboBox<OrderBy> orderByComboBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private ComboBox<TransactionType> typeComboBox;

    @Override
    protected void onInitialize(ResourceBundle res) {
        // Add items to the typeComboBox
        typeComboBox.getItems().addAll(TransactionType.values());
        orderByComboBox.getItems().addAll(OrderBy.values());
        startDatePicker.setConverter(TextUtil.getLocalDateStringConverter(TextUtil.DATE_PICK_FORMAT));
        endDatePicker.setConverter(TextUtil.getLocalDateStringConverter(TextUtil.DATE_PICK_FORMAT));
    }

    public void reset() {
        startDatePicker.setValue(LocalDate.now());
        startDatePicker.setConverter(TextUtil.getLocalDateStringConverter(TextUtil.DATE_PICK_FORMAT));
        endDatePicker.setValue(LocalDate.now());
        typeComboBox.getSelectionModel().selectFirst();
        orderByComboBox.getSelectionModel().selectFirst();
    }

    public void setStartDate(LocalDate date) {
        if (null == date) {
            date = LocalDate.now();
        }
        startDatePicker.setValue(date);
    }

    public LocalDate getStartDate() {
        return startDatePicker.getValue();
    }

    public void setEndDate(LocalDate date) {
        if (null == date) {
            date = LocalDate.now();
        }
        endDatePicker.setValue(date);
    }

    public LocalDate getEndDate() {
        return endDatePicker.getValue();
    }

    public void setType(TransactionType type) {
        if (null != type) {
            typeComboBox.getSelectionModel().select(type);
        }
        else {
            typeComboBox.getSelectionModel().selectFirst();
        }
    }

    public TransactionType getType() {
        return typeComboBox.getSelectionModel().getSelectedItem();
    }

    public void setOrderBy(OrderBy order) {
        if (null != order) {
            orderByComboBox.getSelectionModel().select(order);
        }
        else {
            orderByComboBox.getSelectionModel().selectFirst();
        }
    }

    public OrderBy getOrderBy() {
        return orderByComboBox.getSelectionModel().getSelectedItem();
    }

}

