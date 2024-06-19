package rahulstech.jfx.balancesheet.concurrent;

import javafx.concurrent.Task;
import rahulstech.jfx.balancesheet.database.BalancesheetDb;
import rahulstech.jfx.balancesheet.database.dao.AccountDao;
import rahulstech.jfx.balancesheet.database.dao.TransactionHistoryDao;
import rahulstech.jfx.balancesheet.database.entity.Account;
import rahulstech.jfx.balancesheet.database.entity.TransactionHistory;
import rahulstech.jfx.balancesheet.database.exception.DatabaseException;
import rahulstech.jfx.balancesheet.database.type.Currency;
import rahulstech.jfx.balancesheet.database.type.TransactionType;
import rahulstech.jfx.balancesheet.json.model.DataModel;
import rahulstech.jfx.balancesheet.json.model.MoneyTransfer;
import rahulstech.jfx.balancesheet.json.model.Person;
import rahulstech.jfx.balancesheet.json.model.Transaction;

import java.util.*;

public class InsertTask extends Task<Boolean> {

    private final DataModel model;
    private final List<rahulstech.jfx.balancesheet.json.model.Account> selectedAccounts;
    private final List<Person> selectedPeople;

    private Map<Long,Account> map_accountId_account;

    public InsertTask(DataModel model, List<rahulstech.jfx.balancesheet.json.model.Account> accounts, List<Person> people) {
        this.model = model;
        this.selectedAccounts = accounts;
        this.selectedPeople = people;
    }

    @Override
    protected Boolean call() throws Exception {
        DataModel model = this.model;
        List<rahulstech.jfx.balancesheet.json.model.Account> json_account = null == selectedAccounts ? model.getAccounts() : selectedAccounts;
        List<Transaction> transactions = model.getTransactions();
        List<Person> selected_people = this.selectedPeople;
        boolean isAllPeopleSelected = selected_people == null || selected_people.isEmpty();
        List<MoneyTransfer> moneyTransfers = model.getMoney_transfers();

        List<Account> accounts = convertTo_entity_Account(json_account);
        map_accountId_account = createAccountIdAccountMap(accounts);
        List<TransactionHistory> histories = new ArrayList<>();
        if (null != transactions) {
            for (Transaction t : transactions) {
                if (t.isDeleted()) {
                    continue;
                }
                if (!isAllPeopleSelected) {
                    boolean matched = false;
                    for (Person p : selected_people) {
                        if (Objects.equals(t.getPerson_id(), p.get_id())) {
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) {
                        continue;
                    }
                }
                TransactionHistory h = convertTransaction(t);
                histories.add(h);
            }
        }
        if (null != moneyTransfers) {
            for (MoneyTransfer mt : moneyTransfers) {
                TransactionHistory h = convertMoneyTransfer(mt);
                histories.add(h);
            }
        }

        BalancesheetDb db = BalancesheetDb.getInstance();
        return db.inTransaction(()->{
          if (insertAccounts(db.getAccountDao(),accounts)
                  && insertTransactionHistories(db.getTransactionHistoryDao(),histories)) {
              return true;
          }
          throw new DatabaseException("database insert fail");
        });
    }

    private boolean insertAccounts(AccountDao accountDao, List<Account> accounts) {
        return accountDao.createMultipleAccount(accounts);
    }

    private boolean insertTransactionHistories(TransactionHistoryDao transactionHistoryDao, List<TransactionHistory> histories) {
        System.out.println("adding histories="+histories.size());
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
