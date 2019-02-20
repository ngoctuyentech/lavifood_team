package a1a4w.onhandsme.model;

/**
 * Created by toila on 16/01/2017.
 */

public class OrderStatus {
    String approvedByDelivery;
    String approvedByWarehouse;
    String approvedByDistribution;
    String approvedByDebt;

    public OrderStatus() {
    }

    public OrderStatus(String approvedByDelivery, String approvedByWarehouse, String approvedByDistribution, String approvedByDebt) {
        this.approvedByDelivery = approvedByDelivery;
        this.approvedByWarehouse = approvedByWarehouse;
        this.approvedByDistribution = approvedByDistribution;
        this.approvedByDebt = approvedByDebt;
    }

    public String getApprovedByDelivery() {
        return approvedByDelivery;
    }

    public String getApprovedByWarehouse() {
        return approvedByWarehouse;
    }

    public String getApprovedByDistribution() {
        return approvedByDistribution;
    }

    public String getApprovedByDebt() {
        return approvedByDebt;
    }
}
