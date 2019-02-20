package a1a4w.onhandsme.model;

/**
 * Created by toila on 09/03/2017.
 */

public class CashTransfer {
    String clientName;
    String cashValue;
    String account;
    String dateOfTransfer;

    public CashTransfer() {
    }

    public CashTransfer(String clientName, String cashValue, String account, String dateOfTransfer) {
        this.clientName = clientName;
        this.cashValue = cashValue;
        this.account = account;
        this.dateOfTransfer = dateOfTransfer;
    }

    public String getClientName() {
        return clientName;
    }

    public String getCashValue() {
        return cashValue;
    }

    public String getAccount() {
        return account;
    }

    public String getDateOfTransfer() {
        return dateOfTransfer;
    }
}
