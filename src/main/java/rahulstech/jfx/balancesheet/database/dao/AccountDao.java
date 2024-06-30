package rahulstech.jfx.balancesheet.database.dao;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import rahulstech.jfx.balancesheet.database.entity.Account;

import java.sql.SQLException;
import java.util.List;

import static rahulstech.jfx.balancesheet.database.dao.DaoUtil.callWithoutExceptionHandling;

@SuppressWarnings("ALL")
public class AccountDao {

    private Dao<Account, Long> accountDao;

    public AccountDao(ConnectionSource connectionSource) throws SQLException {
        accountDao = DaoManager.createDao(connectionSource, Account.class);
    }

    public boolean createAccount(Account account) {
        return callWithoutExceptionHandling(()->1==accountDao.create(account));
    }

    public Account saveAccount(Account account) {
        return callWithoutExceptionHandling(()->{
            if (1!=accountDao.createOrUpdate(account).getNumLinesChanged()) {
                throw new SQLException("unable to save account");
            }
            return account;
        });
    }

    public boolean createMultipleAccount(List<Account> accounts)  {
        return callWithoutExceptionHandling(()->{
            int count = accounts.size();
            return count == accountDao.create(accounts);
        });
    }

    public boolean updateAccount(Account account) {
        return callWithoutExceptionHandling(()->1==accountDao.update(account));
    }

    public boolean deleteAccounts(List<Account> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return false;
        }
        return callWithoutExceptionHandling(()->accounts.size()==accountDao.delete(accounts));
    }

    public Account getAccountById(long id) {
        return callWithoutExceptionHandling(()->accountDao.queryForId(id));
    }

    public List<Account> getAllAccounts() {
        return callWithoutExceptionHandling(()->accountDao.queryForAll());
    }

    // Add more methods as needed
}
