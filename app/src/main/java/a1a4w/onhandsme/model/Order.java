package a1a4w.onhandsme.model;

/**
 * Created by toila on 07/01/2017.
 */

public class Order {
    OrderDetail OtherInformation;
    VatModel VAT;

    public Order() {
    }

    public OrderDetail getOtherInformation() {
        return OtherInformation;
    }



    public VatModel getVAT() {
        return VAT;
    }
}
