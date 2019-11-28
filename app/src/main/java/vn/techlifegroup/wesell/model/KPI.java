package vn.techlifegroup.wesell.model;

public class KPI {

    private String kpiTime;
    private String kpiType;
    private String kpiTarget;
    private String kpiReach;

    public KPI() {
    }

    public KPI(String kpiTime, String kpiType, String kpiTarget) {
        this.kpiTime = kpiTime;
        this.kpiType = kpiType;
        this.kpiTarget = kpiTarget;
    }

    public String getKpiTime() {
        return kpiTime;
    }

    public String getKpiType() {
        return kpiType;
    }

    public String getKpiTarget() {
        return kpiTarget;
    }

    public String getKpiReach() {
        return kpiReach;
    }
}