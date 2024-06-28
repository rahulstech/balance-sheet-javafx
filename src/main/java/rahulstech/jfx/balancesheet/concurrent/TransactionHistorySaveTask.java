package rahulstech.jfx.balancesheet.concurrent;

import javafx.concurrent.Task;
import rahulstech.jfx.balancesheet.database.BalancesheetDb;
import rahulstech.jfx.balancesheet.database.dao.AccountDao;
import rahulstech.jfx.balancesheet.database.dao.TransactionHistoryDao;
import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.database.entity.TransactionHistory;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.TransactionType;
import rahulstech.jfx.balancesheet.util.Log;

@SuppressWarnings("ALL")
public class TransactionHistorySaveTask extends Task<TransactionHistory> {

    private static final String TAG = TransactionHistorySaveTask.class.getSimpleName();

    private final TransactionHistory history;

    public TransactionHistorySaveTask(TransactionHistory history) {
        this.history = history;
    }

    @Override
    protected TransactionHistory call() throws Exception {
        BalancesheetDb db = BalancesheetDb.getInstance();
        TransactionHistoryDao dao = db.getTransactionHistoryDao();
        return db.inTransaction(()->{
            boolean isEditing = 0!=this.history.getId();
            TransactionHistory old = isEditing ? dao.getTransactionHistoryById(this.history.getId()) : null;
            TransactionHistory saved = saveHistory(this.history);
            if (null!=saved.getSrc()) {
                Currency srcBalanceChange = calculateAmountChangeForSrcAccount(old,saved);
                updateAccountBalance(saved.getSrc(),srcBalanceChange);
            }
            if (null!=saved.getDest()){
                Currency changeForDestAccount = calculateAmountChangeForDestAccount(old,saved);
                updateAccountBalance(saved.getDest(),changeForDestAccount);
            }
            return saved;
        });
    }

    private void updateAccountBalance(Account account, Currency change) {
        AccountDao dao = BalancesheetDb.getInstance().getAccountDao();
        Currency balance = account.getBalance();
        Currency newBalance = balance.add(change);
        account.setBalance(newBalance);
        dao.updateAccount(account);
    }

    private Currency calculateAmountChangeForSrcAccount(TransactionHistory oldV, TransactionHistory newV) {
        Currency old_amount = null==oldV ? Currency.ZERO : oldV.getAmount();
        Currency old_tax = null==oldV || null==oldV.getTax() || !oldV.isTaxSrc() ? Currency.ZERO : oldV.getTax();
        Currency new_amount = newV.getAmount();
        Currency new_tax = null==newV.getTax() || !newV.isTaxSrc() ? Currency.ZERO : newV.getTax();
        TransactionType type = newV.getType();
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
        Currency old_amount = null==oldV ? Currency.ZERO : oldV.getAmount();
        Currency old_tax = null==oldV || null==oldV.getTax() || oldV.isTaxSrc() ? Currency.ZERO : oldV.getTax();
        Currency new_amount = newV.getAmount();
        Currency new_tax = null==newV.getTax() || newV.isTaxSrc() ? Currency.ZERO : newV.getTax();
        TransactionType type = newV.getType();
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

    private TransactionHistory saveHistory(TransactionHistory history) {
        TransactionHistoryDao dao = BalancesheetDb.getInstance().getTransactionHistoryDao();
        return dao.saveTransaction(history);
    }
}
