package rahulstech.jfx.balancesheet.database.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.types.BigDecimalStringType;
import com.j256.ormlite.table.DatabaseTable;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.DbTypeCurrency;

import java.math.BigDecimal;

import java.math.RoundingMode;

@DatabaseTable(tableName = "derivatives")
public class Derivative {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false, persisterClass = BigDecimalStringType.class)
    private BigDecimal volume;

    @DatabaseField(canBeNull = false, persisterClass = DbTypeCurrency.class)
    private Currency avgBuyPrice;

    @DatabaseField(canBeNull = false, persisterClass = DbTypeCurrency.class)
    private Currency currentUnitPrice;

    @DatabaseField(canBeNull = false, persisterClass = DbTypeCurrency.class)
    private Currency totalRealizedPL = Currency.ZERO;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Account dematAccount;

    @DatabaseField
    private String description;

    // Empty constructor
    public Derivative() {
    }

    // Getters and Setters
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

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume.setScale(4, RoundingMode.HALF_UP);
    }

    public Currency getAvgBuyPrice() {
        return avgBuyPrice;
    }

    public void setAvgBuyPrice(Currency avgBuyPrice) {
        this.avgBuyPrice = avgBuyPrice;
    }

    public Currency getCurrentUnitPrice() {
        return currentUnitPrice;
    }

    public void setCurrentUnitPrice(Currency currentUnitPrice) {
        this.currentUnitPrice = currentUnitPrice;
    }

    public Currency getTotalRealizedPL() {
        return totalRealizedPL;
    }

    public void setTotalRealizedPL(Currency totalRealizedPL) {
        this.totalRealizedPL = totalRealizedPL;
    }

    public Account getDematAccount() {
        return dematAccount;
    }

    public void setDematAccount(Account dematAccount) {
        this.dematAccount = dematAccount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Currency getUnrealizedPL() {
        BigDecimal unrealizedPLValue;
        unrealizedPLValue = currentUnitPrice.getValue().subtract(avgBuyPrice.getValue()).multiply(volume);
        return Currency.from(unrealizedPLValue);
    }

    public Currency getCurrentInvestedValue() {
        return Currency.from(getAvgBuyPrice().getValue().multiply(getVolume()));
    }

    /**
     * @return increase or decrease percentage of current unit price commpared to avg buy price
     */
    public double getNetChange() {
        return currentUnitPrice.subtract(avgBuyPrice).divide(avgBuyPrice).getValue()
                .doubleValue() * 100;
    }

    @Override
    public String toString() {
        return "Derivative{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", volume=" + volume +
                ", avgBuyPrice=" + avgBuyPrice +
                ", currentUnitPrice=" + currentUnitPrice +
                ", totalRealizedPL=" + totalRealizedPL +
                ", dematAccount=" + dematAccount +
                ", description='" + description + '\'' +
                '}';
    }
}
