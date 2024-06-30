package rahulstech.jfx.balancesheet.database.model;

import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.database.type.Currency;

import java.time.YearMonth;

@SuppressWarnings("ALL")
public class MonthlyCategoryModel {

    private YearMonth yearMonth;
    private Currency totalAmount;
    private Category category;

    public MonthlyCategoryModel(YearMonth yearMonth, Currency totalAmount, Category category) {
        this.yearMonth = yearMonth;
        this.totalAmount = totalAmount;
        this.category = category;
    }

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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
