package rahulstech.jfx.balancesheet.database.model;

import rahulstech.jfx.balancesheet.database.entity.Derivative;
import rahulstech.jfx.balancesheet.database.type.Currency;

@SuppressWarnings("ALL")
public class DerivativeReportModel {

    private Derivative derivative;

    private Currency totalInvestedValue = Currency.ZERO;

    private Currency totalTax = Currency.ZERO;

    private Currency totalRealizedPL = Currency.ZERO;

    private Currency totalUnrealizedPL = Currency.ZERO;

    public DerivativeReportModel() {
    }

    public Derivative getDerivative() {
        return derivative;
    }

    public void setDerivative(Derivative derivative) {
        this.derivative = derivative;
    }

    public Currency getTotalInvestedValue() {
        return totalInvestedValue;
    }

    public void setTotalInvestedValue(Currency totalInvestedValue) {
        this.totalInvestedValue = totalInvestedValue;
    }

    public Currency getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(Currency totalTax) {
        this.totalTax = totalTax;
    }

    public Currency getTotalRealizedPL() {
        return totalRealizedPL;
    }

    public void setTotalRealizedPL(Currency totalRealizedPL) {
        this.totalRealizedPL = totalRealizedPL;
    }

    public Currency getTotalUnrealizedPL() {
        return totalUnrealizedPL;
    }

    public void setTotalUnrealizedPL(Currency totalUnrealizedPL) {
        this.totalUnrealizedPL = totalUnrealizedPL;
    }

    public Currency getProfitAfterTax() {
        return totalRealizedPL.subtract(totalTax);
    }

    public double getPercentageChange() {
        if (Currency.ZERO.equals(totalInvestedValue)) {
            return 0.00;
        }
        return totalUnrealizedPL.divide(totalInvestedValue).getValue().doubleValue()*100;
    }

    @Override
    public String toString() {
        return "DerivativeReportModel{" +
                "derivative=" + derivative +
                ", totalInvestedValue=" + totalInvestedValue +
                ", totalTax=" + totalTax +
                ", totalRealizedPL=" + totalRealizedPL +
                ", totalUnrealizedPL=" + totalUnrealizedPL +
                '}';
    }
}
