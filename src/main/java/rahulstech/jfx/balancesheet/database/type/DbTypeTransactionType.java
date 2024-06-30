package rahulstech.jfx.balancesheet.database.type;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.EnumStringType;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.field.FieldType;

import java.sql.SQLException;

@SuppressWarnings("unused")
public class DbTypeTransactionType extends EnumStringType {

    private static final DbTypeTransactionType INSTANCE = new DbTypeTransactionType();

    private DbTypeTransactionType() {
        super(SqlType.STRING, new Class<?>[] { TransactionType.class });
    }

    public static DbTypeTransactionType getSingleton() {
        return INSTANCE;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        return javaObject == null ? null : ((TransactionType) javaObject).name();
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        return sqlArg == null ? null : TransactionType.valueOf((String) sqlArg);
    }

    @Override
    public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        String value = results.getString(columnPos);
        return value == null ? null : TransactionType.valueOf(value);
    }
}
