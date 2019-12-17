package vn.techlifegroup.wesell.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by toila on 07/01/2017.
 */

public class Order {
    OrderDetail OtherInformation;
    VatModel VAT;

    String clientCode;
    String dateDelivery;
    String employeeCode;
    String finalPayment;
    String timeStamp;
    String orderName;
    String orderNote;
    String paymentType;
    String saleName;
    String discount;

    public Order() {
    }

    public Order(String finalPayment, String timeStamp, String orderName) {
        this.finalPayment = finalPayment;
        this.timeStamp = timeStamp;
        this.orderName = orderName;
    }

    public Order(String clientCode, String dateDelivery, String employeeCode, String finalPayment, String timeStamp, String orderName, String orderNote, String paymentType, String saleName, String discount) {
        this.clientCode = clientCode;
        this.dateDelivery = dateDelivery;
        this.employeeCode = employeeCode;
        this.finalPayment = finalPayment;
        this.timeStamp = timeStamp;
        this.orderName = orderName;
        this.orderNote = orderNote;
        this.paymentType = paymentType;
        this.saleName = saleName;
        this.discount = discount;
    }

    public OrderDetail getOtherInformation() {
        return OtherInformation;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("clientCode", clientCode);
        result.put("dateDelivery", dateDelivery);
        result.put("employeeCode", employeeCode);
        result.put("finalPayment", finalPayment);
        result.put("timeStamp", timeStamp);
        result.put("orderName", orderName);
        result.put("orderNote", orderNote);
        result.put("paymentType", paymentType);
        result.put("saleName", saleName);
        result.put("discount", discount);

        return result;
    }



    public VatModel getVAT() {
        return VAT;
    }
}
