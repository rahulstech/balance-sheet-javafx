package rahulstech.jfx.balancesheet.database.dao;

import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.database.type.TransactionType;
import java.time.LocalDate;
import java.util.List;

@SuppressWarnings("ALL")
public class HistoryFilterData {

    public static final int ORDER_BY_AMOUNT = 1;

    public static final int ORDER_BY_WHEN = 2;

    private List<Account> accounts;
    private LocalDate dateStart;
    private LocalDate dateEnd;
    private TransactionType type;
    private List<Category> categories;
    private int orderBy = ORDER_BY_WHEN;

    public boolean isOrderByAscending() {
        return orderByAscending;
    }

    public HistoryFilterData setOrderByAscending(boolean orderByAscending) {
        this.orderByAscending = orderByAscending;
        return this;
    }

    private boolean orderByAscending = false;

    // Getter and setter for accounts
    public List<Account> getAccounts() {
        return accounts;
    }

    public HistoryFilterData setAccounts(List<Account> accounts) {
        this.accounts = accounts;
        return this;
    }

    // Getter and setter for dateStart
    public LocalDate getDateStart() {
        return dateStart;
    }

    public HistoryFilterData setDateStart(LocalDate dateStart) {
        this.dateStart = dateStart;
        return this;
    }

    // Getter and setter for dateEnd
    public LocalDate getDateEnd() {
        return dateEnd;
    }

    public HistoryFilterData setDateEnd(LocalDate dateEnd) {
        this.dateEnd = dateEnd;
        return this;
    }

    // Getter and setter for type
    public TransactionType getType() {
        return type;
    }

    public HistoryFilterData setType(TransactionType type) {
        this.type = type;
        return this;
    }

    // Getter and setter for categories
    public List<Category> getCategories() {
        return categories;
    }

    public HistoryFilterData setCategories(List<Category> categories) {
        this.categories = categories;
        return this;
    }

    public HistoryFilterData setOrderBy(int orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public int getOrderBy() {
        return orderBy;
    }
}
