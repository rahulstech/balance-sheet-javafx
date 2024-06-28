package rahulstech.jfx.balancesheet.concurrent;

import javafx.concurrent.Task;
import rahulstech.jfx.balancesheet.database.BalancesheetDb;
import rahulstech.jfx.balancesheet.database.dao.AccountDao;
import rahulstech.jfx.balancesheet.database.dao.TransactionHistoryDao;
import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.database.entity.TransactionHistory;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.TransactionType;
import rahulstech.jfx.balancesheet.json.model.DataModel;
import rahulstech.jfx.balancesheet.json.model.MoneyTransfer;
import rahulstech.jfx.balancesheet.json.model.Person;
import rahulstech.jfx.balancesheet.json.model.Transaction;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class InsertTask extends Task<Boolean> {

    private static final String TAG = InsertTask.class.getSimpleName();

    public static class FilterData {
        public List<rahulstech.jfx.balancesheet.json.model.Account> accounts;
        public List<Person> people;
        public LocalDate startDate;
        public LocalDate endDate;
        public  boolean importAccounts = true;
        public boolean importCreditTransactions = true;
        public boolean importDebitTransactions = true;
        public boolean importTransfers = true;
    }

    private final DataModel model;
    private final FilterData filterData;
    private Map<Long,Account> map_accountId_account;

    public InsertTask(DataModel model, FilterData filterData) {
        this.model = model;
        this.filterData = filterData;
    }

    @Override
    protected Boolean call() throws Exception {
        DataModel model = this.model;
        List<Transaction> transactions = model.getTransactions();
        List<MoneyTransfer> moneyTransfers = model.getMoney_transfers();
        List<rahulstech.jfx.balancesheet.json.model.Account> selected_accounts = this.filterData.accounts;
        List<Person> selected_people = this.filterData.people;
        LocalDate startDate= this.filterData.startDate;
        LocalDate endDate = this.filterData.endDate;

        List<Account> accounts = convertTo_entity_Account(selected_accounts);
        map_accountId_account = createAccountIdAccountMap(accounts);
        List<TransactionHistory> histories = new ArrayList<>();
        if ((filterData.importCreditTransactions || filterData.importDebitTransactions) && null != transactions) {
            List<Transaction> filteredTransactions = filterTransactionsByNotDeleted(transactions);
            filteredTransactions = filterTransactionsByDateRange(filteredTransactions,startDate,endDate);
            filteredTransactions = filterTransactionsByAccounts(filteredTransactions,selected_accounts);
            filteredTransactions = filterTransactionsByPeople(filteredTransactions,selected_people);
            if (!filterData.importCreditTransactions) {
                filteredTransactions = filteredTransactions.stream().filter(t->t.getType()!=1).collect(Collectors.toList());
            }
            if (!filterData.importDebitTransactions){
                filteredTransactions = filteredTransactions.stream().filter(t->t.getType()!=0).collect(Collectors.toList());
            }
            for (Transaction t : filteredTransactions) {
                TransactionHistory h = convertTransaction(t);
                histories.add(h);
            }
        }
        if (filterData.importTransfers && null != moneyTransfers) {
            List<MoneyTransfer> filteredTransfers = filterTransferByDateRange(moneyTransfers,startDate,endDate);
            filteredTransfers = filterMoneyTransfersByAccounts(filteredTransfers,selected_accounts);
            for (MoneyTransfer mt : filteredTransfers) {
                TransactionHistory h = convertMoneyTransfer(mt);
                histories.add(h);
            }
        }

        BalancesheetDb db = BalancesheetDb.getInstance();
        return db.inTransaction(()->{
            boolean accountsInserted = !this.filterData.importAccounts || insertAccounts(db.getAccountDao(), accounts);
            boolean historiesInserted = insertTransactionHistories(db.getTransactionHistoryDao(), histories);
          if (accountsInserted && historiesInserted) {
              return true;
          }
          throw new SQLException("database insert fail");
        });
    }

    private List<Long> getAccountIds(List<rahulstech.jfx.balancesheet.json.model.Account> accounts) {
        if (null==accounts || accounts.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> accountIds = new ArrayList<>();
        for (rahulstech.jfx.balancesheet.json.model.Account account : accounts) {
            accountIds.add(account.get_id());
        }
        return accountIds;
    }

    private List<Transaction> filterTransactionsByNotDeleted(List<Transaction> transactions) {
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : transactions) {
            if (!t.isDeleted()) {
                filtered.add(t);
            }
        }
        return filtered;
    }

    private List<Transaction> filterTransactionsByAccounts(List<Transaction> transactions, List<rahulstech.jfx.balancesheet.json.model.Account> accounts) {
        List<Long> accountIds = getAccountIds(accounts);
        return transactions.stream().filter(t-> {
            return accountIds.contains(t.getAccount_id());
        }).collect(Collectors.toList());
    }

    private List<Transaction> filterTransactionsByPeople(List<Transaction> transactions, List<Person> people) {
        if (null==people || people.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> peopleIds = new ArrayList<>();
        for (Person person : people) {
            peopleIds.add(person.get_id());
        }
        return transactions.stream().filter(t-> peopleIds.contains(t.getPerson_id()))
                .collect(Collectors.toList());
    }

    private List<Transaction> filterTransactionsByDateRange(List<Transaction> transactions, LocalDate start, LocalDate end) {
        return transactions.stream().filter(t-> {
            LocalDate date = t.getDate();
            boolean inRange = isDateWithinRange(date,start,end);
            return inRange;
        }).collect(Collectors.toList());
    }

    private List<MoneyTransfer> filterMoneyTransfersByAccounts(List<MoneyTransfer> transfers, List<rahulstech.jfx.balancesheet.json.model.Account> accounts) {
        List<Long> accountIds = getAccountIds(accounts);
        return transfers.stream().filter(t->
            accountIds.contains(t.getPayee_account_id())
                    || accountIds.contains(t.getPayer_account_id()))
                    .collect(Collectors.toList());
    }

    private List<MoneyTransfer> filterTransferByDateRange(List<MoneyTransfer> transfers, LocalDate start, LocalDate end) {
        return transfers.stream().filter(t-> {
            LocalDate date = t.getWhen();
            boolean inRange = isDateWithinRange(date,start,end);
            return inRange;
        }).collect(Collectors.toList());
    }

    private boolean isDateWithinRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        boolean afterOrEqualStart = (startDate == null) || !date.isBefore(startDate);
        boolean beforeOrEqualEnd = (endDate == null) || !date.isAfter(endDate);

        return afterOrEqualStart && beforeOrEqualEnd;
    }

    private boolean insertAccounts(AccountDao accountDao, List<Account> accounts) {
        return accountDao.createMultipleAccount(accounts);
    }

    private boolean insertTransactionHistories(TransactionHistoryDao transactionHistoryDao, List<TransactionHistory> histories) {
        return transactionHistoryDao.createMultipleTransactionHistory(histories);
    }

    private List<Account> convertTo_entity_Account(List<rahulstech.jfx.balancesheet.json.model.Account> accounts) {
        if (null == accounts) {
            return Collections.emptyList();
        }
        List<Account> list = new ArrayList<>();
        for (rahulstech.jfx.balancesheet.json.model.Account ac : accounts) {
            Account account = new Account();
            account.setId(ac.get_id());
            account.setName(ac.getAccount_name());
            account.setBalance(Currency.from(ac.getBalance()));
            list.add(account);
        }
        return list;
    }

    private Map<Long,Account> createAccountIdAccountMap(List<Account> accounts) {
        if (null == accounts) {
            return Collections.emptyMap();
        }
        Map<Long,Account> map = new HashMap<>();
        for (Account ac : accounts) {
            map.put(ac.getId(),ac);
        }
        return map;
    }

    private TransactionHistory convertTransaction(Transaction transaction) {
        TransactionHistory transactionHistory = new TransactionHistory();

        // Set the type based on the transaction type
        if (transaction.getType() == 1) {
            transactionHistory.setType(TransactionType.DEPOSIT);
        } else if (transaction.getType() == 0) {
            transactionHistory.setType(TransactionType.WITHDRAW);
        }

        // Set the values from Transaction to TransactionHistory
        transactionHistory.setId(transaction.get_id());
        transactionHistory.setWhen(transaction.getDate());
        transactionHistory.setAmount(Currency.from(transaction.getAmount()));
        transactionHistory.setDescription(transaction.getDescription());

        // Get the account using account_id and set it as src in TransactionHistory
        Account account = getAccountById(transaction.getAccount_id());
        transactionHistory.setSrc(account);

        return transactionHistory;
    }

    private TransactionHistory convertMoneyTransfer(MoneyTransfer moneyTransfer) {
        TransactionHistory transactionHistory = new TransactionHistory();

        // Setting properties from MoneyTransfer to TransactionHistory
        transactionHistory.setId(moneyTransfer.getId());
        transactionHistory.setWhen(moneyTransfer.getWhen());
        transactionHistory.setAmount(Currency.from(moneyTransfer.getAmount()));
        transactionHistory.setDescription(moneyTransfer.getDescription());
        transactionHistory.setType(TransactionType.TRANSFER);

        // Fetching and setting Account objects
        Account srcAccount = getAccountById(moneyTransfer.getPayer_account_id());
        Account destAccount = getAccountById(moneyTransfer.getPayee_account_id());
        transactionHistory.setSrc(srcAccount);
        transactionHistory.setDest(destAccount);

        return transactionHistory;
    }

    private Account getAccountById(Long accountId) {
        return map_accountId_account.get(accountId);
    }

}
