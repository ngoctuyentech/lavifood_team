package vn.techlifegroup.wesell.model;

/**
 * Created by toila on 21/01/2017.
 */

public class VatModel {
    String notVat;
    String includedVat;


    String finalPayment;
    String discount;


    public VatModel() {
    }

    public VatModel(String includedVat, String finalPayment, String discount) {
        this.includedVat = includedVat;
        this.finalPayment = finalPayment;
        this.discount = discount;
    }

    public String getIncludedVat() {
        return includedVat;
    }

    public String getFinalPayment() {
        return finalPayment;
    }

    public String getDiscount() {
        return discount;
    }
}

