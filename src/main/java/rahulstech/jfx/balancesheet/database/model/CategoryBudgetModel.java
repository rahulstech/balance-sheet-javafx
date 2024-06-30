package rahulstech.jfx.balancesheet.database.model;

import rahulstech.jfx.balancesheet.database.type.Currency;

public class CategoryBudgetModel {

    private String categoryName;

    private Currency categoryTotalAmount;

    private Currency budgetTotalAmount;

    public CategoryBudgetModel() {
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Currency getCategoryTotalAmount() {
        return categoryTotalAmount;
    }

    public void setCategoryTotalAmount(Currency categoryTotalAmount) {
        this.categoryTotalAmount = categoryTotalAmount;
    }

    public Currency getBudgetTotalAmount() {
        return budgetTotalAmount;
    }

    public void setBudgetTotalAmount(Currency budgetTotalAmount) {
        this.budgetTotalAmount = budgetTotalAmount;
    }
}
