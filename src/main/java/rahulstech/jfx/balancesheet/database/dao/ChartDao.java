package rahulstech.jfx.balancesheet.database.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import rahulstech.jfx.balancesheet.database.entity.Budget;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.database.entity.HistoryCategory;
import rahulstech.jfx.balancesheet.database.entity.TransactionHistory;
import rahulstech.jfx.balancesheet.database.model.CategoryBudgetModel;
import rahulstech.jfx.balancesheet.database.model.MonthlyCategoryModel;
import rahulstech.jfx.balancesheet.database.model.MonthlyTypeModel;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.DbTypeLocalDate;
import rahulstech.jfx.balancesheet.database.type.TransactionType;
import rahulstech.jfx.balancesheet.util.Log;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static rahulstech.jfx.balancesheet.database.dao.DaoUtil.callWithoutExceptionHandling;

@SuppressWarnings("ALL")
public class ChartDao {

    private static final String TAG = ChartDao.class.getSimpleName();

    private Dao<TransactionHistory, Long> historyDao;
    private Dao<HistoryCategory,Long> historyCategoriesDao;
    private Dao<Category,Long> categoryDao;
    private Dao<Budget,Long> budgetDao;

    public ChartDao(ConnectionSource connectionSource) throws SQLException {
        historyDao = DaoManager.createDao(connectionSource, TransactionHistory.class);
        historyCategoriesDao = DaoManager.createDao(connectionSource, HistoryCategory.class);
        categoryDao = DaoManager.createDao(connectionSource,Category.class);
        budgetDao = DaoManager.createDao(connectionSource,Budget.class);
    }

    public List<MonthlyTypeModel> getMonthlyTypeWiseTotalAmount(TransactionType type, YearMonth startMonth, YearMonth endMonth) {
        return callWithoutExceptionHandling(()->{
            QueryBuilder<TransactionHistory,Long> queryBuilder = historyDao.queryBuilder();

            LocalDate startDate = startMonth.atDay(1);
            LocalDate endDate = endMonth.plusMonths(1).atDay(1); // plusMonths(1) to include the whole end month

            queryBuilder.selectRaw("strftime('%Y-%m', `when`) as yearMonth")
                    .selectRaw("SUM(amount) as totalAmount")
                    .selectRaw("type")
                    .where().eq("type", type)
                    .and().ge("when", startDate)
                    .and().lt("when", endDate)
                    .queryBuilder()
                    .groupByRaw("yearMonth, type");

            List<String[]> results = historyDao.queryRaw(queryBuilder.prepareStatementString()).getResults();
            List<MonthlyTypeModel> monthlyTypeModels = new ArrayList<>();

            for (String[] result : results) {
                YearMonth yearMonth = YearMonth.parse(result[0]);
                Currency totalAmount = Currency.from(result[1]);
                TransactionType resultType = TransactionType.valueOf(result[2]);
                monthlyTypeModels.add(new MonthlyTypeModel(yearMonth, totalAmount, resultType));
            }

            return monthlyTypeModels;
        });
    }

    public YearMonth[] getMinMaxHistoryDates() throws SQLException {
        QueryBuilder<TransactionHistory,Long> queryBuilder = historyDao.queryBuilder();
        queryBuilder.selectRaw("MIN(`when`)").selectRaw("MAX(`when`)");
        long count =  queryBuilder.countOf();

        Log.debug(TAG,"getMinMaxHistoryDates(): count="+count);
        if (count==0) {
            return null;
        }

        String[] columns = historyDao.queryRaw(queryBuilder.prepareStatementString()).getFirstResult();
        Log.trace(TAG," getMinMaxHistoryDates(): columns="+java.util.Arrays.toString(columns));

        LocalDate minDate = (LocalDate) DbTypeLocalDate.getSingleton().parseDefaultString(null,columns[0]);
        LocalDate maxDate = (LocalDate) DbTypeLocalDate.getSingleton().parseDefaultString(null,columns[1]);
        YearMonth[] result = new YearMonth[2];
        result[0] = YearMonth.from(minDate);
        result[1] = YearMonth.from(maxDate);
        return result;
    }

    public List<MonthlyCategoryModel> getMonthlyCategoryWiseTotalAmount(List<Category> categories, YearMonth startMonth, YearMonth endMonth) {
        return callWithoutExceptionHandling(()->{
            QueryBuilder<TransactionHistory,Long> queryBuilder = historyDao.queryBuilder();
            QueryBuilder<HistoryCategory,Long> joinHistoryCategoriesQuery = historyCategoriesDao.queryBuilder();
            QueryBuilder<Category,Long> joinCategoryQuery = categoryDao.queryBuilder();

            LocalDate startDate = startMonth.atDay(1);
            LocalDate endDate = endMonth.plusMonths(1).atDay(1); // plusMonths(1) to include the whole end month

            joinHistoryCategoriesQuery.join(joinCategoryQuery)
                    .where().in("category_id",categories);

            queryBuilder.selectRaw("strftime('%Y-%m', `when`) as yearMonth")
                    .selectRaw("SUM(amount) as totalAmount")
                    .selectRaw("`categories`.`id`","`categories`.`name`")
                    .where().ge("when", startDate)
                    .and().lt("when", endDate)
                    .queryBuilder()
                    .groupByRaw("`yearMonth`, `categories`.`name`");

            queryBuilder.leftJoin(joinHistoryCategoriesQuery);

            List<String[]> results = historyDao.queryRaw(queryBuilder.prepareStatementString()).getResults();
            List<MonthlyCategoryModel> monthlyTypeModels = new ArrayList<>();

            for (String[] result : results) {
                YearMonth yearMonth = YearMonth.parse(result[0]);
                Currency totalAmount = Currency.from(result[1]);
                long categoryId = Long.parseLong(result[2]);
                String categoryName = result[3];
                Category category = new Category();
                category.setId(categoryId);
                category.setName(categoryName);
                monthlyTypeModels.add(new MonthlyCategoryModel(yearMonth, totalAmount, category));
            }

            return monthlyTypeModels;
        });
    }

    public List<CategoryBudgetModel> getCategoryBudget(LocalDate start, LocalDate end, List<Category> categories) {
        return callWithoutExceptionHandling(()->{
            QueryBuilder<TransactionHistory,Long> queryBuilderHistory = historyDao.queryBuilder();
            QueryBuilder<Budget,Long> queryBuilderBudget = budgetDao.queryBuilder();

            // get all histories for given parameters
            List<TransactionHistory> histories = queryBuilderHistory
                    .join(historyCategoriesDao.queryBuilder().where().in("category_id",categories).queryBuilder())
                    .where().ge("when",start)
                    .and().le("when",end)
                    .queryBuilder()
                    .query();

            // createa map of Categories and CategoryBudgetModels
            Map<Category,CategoryBudgetModel> map = new HashMap<>();

            // calculate the category total for each category
            for (Category category : categories) {
                CategoryBudgetModel model = map.get(category);
                if (null==model) {
                    model = new CategoryBudgetModel();
                    model.setCategoryName(category.getName());
                    model.setBudgetTotalAmount(Currency.ZERO);
                    model.setCategoryTotalAmount(Currency.ZERO);
                    map.put(category,model);
                }
                for (TransactionHistory history : histories) {
                    if (history.getCategories().contains(category)) {
                        model.setCategoryTotalAmount( model.getCategoryTotalAmount().add(history.getAmount()));
                    }
                }
            }

            // get budgets for given parameters
            List<Budget> budgets = queryBuilderBudget.where()
                    .ge("start_date",start)
                    .and().le("end_date",end)
                    .and().in("category_id",categories)
                    .query();

            // calculate budget total for each budget
            for (Budget budget : budgets) {
                CategoryBudgetModel model = map.get(budget.getCategory());
                Log.debug(TAG,"getCategoryBudget(): in budget loop model null "+(null==model)+" for category="+budget.getCategory().getName());
                model.setBudgetTotalAmount(model.getBudgetTotalAmount().add(budget.getAmount()));
            }

            // finally returns the list the CategoryBudgetModels
            return new ArrayList<>(map.values());
        });
    }

    // Add other CRUD methods as needed
}
