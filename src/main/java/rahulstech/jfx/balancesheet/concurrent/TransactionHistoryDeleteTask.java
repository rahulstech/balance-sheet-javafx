package rahulstech.jfx.balancesheet.concurrent;

import javafx.concurrent.Task;
import rahulstech.jfx.balancesheet.database.BalancesheetDb;
import rahulstech.jfx.balancesheet.database.dao.AccountDao;
import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.database.entity.TransactionHistory;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.TransactionType;
import rahulstech.jfx.balancesheet.util.Log;

import java.util.List;

@SuppressWarnings("ALL")
public class TransactionHistoryDeleteTask extends Task<Boolean> {

    private static final String TAG = TransactionHistoryDeleteTask.class.getSimpleName();

    private final List<TransactionHistory> histories;

    public TransactionHistoryDeleteTask(List<TransactionHistory> histories) {
        this.histories = histories;
    }

    @Override
    protected Boolean call() throws Exception {
        return BalancesheetDb.getInstance().inTransaction(()->{
            List<TransactionHistory> histories = this.histories;
            for(TransactionHistory history : histories) {
                deleteHistory(history);
            }
            return true;
        });
    }

    private void deleteHistory(TransactionHistory history) {
        if(!BalancesheetDb.getInstance().getTransactionHistoryDao().deleteTransactionHistory(history)) {
            return;
        }
        if (null!=history.getSrc()) {
            Currency change = calculateAmountChangeForSrcAccount(history);
            updateAccountBalance(history.getSrc(),change);
        }
        if (null!=history.getDest()) {
            Currency change = calculateAmountChangeForDestAccount(history);
            updateAccountBalance(history.getDest(),change);
        }
    }

    private void updateAccountBalance(Account account, Currency change) {
        AccountDao dao = BalancesheetDb.getInstance().getAccountDao();
        Currency balance = account.getBalance();
        Currency newBalance = balance.add(change);
        account.setBalance(newBalance);
        dao.updateAccount(account);
    }

    private Currency calculateAmountChangeForSrcAccount(TransactionHistory history) {
        Currency amount = history.getAmount();
        Currency tax = null==history.getTax() || !history.isTaxSrc() ? Currency.ZERO : history.getTax();
        TransactionType type = history.getType();
        Currency change;
        if (type == TransactionType.DEPOSIT) {
            change = amount.negate().add(tax);
        }
        else {
            change = amount.add(tax);
        }
        Log.trace(TAG,"calculateAmountChangeForSrcAccount(): change="+change);
        return change;
    }

    private Currency calculateAmountChangeForDestAccount(TransactionHistory history) {
        Currency amount = history.getAmount();
        Currency tax = null==history.getTax() || history.isTaxSrc() ? Currency.ZERO : history.getTax();
        TransactionType type = history.getType();
        Currency change;
        if (type == TransactionType.TRANSFER) {
            change = amount.negate().add(tax);
        }
        else {
            change = Currency.ZERO;
        }
        Log.trace(TAG,"calculateAmountChangeForDestAccount(): change="+change);
        return change;
    }
}

