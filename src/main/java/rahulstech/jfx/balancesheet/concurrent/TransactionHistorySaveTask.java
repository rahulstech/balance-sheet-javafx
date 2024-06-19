package rahulstech.jfx.balancesheet.concurrent;

import javafx.concurrent.Task;
import rahulstech.jfx.balancesheet.database.BalancesheetDb;
import rahulstech.jfx.balancesheet.database.dao.AccountDao;
import rahulstech.jfx.balancesheet.database.dao.TransactionHistoryDao;
import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.database.entity.TransactionHistory;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.TransactionType;

public class TransactionHistorySaveTask extends Task<TransactionHistory> {

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
            TransactionType type = saved.getType();
            Currency changeForSrcAccount = calculateAmountChangeForSrcAccount(old,saved);
            updateAccountBalance(saved.getSrc(),changeForSrcAccount);
            if (type == TransactionType.TRANSFER) {
                Currency changeForDestAccount = changeForSrcAccount.negate();
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
        Currency new_amount = newV.getAmount();
        TransactionType type = newV.getType();
        if (null == oldV) {
            if (type == TransactionType.DEPOSIT) {
                return new_amount;
            }
            else {
                return new_amount.negate();
            }
        }
        Currency old_amount = oldV.getAmount();
        if (type == TransactionType.DEPOSIT) {
            return new_amount.subtract(old_amount);
        }
        else {
            return old_amount.subtract(new_amount);
        }
    }

    private TransactionHistory saveHistory(TransactionHistory history) {
        TransactionHistoryDao dao = BalancesheetDb.getInstance().getTransactionHistoryDao();;
        return dao.saveTransaction(history);
    }
}
