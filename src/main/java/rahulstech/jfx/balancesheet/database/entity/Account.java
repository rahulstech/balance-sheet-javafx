package rahulstech.jfx.balancesheet.database.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.DbTypeCurrency;

@DatabaseTable(tableName = "accounts")
public class Account {

    // generatedId = true and allowGeneratedIdInsert = true => id == 0 then generate id else use provided id
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private long id;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false, persisterClass = DbTypeCurrency.class)
    private Currency balance;

    // Constructors, getters, and setters

    public Account() {
        // ORMLite requires a no-arg constructor
    }

    public Account(String name, Currency balance) {
        this.name = name;
        this.balance = balance;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Currency getBalance() {
        return balance;
    }

    public void setBalance(Currency balance) {
        this.balance = balance;
    }
}
