package vn.techlifegroup.wesell.model;

/**
 * Created by toila on 29/04/2017.
 */

public class Cash {
    String cashFirst;
    String cashBill;
    String cashOut;
    String cashTotal;
    String timeStamp;
    String cashValue;
    String userPhone;
    String userUid;

    public Cash() {
    }

    public Cash(String cashOut, String timeStamp) {
        this.cashOut = cashOut;
        this.timeStamp = timeStamp;
    }

    public Cash(String cashFirst, String cashBill, String cashOut, String cashTotal) {
        this.cashFirst = cashFirst;
        this.cashBill = cashBill;
        this.cashOut = cashOut;
        this.cashTotal = cashTotal;
    }

    public String getCashFirst() {
        return cashFirst;
    }

    public String getCashBill() {
        return cashBill;
    }

    public String getCashTotal() {
        return cashTotal;
    }

    public String getCashOut() {
        return cashOut;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getCashValue() {
        return cashValue;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public String getUserUid() {
        return userUid;
    }
}
