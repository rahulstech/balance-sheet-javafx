package rahulstech.jfx.balancesheet.concurrent;

import javafx.concurrent.Task;
import rahulstech.jfx.balancesheet.database.BalancesheetDb;
import rahulstech.jfx.balancesheet.database.entity.TransactionHistory;

@SuppressWarnings("ALL")
public class TransactionHistorySaveTask extends Task<TransactionHistory> {

    private static final String TAG = TransactionHistorySaveTask.class.getSimpleName();

    private TransactionHistory history;

    protected TransactionHistorySaveTask() {}

    public TransactionHistorySaveTask(TransactionHistory history) {
        this.history = history;
    }

    @Override
    protected TransactionHistory call() throws Exception {
        final BalancesheetDb db = BalancesheetDb.getInstance();
        return db.inTransaction(()->new TransactionHistoryCRUDHelper().save(db,history));
    }
}
