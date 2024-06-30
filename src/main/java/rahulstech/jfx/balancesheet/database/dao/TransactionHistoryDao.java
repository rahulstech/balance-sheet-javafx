package rahulstech.jfx.balancesheet.database.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.*;
import com.j256.ormlite.support.ConnectionSource;
import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.database.entity.HistoryCategory;
import rahulstech.jfx.balancesheet.database.entity.TransactionHistory;
import rahulstech.jfx.balancesheet.database.type.TransactionType;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static rahulstech.jfx.balancesheet.database.dao.DaoUtil.callWithoutExceptionHandling;

public class TransactionHistoryDao {

    private final ConnectionSource source;
    private final Dao<TransactionHistory, Long> transactionHistoryDao;
    private final Dao<HistoryCategory, Long> historyCategoriesDao;

    public TransactionHistoryDao(ConnectionSource connectionSource) throws SQLException {
        this.source = connectionSource;
        transactionHistoryDao = DaoManager.createDao(connectionSource, TransactionHistory.class);
        historyCategoriesDao = DaoManager.createDao(connectionSource, HistoryCategory.class);
    }

    public TransactionHistory saveTransaction(TransactionHistory history) {
        return callWithoutExceptionHandling(()->
                TransactionManager.callInTransaction(source,()->{
                    if (1==transactionHistoryDao.createOrUpdate(history).getNumLinesChanged()) {
                        updateHistoryCategories(history);
                        return history;
                    }
                    throw new SQLException("fail to save transaction_history");
                })
        );
    }

    public boolean createMultipleTransactionHistory(List<TransactionHistory> histories) {
        if (null == histories || histories.isEmpty()) {
            return false;
        }
        return callWithoutExceptionHandling(()->histories.size() == transactionHistoryDao.create(histories));
    }

    public boolean deleteTransactionHistory(TransactionHistory transactionHistory) {
        return callWithoutExceptionHandling(()-> TransactionManager.callInTransaction(source,()->{
                if (1==transactionHistoryDao.delete(transactionHistory)){
                    removeHistoryCategoriesForHistory(transactionHistory);
                    return true;
                }
                return false;
            }));
    }

    public TransactionHistory getTransactionHistoryById(long id) {
        return callWithoutExceptionHandling(()->transactionHistoryDao.queryForId(id));
    }

    public List<TransactionHistory> getAllTransactionHistoryByData(HistoryFilterData data) {
        return callWithoutExceptionHandling(()-> {
            List<Account> accounts = data.getAccounts();
            LocalDate startDate = data.getDateStart();
            LocalDate endDate = data.getDateEnd();
            TransactionType type = data.getType();
            List<Category> categories = data.getCategories();
            int orderBy = data.getOrderBy();
            boolean asc = data.isOrderByAscending();

            QueryBuilder<TransactionHistory,Long> queryBuilder = transactionHistoryDao.queryBuilder();
            Where<TransactionHistory,Long> where = queryBuilder.where();

            if (null != categories && !categories.isEmpty()) {
                QueryBuilder<HistoryCategory,Long> joinQueryBuilder =  historyCategoriesDao.queryBuilder();
                queryBuilder.join(joinQueryBuilder);
                joinQueryBuilder.where().in("category_id",categories);
            }

            where.isNotNull("id");
            if (null!=accounts && !accounts.isEmpty()){
                where.and().in("src_id",accounts).or().in("dest_id",accounts);
            }
            if (null!=startDate) {
                where.and().ge("when",startDate);
            }
            if (null!=endDate) {
                where.and().le("when",endDate);
            }
            if (null!=type) {
                where.and().eq("type",type);
            }
            switch (orderBy) {
                case HistoryFilterData.ORDER_BY_AMOUNT:  queryBuilder.orderByRaw(asc ? "CAST(`amount` AS REAL) " : "CAST(`amount` AS REAL) DESC");
                break;
                case HistoryFilterData.ORDER_BY_WHEN: queryBuilder.orderBy("when",asc);
            }
            PreparedQuery<TransactionHistory> query = queryBuilder.prepare();
            return  transactionHistoryDao.query(query);
        });
    }

    private void removeHistoryCategoriesForHistory(TransactionHistory history) throws SQLException {
        long id = history.getId();
        DeleteBuilder<HistoryCategory,Long> deleteBuilder = historyCategoriesDao.deleteBuilder();
        deleteBuilder.where().eq("history_id",id);
        historyCategoriesDao.delete( deleteBuilder.prepare());
    }

    private void updateHistoryCategories(TransactionHistory transactionHistory) throws SQLException {
        // Clear existing entries
        removeHistoryCategoriesForHistory(transactionHistory);

        // insert the new etries
        Collection<HistoryCategory> entries = transactionHistory.getHistoryCategories();
        if (null != entries) {
            if (entries.size()!=historyCategoriesDao.create(entries)){
                throw new SQLException("unable to add new history category map");
            }
        }
    }
    // Add more methods as needed
}
