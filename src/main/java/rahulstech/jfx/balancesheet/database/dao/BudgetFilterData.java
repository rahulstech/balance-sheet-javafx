package rahulstech.jfx.balancesheet.database.dao;

import rahulstech.jfx.balancesheet.database.entity.Category;

import java.time.LocalDate;
import java.util.List;

public class BudgetFilterData {
    private LocalDate start;
    private LocalDate end;
    private List<Category> categories;

    // Constructor
    public BudgetFilterData() {}

    // Getters
    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public List<Category> getCategories() {
        return categories;
    }

    // Setters with method chaining
    public BudgetFilterData setStart(LocalDate start) {
        this.start = start;
        return this;
    }

    public BudgetFilterData setEnd(LocalDate end) {
        this.end = end;
        return this;
    }

    public BudgetFilterData setCategories(List<Category> categories) {
        this.categories = categories;
        return this;
    }

    @Override
    public String toString() {
        return "BudgetFilterData{" +
                "start=" + start +
                ", end=" + end +
                ", categories=" + categories +
                '}';
    }
}

