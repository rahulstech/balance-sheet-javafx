package rahulstech.jfx.balancesheet.database.model;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.TransactionType;

import java.time.YearMonth;

@SuppressWarnings("ALL")
public class MonthlyTypeModel {
    private YearMonth yearMonth;
    private Currency totalAmount;
    private TransactionType type;

    public MonthlyTypeModel(YearMonth yearMonth, Currency totalAmount, TransactionType type) {
        this.yearMonth = yearMonth;
        this.totalAmount = totalAmount;
        this.type = type;
    }

    // Getters and Setters
    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(YearMonth yearMonth) {
        this.yearMonth = yearMonth;
    }

    public Currency getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Currency totalAmount) {
        this.totalAmount = totalAmount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }
}
