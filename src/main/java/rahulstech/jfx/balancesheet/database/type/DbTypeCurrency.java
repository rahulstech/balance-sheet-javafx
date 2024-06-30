package rahulstech.jfx.balancesheet.database.type;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;

@SuppressWarnings("ALL")
public class DbTypeCurrency extends BaseDataType {

    private static final DbTypeCurrency singleTon = new DbTypeCurrency();

    public static DbTypeCurrency getSingleton() {
        return singleTon;
    }

    private DbTypeCurrency() {
        super(SqlType.STRING, new Class<?>[]{Currency.class});
    }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        return Currency.from(defaultStr);
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        return javaObject == null ? null : javaObject.toString();
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        return Currency.from((String) sqlArg);
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getString(columnPos);
    }
}
