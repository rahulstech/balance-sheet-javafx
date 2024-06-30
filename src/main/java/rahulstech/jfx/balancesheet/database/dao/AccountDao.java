package rahulstech.jfx.balancesheet.database.dao;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import rahulstech.jfx.balancesheet.database.entity.Account;

import java.sql.SQLException;
import java.util.List;

import static rahulstech.jfx.balancesheet.database.dao.DaoUtil.callWithoutExceptionHandling;

@SuppressWarnings("ALL")
public class AccountDao extends BaseDaoImpl<Account,Long> {

    public AccountDao(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource,Account.class);
    }

    public Account saveAccount(Account account) {
        return callWithoutExceptionHandling(()->{
            if (1!=createOrUpdate(account).getNumLinesChanged()) {
                throw new SQLException("unable to save account");
            }
            return account;
        });
    }

    @Deprecated
    public boolean createMultipleAccount(List<Account> accounts)  {
        return callWithoutExceptionHandling(()->{
            int count = accounts.size();
            return false;
        });
    }

    public boolean deleteAccounts(List<Account> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return false;
        }
        return callWithoutExceptionHandling(()->{
            delete(accounts);
            return true;
        });
    }

    public List<Account> getAllAccounts() {
        return callWithoutExceptionHandling(()->queryForAll());
    }

    // Add more methods as needed
}
