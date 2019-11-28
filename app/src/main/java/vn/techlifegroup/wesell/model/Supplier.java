package vn.techlifegroup.wesell.model;

/**
 * Created by toila on 14/03/2017.
 */

public class Supplier {
    String supplierName;
    String supplierProduct;

    public Supplier() {
    }

    public Supplier(String supplierName, String supplierProduct) {
        this.supplierName = supplierName;
        this.supplierProduct = supplierProduct;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getSupplierProduct() {
        return supplierProduct;
    }
}
