package rahulstech.jfx.balancesheet.database.type;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DbTypeLocalDate extends BaseDataType {

    private static final DbTypeLocalDate INSTANCE = new DbTypeLocalDate();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private DbTypeLocalDate() {
        super(SqlType.STRING, new Class<?>[]{LocalDate.class});
    }

    public static DbTypeLocalDate getSingleton() {
        return INSTANCE;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        return javaObject == null ? null : ((LocalDate) javaObject).format(FORMATTER);
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        return sqlArg == null ? null : LocalDate.parse((String) sqlArg, FORMATTER);
    }

    @Override
    public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        String value = results.getString(columnPos);
        return value == null ? null : LocalDate.parse(value, FORMATTER);
    }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) {
        return null==defaultStr || defaultStr.isEmpty() ? null : LocalDate.parse(defaultStr, FORMATTER);
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getString(columnPos);
    }
}
