package vn.techlifegroup.wesell.model;

/**
 * Created by toila on 27/04/2017.
 */

public class Bill {
    String customerCash;
    String cashBack;
    String billCode;
    String payment;
    String timeStamp;

    public Bill() {
    }

    public Bill(String customerCash, String cashBack, String billCode, String payment, String timeStamp) {
        this.customerCash = customerCash;
        this.cashBack = cashBack;
        this.billCode = billCode;
        this.payment = payment;
        this.timeStamp = timeStamp;
    }

    public Bill(String customerCash, String cashBack) {
        this.customerCash = customerCash;
        this.cashBack = cashBack;
    }

    public Bill(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getCustomerCash() {
        return customerCash;
    }

    public String getCashBack() {
        return cashBack;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getBillCode() {
        return billCode;
    }

    public String getPayment() {
        return payment;
    }
}
