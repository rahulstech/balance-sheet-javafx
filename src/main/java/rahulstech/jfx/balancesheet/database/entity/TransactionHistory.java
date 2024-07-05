package rahulstech.jfx.balancesheet.database.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.TransactionType;
import rahulstech.jfx.balancesheet.database.type.DbTypeCurrency;
import rahulstech.jfx.balancesheet.database.type.DbTypeTransactionType;
import rahulstech.jfx.balancesheet.database.type.DbTypeLocalDate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
@DatabaseTable(tableName = "histories")
public class TransactionHistory {

    @DatabaseField(generatedId = true,allowGeneratedIdInsert = true)
    private long id;

    @DatabaseField(canBeNull = false, persisterClass = DbTypeLocalDate.class)
    private LocalDate when;

    @DatabaseField(canBeNull = false, persisterClass = DbTypeCurrency.class)
    private Currency amount;

    @DatabaseField(foreign = true, columnName = "src_id", foreignAutoRefresh = true)
    private Account src;

    @DatabaseField(foreign = true, columnName = "dest_id", foreignAutoRefresh = true)
    private Account dest;

    @DatabaseField(canBeNull = false, persisterClass = DbTypeTransactionType.class)
    private TransactionType type;

    @DatabaseField
    private String description;

    @DatabaseField(persisterClass = DbTypeCurrency.class)
    private Currency tax;

    @DatabaseField
    private boolean taxSrc;

    @ForeignCollectionField()
    private Collection<HistoryCategory> historyCategories;

    // Other constructors, getters, and setters...

    // Method to get categories
    public List<Category> getCategories() {
        if (historyCategories == null) {
            return Collections.emptyList();
        }
        return historyCategories.stream().map(HistoryCategory::getCategory).collect(Collectors.toList());
    }

    // Default constructor is needed by ORMLite
    public TransactionHistory() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getWhen() {
        return when;
    }

    public void setWhen(LocalDate when) {
        this.when = when;
    }

    public Currency getAmount() {
        return amount;
    }

    public void setAmount(Currency amount) {
        this.amount = amount;
    }

    public Account getSrc() {
        return src;
    }

    public void setSrc(Account src) {
        this.src = src;
    }

    public Account getDest() {
        return dest;
    }

    public void setDest(Account dest) {
        this.dest = dest;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategories(List<Category> categories) {
        ArrayList<HistoryCategory> list = new ArrayList<>();
        for (Category category : categories) {
            HistoryCategory historyCategory = new HistoryCategory();
            historyCategory.setHistory(this);
            historyCategory.setCategory(category);
            list.add(historyCategory);
        }
        this.historyCategories = list;
    }

    public Collection<HistoryCategory> getHistoryCategories() {
        return historyCategories;
    }

    public Currency getTax() {
        return tax;
    }

    public void setTax(Currency tax) {
        this.tax = tax;
    }

    public boolean isTaxSrc() {
        return taxSrc;
    }

    public void setTaxSrc(boolean taxSrc) {
        this.taxSrc = taxSrc;
    }

    @Override
    public String toString() {
        return "TransactionHistory{" +
                "id=" + id +
                ", when=" + when +
                ", amount=" + amount +
                ", src=" + src +
                ", dest=" + dest +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", tax=" + tax +
                ", taxSrc=" + taxSrc +
                ", historyCategories=" + historyCategories +
                '}';
    }
}
