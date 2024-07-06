package rahulstech.jfx.balancesheet.database.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import rahulstech.jfx.balancesheet.database.entity.Derivative;
import rahulstech.jfx.balancesheet.database.entity.DerivativeTransaction;
import rahulstech.jfx.balancesheet.database.model.DerivativeReportModel;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.util.Log;

import java.util.Arrays;

public class ReportDao {

    private static final String TAG = ReportDao.class.getSimpleName();

    private final ConnectionSource source;

    public ReportDao(ConnectionSource source) {
        this.source = source;
    }

    public DerivativeReportModel getOverallDerivativeReport() {
        return DaoUtil.callWithoutExceptionHandling(()->
            TransactionManager.callInTransaction(source,()->{
                Dao<Derivative,Long> derivativeDao = DaoManager.createDao(source,Derivative.class);
                Dao<DerivativeTransaction,Long> transactionsDao = DaoManager.createDao(source, DerivativeTransaction.class);

                String queryDerivatives = derivativeDao.queryBuilder().selectRaw(
                        "SUM(`volume`*`avgBuyPrice`)",
                        "SUM(`totalRealizedPL`)",
                        "SUM(`volume`*(`currentUnitPrice`-`avgBuyPrice`))")
                        .prepareStatementString();
                String queryTax = transactionsDao.queryBuilder()
                        .selectRaw("SUM(`tax`)")
                        .prepareStatementString();

                DerivativeReportModel report = new DerivativeReportModel();

                try (GenericRawResults<String[]> results = derivativeDao.queryRaw(queryDerivatives)) {
                    if (null==results) {
                        Log.info(TAG,"getOverallDerivativeReport: dervatives query result empty");
                        return report;
                    }
                    String[] columns = results.getFirstResult();
                    Log.trace(TAG,"getOverallDerivativeReport: derivatives columns="+ Arrays.toString(columns));
                    report.setTotalInvestedValue(Currency.from(columns[0]));
                    report.setTotalRealizedPL(Currency.from(columns[1]));
                    report.setTotalUnrealizedPL(Currency.from(columns[2]));
                }

                try (GenericRawResults<String[]> results = transactionsDao.queryRaw(queryTax)) {
                    if (null==results) {
                        Log.info(TAG,"getOverallDerivativeReport: tax query result empty");
                        return report;
                    }
                    String[] columns = results.getFirstResult();
                    Log.trace(TAG,"getOverallDerivativeReport: derivative-transactions columns="+ Arrays.toString(columns));
                    report.setTotalTax(Currency.from(columns[0]));
                }

                return report;
            })
        );
    }
}
