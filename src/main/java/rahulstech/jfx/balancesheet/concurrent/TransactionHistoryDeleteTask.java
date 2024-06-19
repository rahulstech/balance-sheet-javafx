package rahulstech.jfx.balancesheet.concurrent;

import javafx.concurrent.Task;
import rahulstech.jfx.balancesheet.database.BalancesheetDb;
import rahulstech.jfx.balancesheet.database.dao.AccountDao;
import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.database.entity.TransactionHistory;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.TransactionType;

import java.util.List;

public class TransactionHistoryDeleteTask extends Task<Boolean> {

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
        TransactionType type = history.getType();
        if(!BalancesheetDb.getInstance().getTransactionHistoryDao().deleteTransactionHistory(history)) {
            return;
        }
        Currency changeForSrcAccount = calculateAmountChangeForSrcAccount(history);
        updateAccountBalance(history.getSrc(),changeForSrcAccount);
        if (type == TransactionType.TRANSFER) {
            Currency changeForDestAccount = changeForSrcAccount.negate();
            updateAccountBalance(history.getDest(),changeForDestAccount);
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
        TransactionType type = history.getType();
        if (type == TransactionType.DEPOSIT) {
            return amount.negate();
        }
        else {
            return amount;
        }
    }
}

