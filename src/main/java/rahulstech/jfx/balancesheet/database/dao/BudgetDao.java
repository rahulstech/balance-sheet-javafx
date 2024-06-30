package rahulstech.jfx.balancesheet.database.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import rahulstech.jfx.balancesheet.database.entity.Budget;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.util.Log;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static rahulstech.jfx.balancesheet.database.dao.DaoUtil.callWithoutExceptionHandling;

public class BudgetDao extends BaseDaoImpl<Budget,Long> {

    private static final String TAG = BudgetDao.class.getSimpleName();

    public BudgetDao(ConnectionSource source) throws SQLException {
        super(source, Budget.class);
    }

    public Budget saveBudget(Budget budget) {
        return callWithoutExceptionHandling(()->{
            if (1!=createOrUpdate(budget).getNumLinesChanged()) {
                Log.trace(TAG,"Budget="+budget+" not saved");
                throw new SQLException("budget not saved");
            }
            return budget;
        });
    }

    public List<Budget> filter(BudgetFilterData data) {
        return callWithoutExceptionHandling(()->{
            LocalDate start = data.getStart();
            LocalDate end = data.getEnd();
            List<Category> categories = data.getCategories();
            QueryBuilder<Budget,Long> queryBuilder = queryBuilder();
            Where<Budget,Long> where = queryBuilder.where();
            where.isNotNull("id");
            if (null!=categories && !categories.isEmpty()) {
                where.and().in("category_id",categories);
            }
            if (null!=start)  {
                where.and().ge("start_date",start);
            }
            if (null!=end) {
                where.and().le("end_date",end);
            }
            return queryBuilder.query();
        });

    }
}
