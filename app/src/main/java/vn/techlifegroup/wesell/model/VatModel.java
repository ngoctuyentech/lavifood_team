package vn.techlifegroup.wesell.model;

/**
 * Created by toila on 21/01/2017.
 */

public class VatModel {
    float notVat;
    float includedVat;

    float finalPayment;
    String discount;


    public VatModel() {
    }

    public VatModel(float notVat, float includedVat, float finalPayment) {
        this.notVat = notVat;
        this.includedVat = includedVat;
        this.finalPayment = finalPayment;
    }

    public float getNotVat() {
        return notVat;
    }

    public float getIncludedVat() {
        return includedVat;
    }

    public float getFinalPayment() {
        return finalPayment;
    }

    public String getDiscount() {
        return discount;
    }
}

