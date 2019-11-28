package vn.techlifegroup.wesell.model;

/**
 * Created by toila on 07/04/2017.
 */

public class DebtHistory {
    String clientName;
    String currentDebt;
    String newDebt;
    String clientRepay;
    String updateDebt;
    String timeStamp;

    public DebtHistory() {
    }

    public DebtHistory(String clientName, String currentDebt, String newDebt, String clientRepay, String updateDebt) {
        this.clientName = clientName;
        this.currentDebt = currentDebt;
        this.newDebt = newDebt;
        this.clientRepay = clientRepay;
        this.updateDebt = updateDebt;
    }


    public String getCurrentDebt() {
        return currentDebt;
    }

    public String getNewDebt() {
        return newDebt;
    }

    public String getUpdateDebt() {
        return updateDebt;
    }

    public String getClientName() {
        return clientName;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getClientRepay() {
        return clientRepay;
    }
}
