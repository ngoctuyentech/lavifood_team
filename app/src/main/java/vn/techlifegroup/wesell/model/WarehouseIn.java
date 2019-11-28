package vn.techlifegroup.wesell.model;

/**
 * Created by toila on 10/03/2017.
 */

public class WarehouseIn {
    String supplier;
    String productName;
    String productQuantity;
    String dateIn;
    String productStorageStart;
    String productStorageEnd;

    public WarehouseIn() {
    }

    public WarehouseIn(String supplier, String productName, String productQuantity, String dateIn) {
        this.supplier = supplier;
        this.productName = productName;
        this.productQuantity = productQuantity;
        this.dateIn = dateIn;
    }

    public WarehouseIn(String supplier, String productName, String productQuantity, String dateIn, String productStorageStart, String productStorageEnd) {
        this.supplier = supplier;
        this.productName = productName;
        this.productQuantity = productQuantity;
        this.dateIn = dateIn;
        this.productStorageStart = productStorageStart;
        this.productStorageEnd = productStorageEnd;
    }

    public String getDateIn() {
        return dateIn;
    }

    public String getSupplier() {
        return supplier;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductQuantity() {
        return productQuantity;
    }
}
