package rahulstech.jfx.balancesheet.controller;

import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.database.entity.Category;
import rahulstech.jfx.balancesheet.database.entity.TransactionHistory;
import rahulstech.jfx.balancesheet.database.model.MonthlyCategoryModel;
import rahulstech.jfx.balancesheet.database.model.MonthlyTypeModel;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.TransactionType;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SampleData {

    public static List<TransactionHistory> generateSampleData() {
        List<TransactionHistory> sampleData = new ArrayList<>();

        // Sample Accounts
        Account account1 = new Account("Savings Account", Currency.from(1000));
        account1.setId(1);
        Account account2 = new Account("Checking Account", Currency.from(500));
        account2.setId(2);

        // Generate sample transactions
        for (int i = 1; i <= 50; i++) {
            TransactionHistory transaction = new TransactionHistory();
            transaction.setId(i);
            transaction.setWhen(LocalDate.now().minusDays(i)); // Varying dates for sample data
            transaction.setAmount(Currency.from(Math.random() * 100)); // Random amount
            transaction.setSrc(account1);
            transaction.setDest(account2);
            transaction.setType(TransactionType.values()[(int) (Math.random() * TransactionType.values().length)]);
            transaction.setDescription("Sample transaction " + i);

            sampleData.add(transaction);
        }

        return sampleData;
    }

    public static List<MonthlyTypeModel> getSampleMonthlyTypeModels() {
        List<MonthlyTypeModel> monthlyTypeModels = new ArrayList<>();

        monthlyTypeModels.add(new MonthlyTypeModel(
                YearMonth.of(2024, 5),
                Currency.from("100.00"),
                TransactionType.DEPOSIT
        ));

        monthlyTypeModels.add(new MonthlyTypeModel(
                YearMonth.of(2024, 6),
                Currency.from("250.00"),
                TransactionType.DEPOSIT
        ));

        return monthlyTypeModels;
    }

    public static List<YearMonth> getSampleYearMonths() {
        List<YearMonth> yearMonths = new ArrayList<>();

        // Start from April 2023
        YearMonth start = YearMonth.of(2023, 4);

        // End at August 2024
        YearMonth end = YearMonth.of(2024, 8);

        // Loop from start to end and add each YearMonth to the list
        for (YearMonth yearMonth = start; !yearMonth.isAfter(end); yearMonth = yearMonth.plusMonths(1)) {
            yearMonths.add(yearMonth);
        }
        return yearMonths;
    }

    public static List<MonthlyCategoryModel> getMonthlyCategorySampleData() {
        Category income = new Category();
        income.setName("Income");

        Category expense = new Category();
        expense.setName("Expense");

        return Arrays.asList(
                new MonthlyCategoryModel(YearMonth.of(2024, 1), Currency.from("900.00"), income),
                new MonthlyCategoryModel(YearMonth.of(2024, 1), Currency.from("450.00"), expense),
                new MonthlyCategoryModel(YearMonth.of(2024, 2), Currency.from("950.00"), income),
                new MonthlyCategoryModel(YearMonth.of(2024, 2), Currency.from("475.00"), expense),
                new MonthlyCategoryModel(YearMonth.of(2024, 3), Currency.from("1000.00"), income),
                new MonthlyCategoryModel(YearMonth.of(2024, 3), Currency.from("500.00"), expense),
                new MonthlyCategoryModel(YearMonth.of(2024, 4), Currency.from("1050.00"), income),
                new MonthlyCategoryModel(YearMonth.of(2024, 4), Currency.from("525.00"), expense),
                new MonthlyCategoryModel(YearMonth.of(2024, 5), Currency.from("1100.00"), income),
                new MonthlyCategoryModel(YearMonth.of(2024, 5), Currency.from("550.00"), expense),
                new MonthlyCategoryModel(YearMonth.of(2024, 6), Currency.from("1150.00"), income),
                new MonthlyCategoryModel(YearMonth.of(2024, 6), Currency.from("575.00"), expense),
                new MonthlyCategoryModel(YearMonth.of(2024, 7), Currency.from("1200.00"), income),
                new MonthlyCategoryModel(YearMonth.of(2024, 7), Currency.from("600.00"), expense),
                new MonthlyCategoryModel(YearMonth.of(2024, 8), Currency.from("1250.00"), income),
                new MonthlyCategoryModel(YearMonth.of(2024, 8), Currency.from("625.00"), expense),
                new MonthlyCategoryModel(YearMonth.of(2024, 9), Currency.from("1300.00"), income),
                new MonthlyCategoryModel(YearMonth.of(2024, 9), Currency.from("650.00"), expense),
                new MonthlyCategoryModel(YearMonth.of(2024, 10), Currency.from("1350.00"), income),
                new MonthlyCategoryModel(YearMonth.of(2024, 10), Currency.from("675.00"), expense),
                new MonthlyCategoryModel(YearMonth.of(2024, 11), Currency.from("1400.00"), income),
                new MonthlyCategoryModel(YearMonth.of(2024, 11), Currency.from("700.00"), expense),
                new MonthlyCategoryModel(YearMonth.of(2024, 12), Currency.from("1450.00"), income),
                new MonthlyCategoryModel(YearMonth.of(2024, 12), Currency.from("725.00"), expense)
        );
    }
}

