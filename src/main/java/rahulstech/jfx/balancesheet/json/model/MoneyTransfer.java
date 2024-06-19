package rahulstech.jfx.balancesheet.json.model;

import java.time.LocalDate;

public class MoneyTransfer {
    private String amount;
    private String description;
    private long id;
    private Long payee_account_id;
    private Long payer_account_id;
    private LocalDate when;

    public Long getPayee_account_id() {
        return payee_account_id;
    }

    public void setPayee_account_id(Long payee_account_id) {
        this.payee_account_id = payee_account_id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getPayer_account_id() {
        return payer_account_id;
    }

    public void setPayer_account_id(Long payer_account_id) {
        this.payer_account_id = payer_account_id;
    }

    public LocalDate getWhen() {
        return when;
    }

    public void setWhen(LocalDate when) {
        this.when = when;
    }
}

