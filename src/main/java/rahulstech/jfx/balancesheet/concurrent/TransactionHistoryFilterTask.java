package rahulstech.jfx.balancesheet.concurrent;

import javafx.concurrent.Task;
import rahulstech.jfx.balancesheet.database.BalancesheetDb;
import rahulstech.jfx.balancesheet.database.dao.HistoryFilterData;
import rahulstech.jfx.balancesheet.database.dao.TransactionHistoryDao;
import rahulstech.jfx.balancesheet.database.entity.TransactionHistory;

import java.util.List;
import java.util.stream.Collectors;

public class TransactionHistoryFilterTask extends Task<List<TransactionHistory>> {

    private static final int FILTER_BY_DATA = 1;

    private static final int FILTER_BY_PHRASE = 2;

    private final int id;
    private List<TransactionHistory> original;
    private String filterPhrase;
    private HistoryFilterData filterData;

    public TransactionHistoryFilterTask(List<TransactionHistory> histories, String filterPhrase) {
        this.id = FILTER_BY_PHRASE;
        this.original = histories;
        this.filterPhrase = filterPhrase.toLowerCase();
    }

    public TransactionHistoryFilterTask(HistoryFilterData filterData) {
        this.id = FILTER_BY_DATA;
        this.filterData = filterData;
    }

    @Override
    protected List<TransactionHistory> call() throws Exception {
        if (id == FILTER_BY_PHRASE) {
            return filterByPhrase(this.filterPhrase);
        }
        else {
            return filterByData(this.filterData);
        }
    }

    private List<TransactionHistory> filterByPhrase(String phrase) {
        if (null == phrase || phrase.isEmpty()) {
            return original;
        }
        return original.stream()
                .filter(history -> {
                    String description = history.getDescription();
                    if (null == description) {
                        return false;
                    }
                    return description.toLowerCase().contains(phrase.toLowerCase());
                })
                .collect(Collectors.toList());
    }

    private List<TransactionHistory> filterByData(HistoryFilterData data) {
        TransactionHistoryDao dao = BalancesheetDb.getInstance().getTransactionHistoryDao();
        return dao.getAllTransactionHistoryByData(data);
    }
}
