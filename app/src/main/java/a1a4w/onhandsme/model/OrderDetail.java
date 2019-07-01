package a1a4w.onhandsme.model;

/**
 * Created by toila on 15/01/2017.
 */

public class OrderDetail {
    String clientCode;
    String orderName;
    String saleName;
    String clientType;
    String paymentType;
    String dateDelivery;
    String orderNote;
    String employeeCode;
    String orderType;
    String shopCode;
    String clientPayment;
    String billCode;
    String timeStamp;


    public OrderDetail() {
    }

    public OrderDetail(String orderName) {
        this.orderName = orderName;
    }

    public OrderDetail(String employeeCode, String shopCode, String clientPayment, String billCode, String timeStamp) {
        this.employeeCode = employeeCode;
        this.shopCode = shopCode;
        this.clientPayment = clientPayment;
        this.billCode = billCode;
        this.timeStamp = timeStamp;
    }


    public OrderDetail(String clientCode, String orderName, String saleName, String paymentType, String dateDelivery, String employeeCode,String orderNote) {
        this.clientCode = clientCode;
        this.orderName = orderName;
        this.saleName = saleName;
        this.paymentType = paymentType;
        this.dateDelivery = dateDelivery;
        this.employeeCode = employeeCode;
        this.orderNote = orderNote;
    }

    public String getOrderName() {
        return orderName;
    }

    public String getSaleName() {
        return saleName;
    }

    public String getClientType() {
        return clientType;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public String getDateDelivery() {
        return dateDelivery;
    }

    public String getClientCode() {
        return clientCode;
    }

    public String getOrderNote() {
        return orderNote;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getOrderType() {
        return orderType;
    }

    public String getShopCode() {
        return shopCode;
    }

    public String getClientPayment() {
        return clientPayment;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getBillCode() {
        return billCode;
    }
}
