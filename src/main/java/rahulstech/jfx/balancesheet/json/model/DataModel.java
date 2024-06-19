package rahulstech.jfx.balancesheet.json.model;

import java.util.List;

public class DataModel {
    private List<Account> accounts;
    private List<Person> people;
    private List<Transaction> transactions;
    private List<MoneyTransfer> money_transfers;

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public List<Person> getPeople() {
        return people;
    }

    public void setPeople(List<Person> people) {
        this.people = people;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<MoneyTransfer> getMoney_transfers() {
        return money_transfers;
    }

    public void setMoney_transfers(List<MoneyTransfer> money_transfers) {
        this.money_transfers = money_transfers;
    }
}