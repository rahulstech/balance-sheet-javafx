package rahulstech.jfx.balancesheet.database.migration;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import rahulstech.jfx.balancesheet.database.entity.Derivative;
import rahulstech.jfx.balancesheet.database.entity.DerivativeTransaction;

import java.sql.SQLException;

public class MIGRATION_2_3 extends Migration {

    public MIGRATION_2_3() {
        super(2, 3);
    }

    @Override
    public void migrate(ConnectionSource source) throws SQLException {
        TableUtils.createTableIfNotExists(source, Derivative.class);
        TableUtils.createTableIfNotExists(source, DerivativeTransaction.class);
    }
}
