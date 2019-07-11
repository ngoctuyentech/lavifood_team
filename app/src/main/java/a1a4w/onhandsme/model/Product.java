package a1a4w.onhandsme.model;

/**
 * Created by toila on 14/01/2017.
 */

public class Product {
    String productName;
    String unitPrice;
    String unitQuantity;
    String unitName;
    Product promotion;
    String productVAT;
    String finalPayment;
    String productUrl;
    String productOrder;
    String productTotal;
    String nameNotViet;
    String productCode;
    String productDiscount;

    public Product() {
    }

    public Product(String productName) {
        this.productName = productName;
    }

    public Product(String productName, String unitPrice, String unitName, String product) {
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.unitName = unitName;
        this.productCode = productCode;
    }

    public Product(String productName, String productDiscount) {
        this.productName = productName;
        this.productDiscount = productDiscount;
    }

    public Product(String productName, String unitQuantity, String nameNotViet) {
        this.productName = productName;
        this.unitQuantity = unitQuantity;
        this.nameNotViet = nameNotViet;
    }

    public Product(String productName, String unitPrice, String unitQuantity, String productOrder, String productTotal, String nameNotViet) {
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.unitQuantity = unitQuantity;
        this.productOrder = productOrder;
        this.productTotal = productTotal;
        this.nameNotViet = nameNotViet;
    }

    public Product(String productName, String unitPrice, String unitQuantity, String productCode, String finalPayment) {
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.unitQuantity = unitQuantity;
        this.productCode = productCode;
        this.finalPayment = finalPayment;
    }

    public String getProductName() {
        return productName;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public String getUnitQuantity() {
        return unitQuantity;
    }

    public Product getPromotion() {
        return promotion;
    }

    public String getProductVAT() {
        return productVAT;
    }

    public String getFinalPayment() {
        return finalPayment;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public String getUnitName() {
        return unitName;
    }

    public String getProductOrder() {
        return productOrder;
    }

    public String getProductTotal() {
        return productTotal;
    }

    public String getNameNotViet() {
        return nameNotViet;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductDiscount() {
        return productDiscount;
    }
}
