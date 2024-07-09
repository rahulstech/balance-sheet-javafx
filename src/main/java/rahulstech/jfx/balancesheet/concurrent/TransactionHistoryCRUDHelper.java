package rahulstech.jfx.balancesheet.concurrent;

import rahulstech.jfx.balancesheet.database.BalancesheetDb;
import rahulstech.jfx.balancesheet.database.dao.AccountDao;
import rahulstech.jfx.balancesheet.database.dao.TransactionHistoryDao;
import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.database.entity.TransactionHistory;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.TransactionType;
import rahulstech.jfx.balancesheet.util.Log;

class TransactionHistoryCRUDHelper {

    private static final String TAG = TransactionHistoryCRUDHelper.class.getSimpleName();

    TransactionHistoryCRUDHelper() {}

    TransactionHistory save(BalancesheetDb db, TransactionHistory history) throws Exception {
        TransactionHistoryDao dao = db.getTransactionHistoryDao();
        boolean isEditing = 0!=history.getId();
        TransactionHistory old = isEditing ? dao.getTransactionHistoryById(history.getId()) : null;
        TransactionHistory saved = saveHistory(history);
        if (null!=saved.getSrc()) {
            Currency srcBalanceChange = calculateAmountChangeForSrcAccount(old,saved);
            updateAccountBalance(saved.getSrc(),srcBalanceChange);
        }
        if (null!=saved.getDest()){
            Currency changeForDestAccount = calculateAmountChangeForDestAccount(old,saved);
            updateAccountBalance(saved.getDest(),changeForDestAccount);
        }
        return saved;
    }

    void delete(BalancesheetDb db, TransactionHistory history) throws Exception {
        if (null==history) {
            return;
        }
        db.getTransactionHistoryDao().delete(history);
        if (null!=history.getSrc()) {
            Currency change = calculateAmountChangeForSrcAccount(history,null);
            updateAccountBalance(history.getSrc(),change);
        }
        if (null!=history.getDest()) {
            Currency change = calculateAmountChangeForDestAccount(history,null);
            updateAccountBalance(history.getDest(),change);
        }
    }

    TransactionHistory getForSameId(BalancesheetDb db, TransactionHistory history) throws Exception {
        return db.getTransactionHistoryDao().queryForSameId(history);
    }

    void updateAccountBalance(Account account, Currency change) throws Exception {
        if (null==account) {
            return;
        }
        AccountDao dao = BalancesheetDb.getInstance().getAccountDao();
        // it's found that the provided account may not contain the refreshed value.
        // so fetch the account with most recent values first then update it
        Account real = dao.queryForSameId(account);
        Currency balance = real.getBalance();
        Currency newBalance = balance.add(change);
        real.setBalance(newBalance);
        Log.debug(TAG,"updateAccountBalance: from="+balance+" to="+newBalance);
        dao.saveAccount(real);
    }

    private Currency calculateAmountChangeForSrcAccount(TransactionHistory oldV, TransactionHistory newV) {
        Currency old_amount = getAmount(oldV);
        Currency old_tax = getTax(oldV,true);
        Currency new_amount = getAmount(newV);
        Currency new_tax = getTax(newV,true);
        TransactionType type;
        if (null!=newV) {
            type = newV.getType();
        }
        else {
            type = oldV.getType();
        }
        Currency change;
        if (type==TransactionType.DEPOSIT) {
            change = new_amount.subtract(new_tax).subtract(old_amount).add(old_tax);
        }
        else {
            change = old_amount.add(old_tax).subtract(new_amount).subtract(new_tax);
        }
        Log.trace(TAG,"calculateAmountChangeForSrcAccount(): change="+change);
        return change;
    }

    private Currency calculateAmountChangeForDestAccount(TransactionHistory oldV, TransactionHistory newV) {
        Currency old_amount = getAmount(oldV);
        Currency old_tax = getTax(oldV,false);
        Currency new_amount = getAmount(newV);
        Currency new_tax = getTax(newV,false);
        TransactionType type;
        if (null!=newV) {
            type = newV.getType();
        }
        else {
            type = oldV.getType();
        }
        Currency change;
        if (type==TransactionType.TRANSFER) {
            change = new_amount.subtract(new_tax).subtract(old_amount).add(old_tax);
        }
        else {
            change = Currency.ZERO;
        }
        Log.trace(TAG,"calculateAmountChangeForSrcAccount(): change="+change);
        return change;
    }

    private Currency getAmount(TransactionHistory history) {
        return null==history ? Currency.ZERO : history.getAmount();
    }

    private Currency getTax(TransactionHistory history, boolean isForSrc) {
        if (null==history) {
            return Currency.ZERO;
        }
        Currency tax = history.getTax();
        if (null==tax) {
            return Currency.ZERO;
        }
        if (isForSrc && history.isTaxSrc()) {
            return tax;
        }
        else if (!isForSrc && !history.isTaxSrc()) {
            return tax;
        }
        return Currency.ZERO;
    }

    private TransactionHistory saveHistory(TransactionHistory history) {
        TransactionHistoryDao dao = BalancesheetDb.getInstance().getTransactionHistoryDao();
        return dao.saveTransaction(history);
    }
}
