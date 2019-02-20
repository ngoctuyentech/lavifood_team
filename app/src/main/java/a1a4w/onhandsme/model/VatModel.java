package a1a4w.onhandsme.model;

/**
 * Created by toila on 21/01/2017.
 */

public class VatModel {
    String notVat;
    String includedVat;
    String notVatDiscount;
    String includedVatDiscount;
    String finalPayment;

    public VatModel() {
    }



    public VatModel(String notVat, String includedVat, String notVatDiscount, String includedVatDiscount, String finalPayment) {
        this.notVat = notVat;
        this.includedVat = includedVat;
        this.notVatDiscount = notVatDiscount;
        this.includedVatDiscount = includedVatDiscount;
        this.finalPayment = finalPayment;
    }

    public String getNotVat() {
        return notVat;
    }

    public String getIncludedVat() {
        return includedVat;
    }

    public String getNotVatDiscount() {
        return notVatDiscount;
    }

    public String getIncludedVatDiscount() {
        return includedVatDiscount;
    }


    public String getFinalPayment() {
        return finalPayment;
    }
}
