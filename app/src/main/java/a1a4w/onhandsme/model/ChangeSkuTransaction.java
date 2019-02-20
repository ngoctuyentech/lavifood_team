package a1a4w.onhandsme.model;

/**
 * Created by toila on 23/03/2017.
 */

public class ChangeSkuTransaction {
    String sourceProduct;
    String desProduct;
    String sourceQuantity;
    String sourceStorage;
    String desStorage;
    String changeRate;
    String resultSourceProduct;
    String resultDesProduct;
    String timeStamp;

    public ChangeSkuTransaction() {
    }

    public ChangeSkuTransaction(String sourceProduct, String desProduct, String sourceQuantity, String sourceStorage, String desStorage, String changeRate, String resultSourceProduct, String resultDesProduct, String timeStamp) {
        this.sourceProduct = sourceProduct;
        this.desProduct = desProduct;
        this.sourceQuantity = sourceQuantity;
        this.sourceStorage = sourceStorage;
        this.desStorage = desStorage;
        this.changeRate = changeRate;
        this.resultSourceProduct = resultSourceProduct;
        this.resultDesProduct = resultDesProduct;
        this.timeStamp = timeStamp;
    }

    public String getSourceProduct() {
        return sourceProduct;
    }

    public String getDesProduct() {
        return desProduct;
    }

    public String getChangeRate() {
        return changeRate;
    }

    public String getResultSourceProduct() {
        return resultSourceProduct;
    }

    public String getResultDesProduct() {
        return resultDesProduct;
    }

    public String getSourceStorage() {
        return sourceStorage;
    }

    public String getDesStorage() {
        return desStorage;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getSourceQuantity() {
        return sourceQuantity;
    }
}
