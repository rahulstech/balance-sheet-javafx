package rahulstech.jfx.balancesheet.database.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import rahulstech.jfx.balancesheet.database.entity.DerivativeTransaction;

import java.sql.SQLException;

public class DerivativeTransactionDao extends BaseDaoImpl<DerivativeTransaction,Long> {

    public DerivativeTransactionDao(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, DerivativeTransaction.class);
    }
}
