package rahulstech.jfx.balancesheet.database.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.types.BigDecimalStringType;
import com.j256.ormlite.table.DatabaseTable;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.DbTypeCurrency;
import rahulstech.jfx.balancesheet.database.type.DbTypeLocalDate;
import rahulstech.jfx.balancesheet.database.type.DerivativeTType;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.math.RoundingMode;

@DatabaseTable(tableName = "derivative_transactions")
public class DerivativeTransaction {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false, persisterClass = DbTypeLocalDate.class)
    private LocalDate when;

    @DatabaseField(canBeNull = false, persisterClass = BigDecimalStringType.class)
    private BigDecimal volume;

    @DatabaseField(canBeNull = false, persisterClass = DbTypeCurrency.class)
    private Currency price;

    @DatabaseField(canBeNull = false)
    private DerivativeTType type;

    @DatabaseField(persisterClass = DbTypeCurrency.class)
    private Currency tax = Currency.ZERO;

    @DatabaseField
    private String description;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Derivative derivative;

    @DatabaseField(canBeNull = false, foreign = true)
    private TransactionHistory history;

    // Empty constructor
    public DerivativeTransaction() {
    }

    // Getters and Setters
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

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume.setScale(4, RoundingMode.HALF_UP);
    }

    public Currency getPrice() {
        return price;
    }

    public void setPrice(Currency price) {
        this.price = price;
    }

    public DerivativeTType getType() {
        return type;
    }

    public void setType(DerivativeTType type) {
        this.type = type;
    }

    public Currency getTax() {
        return tax;
    }

    public void setTax(Currency tax) {
        this.tax = tax;
    }

    public Currency getValueWithOutTax() {
        BigDecimal value = volume.multiply(price.getValue());
        return Currency.from(value);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Derivative getDerivative() {
        return derivative;
    }

    public void setDerivative(Derivative derivative) {
        this.derivative = derivative;
    }

    public TransactionHistory getHistory() {
        return history;
    }

    public void setHistory(TransactionHistory history) {
        this.history = history;
    }

    public Currency getValue() {
        return Currency.from(price.getValue().multiply(volume));
    }

    @Override
    public String toString() {
        return "DerivativeTransaction{" +
                "id=" + id +
                ", when=" + when +
                ", volume=" + volume +
                ", price=" + price +
                ", type=" + type +
                ", tax=" + tax +
                ", description='" + description + '\'' +
                ", derivative=" + derivative +
                ", history=" + history +
                '}';
    }
}
