package rahulstech.jfx.balancesheet.database.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.DbTypeCurrency;
import rahulstech.jfx.balancesheet.database.type.DbTypeLocalDate;

import java.time.LocalDate;

@SuppressWarnings("ALL")
@DatabaseTable(tableName = "budgets")
public class Budget {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(foreign = true, canBeNull = false, columnName = "category_id", foreignAutoRefresh = true)
    private Category category;

    @DatabaseField(canBeNull = false, columnName = "start_date", persisterClass = DbTypeLocalDate.class)
    private LocalDate startDate;

    @DatabaseField(canBeNull = false, columnName = "end_date", persisterClass = DbTypeLocalDate.class)
    private LocalDate endDate;

    @DatabaseField
    private String description;

    @DatabaseField(canBeNull = false, persisterClass = DbTypeCurrency.class)
    private Currency amount;

    public Budget() {
        // ORMLite needs a no-arg constructor
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Currency getAmount() {
        return amount;
    }

    public void setAmount(Currency amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Budget{" +
                "id=" + id +
                ", category=" + category +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                '}';
    }
}
