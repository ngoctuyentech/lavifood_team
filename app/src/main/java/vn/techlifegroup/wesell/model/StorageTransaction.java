package vn.techlifegroup.wesell.model;

/**
 * Created by toila on 27/03/2017.
 */

public class StorageTransaction {
    String productName;
    String productIn;
    String productOut;
    String productStorage;

    public StorageTransaction() {
    }

    public StorageTransaction(String productName, String productIn, String productOut, String productStorage) {
        this.productName = productName;
        this.productIn = productIn;
        this.productOut = productOut;
        this.productStorage = productStorage;
    }

    public StorageTransaction(String productIn, String productOut, String productStorage) {
        this.productIn = productIn;
        this.productOut = productOut;
        this.productStorage = productStorage;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductIn() {
        return productIn;
    }

    public String getProductOut() {
        return productOut;
    }

    public String getProductStorage() {
        return productStorage;
    }
}
