package rahulstech.jfx.balancesheet.concurrent;

import javafx.concurrent.Task;
import rahulstech.jfx.balancesheet.database.BalancesheetDb;
import rahulstech.jfx.balancesheet.database.dao.*;
import rahulstech.jfx.balancesheet.database.entity.*;
import rahulstech.jfx.balancesheet.database.model.CategoryBudgetModel;
import rahulstech.jfx.balancesheet.database.model.MonthlyCategoryModel;
import rahulstech.jfx.balancesheet.database.model.MonthlyTypeModel;
import rahulstech.jfx.balancesheet.database.type.TransactionType;
import rahulstech.jfx.balancesheet.json.JsonHelper;
import rahulstech.jfx.balancesheet.json.model.DataModel;

import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TaskUtils {

    public static <T> Task<T> createTask(Callable<T> callable, TaskCallback<T> onSuccess, TaskCallback<T> onFail){
        Task<T> task = new Task<T>() {
            @Override
            protected T call() throws Exception {
                return callable.call();
            }
        };
        if (null != onSuccess) {
            task.setOnSucceeded(e->onSuccess.call(task));
        }
        if (null != onFail) {
            task.setOnFailed(e->onFail.call(task));
        }
        return task;
    }

    public static Task<DataModel> importJSON(File file, TaskCallback<DataModel> onSuccess, TaskCallback<DataModel> onFail) {
        return createTask(()-> JsonHelper.readJsonFile(file),onSuccess,onFail);
    }
    public static Task<List<YearMonth>> getYearMonthsListBetweenMinAndMaxHistoryDate(boolean asc,
                                                                                     TaskCallback<List<YearMonth>> onSuccess,
                                                                                     TaskCallback<List<YearMonth>> onFail) {
        return createTask(()->{
            ChartDao dao = BalancesheetDb.getInstance().getChartDao();
            YearMonth[] min_max = dao.getMinMaxHistoryDates();
            if (null==min_max) {
                return Collections.emptyList();
            }
            YearMonth min = min_max[0];
            YearMonth max = min_max[1];
            final List<YearMonth> yearMonths = new ArrayList<>();
            for (YearMonth now=min; max.equals(now) || max.isAfter(now); now=now.plusMonths(1)) {
                yearMonths.add(now);
            }
            if (!asc) {
                int size = yearMonths.size();
                return IntStream.range(0,size)
                        .mapToObj(i->yearMonths.get(size-1-i))
                        .collect(Collectors.toList());
            }
            return yearMonths;
        },onSuccess,onFail);
    }

    public static Task<List<MonthlyTypeModel>> getMonthlyTypeChartQueryTask(YearMonth startMonth, YearMonth endMonth, TransactionType type,
                                                                            TaskCallback<List<MonthlyTypeModel>> onSuccess,
                                                                            TaskCallback<List<MonthlyTypeModel>> onFail) {
        return createTask(()->{
            ChartDao dao = BalancesheetDb.getInstance().getChartDao();
            return dao.getMonthlyTypeWiseTotalAmount(type, startMonth, endMonth);
        },onSuccess,onFail);
    }

    public static Task<List<MonthlyCategoryModel>> getMonthlyCategoroyChartQueryTask(YearMonth startMonth, YearMonth endMonth, List<Category> categories,
                                                                                TaskCallback<List<MonthlyCategoryModel>> onSuccess,
                                                                                TaskCallback<List<MonthlyCategoryModel>> onFail) {
        return createTask(()->{
            ChartDao dao = BalancesheetDb.getInstance().getChartDao();
            return dao.getMonthlyCategoryWiseTotalAmount(categories, startMonth, endMonth);
        },onSuccess,onFail);
    }

    public static Task<Boolean> deleteCategories(List<Category> categories,
                                                                            TaskCallback<Boolean> onSuccess,
                                                                            TaskCallback<Boolean> onFail) {
        return createTask(()->{
            CategoryDao dao = BalancesheetDb.getInstance().getCategoryDao();
            return dao.deleteCategories(categories);
        },onSuccess,onFail);
    }

    public static Task<List<Category>> getAllCategories(TaskCallback<List<Category>> onSuccess,
                                              TaskCallback<List<Category>> onFail) {
        return createTask(()->{
            CategoryDao dao = BalancesheetDb.getInstance().getCategoryDao();
            return dao.getAll();
        },onSuccess,onFail);
    }

    public static Task<List<Category>> filterCategory(final List<Category> original, String phrase, TaskCallback<List<Category>> onSuccess,
                                                        TaskCallback<List<Category>> onFail) {
        return createTask(()->{
            if (null == phrase || phrase.isEmpty()) {
                return original;
            }
            return original.stream()
                .filter(category -> {
                    String name = category.getName();
                    return name.toLowerCase().contains(phrase.toLowerCase());
                })
                .collect(Collectors.toList());
        },onSuccess,onFail);
    }

    public static Task<Category> saveCategory(Category category, TaskCallback<Category> onSuccess,
                                              TaskCallback<Category> onFail) {
        return createTask(()->{
            CategoryDao dao = BalancesheetDb.getInstance().getCategoryDao();
            return dao.insertOrUpdate(category);
        },onSuccess,onFail);
    }

    public static Task<List<Account>> filterAccount(final List<Account> original, String phrase, TaskCallback<List<Account>> onSuccess,
                                                     TaskCallback<List<Account>> onFail) {
        return createTask(()->{
            if (null == phrase || phrase.isEmpty()) {
                return original;
            }
            return original.stream()
                    .filter(acc -> {
                        String name = acc.getName();
                        return name.toLowerCase().contains(phrase.toLowerCase());
                    })
                    .collect(Collectors.toList());
        },onSuccess,onFail);
    }

    public static Task<List<Account>> getAllAccounts(TaskCallback<List<Account>> onSuccess, TaskCallback<List<Account>> onFail) {
        return createTask(()->{
            AccountDao dao = BalancesheetDb.getInstance().getAccountDao();
            return dao.getAllAccounts();
        },onSuccess,onFail);
    }

    public static Task<Boolean> deleteAccount(List<Account> accounts, TaskCallback<Boolean> onSuccess, TaskCallback<Boolean> onFail) {
        return createTask(()-> BalancesheetDb.getInstance().getAccountDao().deleteAccounts(accounts),onSuccess,onFail);
    }

    public static Task<Account> saveAccount(Account account, TaskCallback<Account> onSuccess, TaskCallback<Account> onFail) {
        return createTask(()-> BalancesheetDb.getInstance().getAccountDao().saveAccount(account),onSuccess,onFail);
    }

    public static Task<Budget> saveBudget(Budget budget, TaskCallback<Budget> onSuccess, TaskCallback<Budget> onFail) {
        return createTask(()->BalancesheetDb.getInstance().getBudgetDao().saveBudget(budget),onSuccess,onFail);
    }

    public static Task<List<Budget>> filterBudget(BudgetFilterData data, TaskCallback<List<Budget>> onSuccess, TaskCallback<List<Budget>> onFail) {
        return createTask(()->BalancesheetDb.getInstance().getBudgetDao().filter(data),onSuccess,onFail);
    }

    public static Task<List<CategoryBudgetModel>> getCategoryBudgetChartData(LocalDate start, LocalDate end, List<Category> categories,
                                                                             TaskCallback<List<CategoryBudgetModel>> onSuccess,
                                                                             TaskCallback<List<CategoryBudgetModel>> onFail) {
        return createTask(()->{
            ChartDao dao = BalancesheetDb.getInstance().getChartDao();
            return dao.getCategoryBudget(start, end, categories);
        },onSuccess,onFail);
    }

    public static Task<Boolean> deleteBudgets(List<Budget> budgets, TaskCallback<Boolean> onSuccess,
                                              TaskCallback<Boolean> onFail ) {
        return createTask(()-> budgets.size()==BalancesheetDb.getInstance().getBudgetDao().delete(budgets),onSuccess,onFail);
    }

    public interface TaskCallback<T> {
        void call(Task<T> task);
    }
}