package rahulstech.jfx.balancesheet.database.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import rahulstech.jfx.balancesheet.database.entity.Derivative;

import java.sql.SQLException;

public class DerivativeDao extends BaseDaoImpl<Derivative,Long> {

    public DerivativeDao(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Derivative.class);
    }
}
