package rahulstech.jfx.balancesheet.database.type;

import java.math.BigDecimal;
import java.math.RoundingMode;

@SuppressWarnings("unused")
public class Currency implements Comparable<Currency> {

    public static final Currency ZERO = new Currency(BigDecimal.ZERO);

    private final BigDecimal value;

    // Private constructor
    private Currency(BigDecimal value) {
        this.value = value.setScale(2, RoundingMode.HALF_UP);
    }

    // Static factory methods for creating Currency objects
    public static Currency from(Number number) {
        BigDecimal value = new BigDecimal(number.toString());
        return value.compareTo(BigDecimal.ZERO) == 0 ? ZERO : new Currency(value);
    }

    public static Currency from(String number) {
        BigDecimal value = new BigDecimal(number);
        return value.compareTo(BigDecimal.ZERO) == 0 ? ZERO : new Currency(value);
    }

    public static Currency from(double number) {
        return 0 == number ? ZERO : new Currency(new BigDecimal(number));
    }

    public BigDecimal getValue() {
        return value;
    }

    // Method to add two Currency objects
    public Currency add(Currency other) {
        BigDecimal result = this.value.add(other.value);
        return result.compareTo(BigDecimal.ZERO) == 0 ? ZERO : new Currency(result);
    }

    // Method to subtract one Currency object from another
    public Currency subtract(Currency other) {
        BigDecimal result = this.value.subtract(other.value);
        return result.compareTo(BigDecimal.ZERO) == 0 ? ZERO : new Currency(result);
    }

    // Method to multiply two Currency objects
    public Currency multiply(Currency other) {
        BigDecimal result = this.value.multiply(other.value);
        return result.compareTo(BigDecimal.ZERO) == 0 ? ZERO : new Currency(result);
    }

    // Method to divide one Currency object by another
    public Currency divide(Currency other) {
        BigDecimal result = this.value.divide(other.value, 2, RoundingMode.HALF_UP);
        return result.compareTo(BigDecimal.ZERO) == 0 ? ZERO : new Currency(result);
    }

    public Currency negate() {
        return multiply(Currency.from("-1"));
    }


    @Override
    public int compareTo(Currency other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
