package vn.techlifegroup.wesell.model;

public class vatTest {

    String discount;
    String includedVat;
    String finalPayment;
    String timeStamp;

    public vatTest() {
    }

    public vatTest(String discount, String includedVat, String finalPayment, String timeStamp) {
        this.discount = discount;
        this.includedVat = includedVat;
        this.finalPayment = finalPayment;
        this.timeStamp = timeStamp;
    }

    public String getDiscount() {
        return discount;
    }

    public String getIncludedVat() {
        return includedVat;
    }

    public String getFinalPayment() {
        return finalPayment;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
}
