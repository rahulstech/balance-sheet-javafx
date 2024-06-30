package rahulstech.jfx.balancesheet.database.entity;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@SuppressWarnings("ALL")
@DatabaseTable(tableName = "history_categories")
public class HistoryCategory {

    @DatabaseField(foreign = true, canBeNull = false, columnName = "history_id", foreignAutoRefresh = true, uniqueCombo = true)
    private TransactionHistory history;

    @DatabaseField(foreign = true, canBeNull = false, columnName = "category_id", foreignAutoRefresh = true, uniqueCombo = true)
    private Category category;

    public HistoryCategory() {}

    public HistoryCategory(TransactionHistory history, Category category) {
        this.history = history;
        this.category = category;
    }

    // Getters and Setters
    public TransactionHistory getHistory() {
        return history;
    }

    public void setHistory(TransactionHistory history) {
        this.history = history;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
