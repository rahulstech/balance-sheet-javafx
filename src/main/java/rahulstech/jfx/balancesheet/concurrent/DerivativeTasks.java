package rahulstech.jfx.balancesheet.concurrent;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.QueryBuilder;
import javafx.concurrent.Task;
import rahulstech.jfx.balancesheet.database.BalancesheetDb;
import rahulstech.jfx.balancesheet.database.dao.DerivativeDao;
import rahulstech.jfx.balancesheet.database.dao.DerivativeTransactionDao;
import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.database.entity.Derivative;
import rahulstech.jfx.balancesheet.database.entity.DerivativeTransaction;
import rahulstech.jfx.balancesheet.database.entity.TransactionHistory;
import rahulstech.jfx.balancesheet.database.model.DerivativeReportModel;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.DerivativeTType;
import rahulstech.jfx.balancesheet.database.type.TransactionType;
import rahulstech.jfx.balancesheet.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("ALL")
public class DerivativeTasks {

    private static final String TAG = DerivativeTasks.class.getSimpleName();

    private DerivativeTasks() {}

    public static Task<Derivative> createDerivative(Derivative derivative, DerivativeTransaction transaction) {
        return new DatabaseTask<>() {
            @Override
            protected Derivative executeInTransaction(BalancesheetDb db) throws Exception {
                return new DerivativeTasks().createDerivative(db,derivative,transaction);
            }
        };
    }

    public static Task<Void> updateDerivatives(Collection<Derivative> derivatives) {
        return new DatabaseTask<>() {
            @Override
            protected Void executeInTransaction(BalancesheetDb db) throws Exception {
                DerivativeTasks tasks = new DerivativeTasks();
                for (Derivative derivative : derivatives) {
                    tasks.updateDerivate(db,derivative);
                }
                return null;
            }
        };
    }

    public static Task<DerivativeTransaction> saveDerivativeTransaction(DerivativeTransaction transaction) {
        return new DatabaseTask<>() {

            @Override
            protected DerivativeTransaction executeInTransaction(BalancesheetDb db) throws Exception {
                DerivativeTasks tasks = new DerivativeTasks();
                boolean isEditing = transaction.getId()!=0;
                DerivativeTransaction oldTransaction;
                if (isEditing) {
                    oldTransaction = db.getDerivativeTransactionsDao().queryForSameId(transaction);
                }
                else {
                    oldTransaction = null;
                }
                // save the derivative transaction
                DerivativeTransaction saved = tasks.saveDerivativeTransaction(db, transaction);
                // save the changes in derivative
                Derivative derivative = saved.getDerivative();
                tasks.updateDerivateOnTrasaction(db,derivative,saved,oldTransaction);
                return saved;
            }
        };
    }

    public static Task<Void> deleteDerivatives(List<Derivative> derivatives) {
        return new DatabaseTask<>() {
            @Override
            protected Void executeInTransaction(BalancesheetDb db) throws Exception {
                DerivativeTasks tasks = new DerivativeTasks();
                for (Derivative derivative : derivatives) {
                    tasks.deleteDerivate(db,derivative);
                }
                return null;
            }
        };
    }

    public static Task<Void> deleteDerivativeTransactions(List<DerivativeTransaction> transactions) {
        return new DatabaseTask<>() {
            @Override
            protected Void executeInTransaction(BalancesheetDb db) throws Exception {
                DerivativeTasks tasks = new DerivativeTasks();
                for (DerivativeTransaction transaction : transactions) {
                    tasks.deleteDerivativeTransaction(db,transaction);
                }
                return null;
            }
        };
    }

    public static Task<List<Derivative>> getAllDerivatives() {
        return new DatabaseTask<>() {
            @Override
            protected List<Derivative> executeInTransaction(BalancesheetDb db) throws Exception {
                return db.getDerivativeDao().queryForAll();
            }
        };
    }

    public static Task<List<DerivativeTransaction>> getAllDerivativesTransactionFor(Derivative derivative) {
        return new DatabaseTask<>() {

            @Override
            protected List<DerivativeTransaction> executeInTransaction(BalancesheetDb db) throws Exception {
                return db.getDerivativeTransactionsDao().queryForEq("derivative_id", derivative.getId());
            }
        };
    }

    public static Task<Derivative> reloadDerivative(Derivative derivative) {
        return new DatabaseTask<>() {
            @Override
            protected Derivative executeInTransaction(BalancesheetDb db) throws Exception {
                return db.getDerivativeDao().queryForSameId(derivative);
            }
        };
    }

    public static Task<DerivativeReportModel> getOverallDervitiveReport() {
        return new DatabaseTask<>() {
            @Override
            protected DerivativeReportModel executeInTransaction(BalancesheetDb db) throws Exception {
                return db.getReportDao().getOverallDerivativeReport();
            }
        };
    }

    Derivative createDerivative(BalancesheetDb db, Derivative derivative, DerivativeTransaction transaction) throws Exception {
        // save the derivate
        db.getDerivativeDao().create(derivative);
        // save the derivative transaction
        saveDerivativeTransaction(db,transaction);
        Log.debug(TAG,"created: "+derivative);
        return derivative;
    }

    DerivativeTransaction saveDerivativeTransaction(BalancesheetDb db, DerivativeTransaction transaction) throws Exception {
        boolean isEditing = transaction.getId()!=0;
        Currency amount = transaction.getValueWithOutTax();
        Derivative derivative = transaction.getDerivative();
        TransactionHistory history;
        if (isEditing) {
            history = db.getTransactionHistoryDao().queryForSameId(transaction.getHistory());
        }
        else {
            history = new TransactionHistory();
            history.setSrc(derivative.getDematAccount());
            history.setTaxSrc(true);

            DerivativeTType derivativeTType = transaction.getType();
            if (derivativeTType==DerivativeTType.BUY) {
                // in case of sell and reward, derivative value is withdrawn from demat a/c
                history.setType(TransactionType.WITHDRAW);
            } else {
                // in case of sell and reward, derivative value is deposited to demat a/c
                history.setType(TransactionType.DEPOSIT);
            }
        }
        history.setWhen(transaction.getWhen());
        history.setAmount(amount);
        history.setTax(transaction.getTax());
        history.setTaxSrc(true);
        history.setDescription(transaction.getDescription());

        // save the transaction history
        TransactionHistory saved = new TransactionHistoryCRUDHelper().save(db,history);
        // save the derivative transaction
        transaction.setHistory(saved);
        db.getDerivativeTransactionsDao().createOrUpdate(transaction);
        Log.debug(TAG,"saved: "+transaction);
        return transaction;
    }

    private void updateDerivateOnTrasaction(BalancesheetDb db, Derivative derivative,
                                            DerivativeTransaction transaction, DerivativeTransaction oldTransaction) throws Exception {
        if (oldTransaction==null) {
            DerivativeTType type = transaction.getType();
            Currency newCurrentUnitPrice = transaction.getPrice();
            Log.debug(TAG,"updateDerivateOnTrasaction: has new transaction of type="+type);
            // update the derivate
            if (type == DerivativeTType.SELL) {
                // if it's sell then reduce volume, change current_unit_price and realized_pl
                // realized_pl_change = (transaction price - avg buy price) * transaction volume
                // new relalized_pl = realized_pl + realized_pl_change
                // Note: realized_pl_change may be negative
                BigDecimal newVolume = derivative.getVolume().subtract(transaction.getVolume());
                Currency realizedPLChange = Currency.from(
                        transaction.getPrice().subtract(derivative.getAvgBuyPrice()).getValue().multiply(transaction.getVolume())
                );
                Currency newTotalRealizedPL = derivative.getTotalRealizedPL().add(realizedPLChange);

                Log.trace(TAG, "updateDerivateOnTrasaction: name=" + derivative.getName() +
                        " odlVolume=" + derivative.getVolume() + " newVolume=" + newVolume +
                        " oldCurrentUnitPrice=" + derivative.getCurrentUnitPrice() + " newCurrentUnitPrice=" + newCurrentUnitPrice +
                        " oldTotalRealizedPL=" + derivative.getTotalRealizedPL() + " realizedPLChange=" + realizedPLChange + " newTotalRealizedPL=" + newTotalRealizedPL);

                derivative.setVolume(newVolume);
                derivative.setTotalRealizedPL(newTotalRealizedPL);
            } else if (type == DerivativeTType.BUY) {
                // if it's buy then add volume, change current_unit_price and avg_buy_price
                BigDecimal newVolume = derivative.getVolume().add(transaction.getVolume());
                Currency newAvgBuyPrice = calculateAverageBuyPrice(db,derivative);

                Log.trace(TAG, "updateDerivateOnTrasaction: name=" + derivative.getName() +
                        " odlVolume=" + derivative.getVolume() + " newVolume=" + newVolume +
                        " oldCurrentUnitPrice=" + derivative.getCurrentUnitPrice() + " newCurrentUnitPrice=" + newCurrentUnitPrice +
                        " oldAvgBuyPrice=" + derivative.getAvgBuyPrice() + " newAvgBuyPrice=" + newAvgBuyPrice);

                derivative.setVolume(newVolume);
                derivative.setAvgBuyPrice(newAvgBuyPrice);
            } else {
                // if it's reward add volume
                BigDecimal newVolume = derivative.getVolume().add(transaction.getVolume());

                Log.trace(TAG, "updateDerivateOnTrasaction: name=" + derivative.getName() +
                        " oldVolume=" + derivative.getVolume() + " newVolume=" + newVolume+
                        " oldCurrentUnitPrice=" + derivative.getCurrentUnitPrice() + " newCurrentUnitPrice=" + newCurrentUnitPrice);

                derivative.setVolume(newVolume);
            }
            derivative.setCurrentUnitPrice(newCurrentUnitPrice);
        }
        else {
            DerivativeTType type = oldTransaction.getType();
            boolean isDelete = null==transaction;
            Currency newCurrentUnitPrice = isDelete ? derivative.getCurrentUnitPrice() :
                    !oldTransaction.getPrice().equals(transaction.getPrice()) ? transaction.getPrice() : derivative.getCurrentUnitPrice();
            Log.debug(TAG,"updateDerivateOnTrasaction: has old transaction of type="+type);
            // update the unit price only if it's updated otherwise keep the old unit price
            if (type==DerivativeTType.SELL) {
                Currency newPL = isDelete ? Currency.ZERO : transaction.getPrice().subtract(derivative.getAvgBuyPrice())
                        .multiply(Currency.from(transaction.getVolume()));
                Currency newTotalRealizedPL = derivative.getTotalRealizedPL()
                        .add(newPL)
                        .subtract(oldTransaction.getPrice().subtract(derivative.getAvgBuyPrice())
                                        .multiply(Currency.from(oldTransaction.getVolume())));
                BigDecimal oldVolume = isDelete ? derivative.getVolume().add(oldTransaction.getVolume())
                        : derivative.getVolume().add(oldTransaction.getVolume()).subtract(transaction.getVolume());

                Log.trace(TAG, "updateDerivateOnTrasaction: name=" + derivative.getName() +
                        " currentVolume=" + derivative.getVolume() + " newVolume=" + oldVolume +
                        " oldCurrentUnitPrice=" + derivative.getCurrentUnitPrice() + " newCurrentUnitPrice=" + newCurrentUnitPrice +
                        " currentTotalRealizedPL=" + derivative.getTotalRealizedPL() + " newTotalRealizedPL=" + newTotalRealizedPL);

                derivative.setTotalRealizedPL(newTotalRealizedPL);
                derivative.setVolume(oldVolume);
            }
            else if (type==DerivativeTType.BUY) {
                // old avg buy price = ((current avg buy price * total volume) - transaction value)/(total volume - transaction volume)
                BigDecimal oldVolume = isDelete ? derivative.getVolume().subtract(oldTransaction.getVolume())
                        : derivative.getVolume().subtract(oldTransaction.getVolume()).add(transaction.getVolume());
                Currency newAvgBuyPrice = calculateAverageBuyPrice(db,derivative);

                Log.trace(TAG, "updateDerivateOnTrasaction: name=" + derivative.getName() +
                        " currentVolume=" + derivative.getVolume() + " newVolume=" + oldVolume +
                        " oldCurrentUnitPrice=" + derivative.getCurrentUnitPrice() + " newCurrentUnitPrice=" + newCurrentUnitPrice +
                        " currentAvgBuyPrice=" + derivative.getAvgBuyPrice()+" newAvgBuyPrice="+newAvgBuyPrice);

                derivative.setVolume(oldVolume);
                derivative.setAvgBuyPrice(newAvgBuyPrice);
            }
            else {
                BigDecimal oldVolume = isDelete ? derivative.getVolume().subtract(oldTransaction.getVolume()) : derivative.getVolume().subtract(oldTransaction.getVolume()).add(transaction.getVolume());

                Log.trace(TAG, "updateDerivateOnTrasaction: name=" + derivative.getName() +
                        " currentVolume=" + derivative.getVolume() + " newVolume=" + oldVolume+
                        " oldCurrentUnitPrice=" + derivative.getCurrentUnitPrice() + " newCurrentUnitPrice=" + newCurrentUnitPrice);

                derivative.setVolume(oldVolume);
            }
            derivative.setCurrentUnitPrice(newCurrentUnitPrice);
        }
        // update the derivative
        updateDerivate(db,derivative);
    }

    Derivative updateDerivate(BalancesheetDb db, Derivative derivative) throws Exception {
        DerivativeDao dao = db.getDerivativeDao();
        db.getDerivativeDao().update(derivative);
        return derivative;
    }

    /**
     * this method will delete a derivative along with its transactions. all the deposite and withdraw actions
     * will be reverted too
     */
    void deleteDerivate(BalancesheetDb db, Derivative derivative) throws Exception {
        // delete its transactions
        deleteTransactionsForDerivative(db,derivative);
        // delete the derivative
        db.getDerivativeDao().delete(derivative);
    }

    void deleteTransactionsForDerivative(BalancesheetDb db, Derivative derivative) throws Exception {
        DerivativeTransactionDao dao = db.getDerivativeTransactionsDao();
        // get all transactions for derivative
        List<DerivativeTransaction> transactions = db.getDerivativeTransactionsDao()
                .queryBuilder().where().eq("derivative_id",derivative).query();
        if (null!=transactions) {
            // NOTE: null check for account required, because user may accidentally delete the account
            Account account = derivative.getDematAccount();
            List<TransactionHistory> histories = new ArrayList<>();
            BigDecimal toBeAdded = BigDecimal.ZERO;
            BigDecimal toBeSubtracted = BigDecimal.ZERO;
            for (DerivativeTransaction transaction : transactions) {
                if (null!=account) {
                    // value = volume * price
                    // value of BUY and REWARD transactions to be added
                    // all taxs to be added
                    // value of SELL transactions to be subtracted
                    BigDecimal value = transaction.getVolume().multiply(transaction.getPrice().getValue());
                    DerivativeTType type = transaction.getType();
                    if (type == DerivativeTType.SELL) {
                        toBeSubtracted = toBeSubtracted.add(value);
                    } else {
                        toBeAdded = toBeAdded.add(value);
                    }
                    toBeAdded = toBeAdded.add(transaction.getTax().getValue());
                }

                // add the linked histories to be deleted
                histories.add(transaction.getHistory());
            }
            if (null!=account) {
                Currency changeInBalance = Currency.from(toBeAdded.subtract(toBeSubtracted));
                Currency newBalance = account.getBalance().add(changeInBalance);

                Log.debug(TAG, "deleteTransactionsForDerivative: account=" + account.getId() +
                        " currentBalance=" + account.getBalance() + " newBalance=" + newBalance);

                account.setBalance(newBalance);
                // update the linked account's balance
                db.getAccountDao().update(account);
            }
            // delete the histories
            db.getTransactionHistoryDao().delete(histories);
            // now delete all transactions of derivative
            dao.delete(transactions);
        }
    }

    void deleteDerivativeTransaction(BalancesheetDb db, DerivativeTransaction transaction) throws Exception {
        TransactionHistoryCRUDHelper helper = new TransactionHistoryCRUDHelper();
        Log.debug(TAG,"deleteDerivativeTransaction: TO BE DELETED derivative-transaction's transaction-history="+transaction.getHistory());
        // query for history because the history is not fully loaded in derivative transaction
        TransactionHistory history = helper.getForSameId(db,transaction.getHistory());
        // delete the transaction history
        helper.delete(db,history);
        // delete the derivative transaction
        db.getDerivativeTransactionsDao().delete(transaction);
        // update the derivative
        Derivative derivative = transaction.getDerivative();
        updateDerivateOnTrasaction(db,derivative,null,transaction);
    }

    /**
     * calculate the weighted average of price of BUY transactions. this methods should be called after the transaction
     * is saved.
     */
    static Currency calculateAverageBuyPrice(BalancesheetDb db, Derivative derivative) throws Exception {
        DerivativeTransactionDao dao = db.getDerivativeTransactionsDao();
        QueryBuilder<DerivativeTransaction,Long> queryBuilder = dao.queryBuilder()
                .selectRaw("SUM(`volume`)","SUM(`price`*`volume`)").where().eq("derivative_id",derivative)
                .and().eq("type",DerivativeTType.BUY).queryBuilder();
        String query = queryBuilder.prepareStatementString();
        Log.debug(TAG,"calculateAverageBuyPrice: "+query);
        try (GenericRawResults<String[]> result = dao.queryRaw(query)) {
            if (null == result) {
                Log.debug(TAG,"calculateAverageBuyPrice: no query result");
                return derivative.getAvgBuyPrice();
            }
            String[] columns = result.getFirstResult();
            if (null==columns) {
                Log.debug(TAG,"calculateAverageBuyPrice: query result empty");
                return derivative.getAvgBuyPrice();
            }
            BigDecimal volume = new BigDecimal(columns[0]).setScale(4,RoundingMode.HALF_UP);
            BigDecimal price = new BigDecimal(columns[1]).setScale(4,RoundingMode.HALF_UP);
            Log.debug(TAG,"calculateAverageBuyPrice: derivative: id="+derivative.getId()+" total volume="+volume+" total buy price="+price);
            if (BigDecimal.ZERO.equals(volume)) {
                return derivative.getAvgBuyPrice();
            }
            BigDecimal average = price.divide(volume, RoundingMode.HALF_UP);
            return Currency.from(average);
        }
    }

    private static abstract class DatabaseTask<T> extends Task<T> {

        DatabaseTask() {}

        @Override
        protected T call() throws Exception {
            final BalancesheetDb db = BalancesheetDb.getInstance();
            return db.inTransaction(()-> executeInTransaction(db));
        }

        protected abstract T executeInTransaction(final BalancesheetDb db) throws Exception;
    }
}
