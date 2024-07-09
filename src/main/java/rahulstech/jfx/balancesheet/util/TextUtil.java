package rahulstech.jfx.balancesheet.util;

import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;
import rahulstech.jfx.balancesheet.database.type.Currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.UnaryOperator;

@SuppressWarnings("ALL")
public class TextUtil {

    private static final String TAG = TextUtil.class.getSimpleName();

    private static final Locale LOCALE_INDIAN_ENGLISH = new Locale("en","IN");

    public static final DateTimeFormatter DATE_PICK_FORMAT = DateTimeFormatter.ofPattern("d/M/yyyy");

    public static TextFormatter<Currency> createCurrencyTextFormater() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            if (text.isEmpty()) {
                return change;
            }
            try {
                Currency.from(text);
                return change;
            }
            catch (Exception ignored) {
                return null;
            }
        };
        StringConverter<Currency> converter = getCurrencyStringConverter();
        return new TextFormatter<>(converter, Currency.ZERO, filter);
    }

    public static TextFormatter<BigDecimal> createBigDecimalTextFormater(int roundUpTo) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            if (text.isEmpty()) {
                return change;
            }
            try {
                new BigDecimal(text);
                return change;
            }
            catch (Exception ignored) {
                return null;
            }
        };
        StringConverter<BigDecimal> converter = getBigDecimalStringConverter(roundUpTo);
        return new TextFormatter<>(converter, BigDecimal.ZERO, filter);
    }

    public static StringConverter<Currency> getCurrencyStringConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Currency object) {
                return object==null ? "0.00" : object.toString();
            }

            @Override
            public Currency fromString(String string) {
                if (null==string || string.isEmpty()) {
                    return Currency.ZERO;
                }
                try {
                    return Currency.from(string);
                }
                catch (NumberFormatException ex) {
                    return Currency.ZERO;
                }
            }
        };
    }

    public static StringConverter<BigDecimal> getBigDecimalStringConverter(int round) {
        return new StringConverter<>() {
            @Override
            public String toString(BigDecimal object) {
                if (null==object) {
                    return "0";
                }
                return object.setScale(round,RoundingMode.HALF_UP).toString();
            }

            @Override
            public BigDecimal fromString(String string) {
                if (null==string || string.isEmpty()) {
                    return BigDecimal.ZERO.setScale(round,RoundingMode.HALF_UP);
                }
                try {
                    return new BigDecimal(string).setScale(round, RoundingMode.HALF_UP);
                }
                catch (NumberFormatException ex) {
                    return BigDecimal.ZERO.setScale(round,RoundingMode.HALF_UP);
                }
            }
        };
    }

    public static StringConverter<LocalDate> getLocalDateStringConverter(DateTimeFormatter formater) {
        return new StringConverter<>() {
            final DateTimeFormatter FORMATER = null == formater ? DateTimeFormatter.ISO_LOCAL_DATE : formater;

            @Override
            public String toString(LocalDate object) {
                return null == object ? "" : object.format(FORMATER);
            }

            @Override
            public LocalDate fromString(String string) {
                return null==string || string.isEmpty() ? null : LocalDate.parse(string,FORMATER);
            }
        };
    }

    public static String prettyPrintCurrency(Currency currency) {
        if (currency==null) {
            return "";
        }
        return prettyPrintCurrency(currency.getValue());
    }

    public static String prettyPrintCurrency(Number price) {
        if (price==null) {
            return "";
        }
        DecimalFormat formater = (DecimalFormat) NumberFormat.getCurrencyInstance(LOCALE_INDIAN_ENGLISH);
        String currencySymbol = formater.getCurrency().getSymbol();
        formater.setNegativePrefix(currencySymbol+"-");
        return formater.format(price);
    }

    public static String prettyPrintPercentageChange(Number number) {
        if (number==null) {
            return "";
        }
        float value = number.floatValue();
        String prefix = value <= 0 ? "" : "+";
        return prefix+String.format("%.02f%%",value);
    }

    public static void setValueBasedTextStyleClass(Number number, Node node) {
        BigDecimal value = number instanceof BigDecimal ? (BigDecimal) number : new BigDecimal(number.toString());
        int compare = value.compareTo(BigDecimal.ZERO);
        node.getStyleClass().removeAll("text-debit","text-credit","text-body");
        if (compare<0) {
            node.getStyleClass().add("text-debit");
        }
        else if (compare>0) {
            node.getStyleClass().add("text-credit");
        }
        else {
            node.getStyleClass().add("text-title-large");
        }
    }

    public static void setValueBasedTextStyleClass(Number number, Cell<?> cell) {
        BigDecimal value = number instanceof BigDecimal ? (BigDecimal) number : new BigDecimal(number.toString());
        int compare = value.compareTo(BigDecimal.ZERO);
        cell.getStyleClass().removeAll("text-debit","text-credit","text-body");
        if (compare<0) {
            cell.getStyleClass().add("text-debit");
        }
        else if (compare>0) {
            cell.getStyleClass().add("text-credit");
        }
        else {
            cell.getStyleClass().add("text-body");
        }
    }
}
