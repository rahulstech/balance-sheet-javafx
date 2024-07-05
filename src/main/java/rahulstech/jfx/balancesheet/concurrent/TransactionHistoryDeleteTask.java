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
        final BalancesheetDb db = BalancesheetDb.getInstance();
        return db.inTransaction(()->{
            List<TransactionHistory> histories = this.histories;
            TransactionHistoryCRUDHelper helper = new TransactionHistoryCRUDHelper();
            for(TransactionHistory history : histories) {
                helper.delete(db,history);
            }
            return true;
        });
    }
}

