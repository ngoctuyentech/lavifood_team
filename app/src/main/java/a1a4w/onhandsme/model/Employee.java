package a1a4w.onhandsme.model;

/**
 * Created by toila on 20/03/2017.
 */

public class Employee {
    String employeeCode;
    String employeeName;
    String employeeAddress;
    String employeePhone;
    String employeeUrl;
    String employeeEmail;
    String employeePass;
    String employeeRole;
    String shopCode;
    String employeeShift;
    String dateOfWork;
    String menuOrderName;
    String menuOrderUrl;
    String visitTime;
    String visitClientCode;
    String visitClientName;
    String employeeMonthSale;
    String nearestSellDay;
    String managedBy;

    public Employee() {
    }

    public Employee(String employeeName, String employeeEmail) {
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
    }

    public Employee(String employeeName, String employeeAddress, String employeePhone, String employeeEmail, String employeePass, String managedBy) {
        this.employeeName = employeeName;
        this.employeeAddress = employeeAddress;
        this.employeePhone = employeePhone;
        this.employeeEmail = employeeEmail;
        this.employeePass = employeePass;
        this.managedBy = managedBy;
    }

    public Employee(String visitTime, String visitClientCode, String visitClientName) {
        this.visitTime = visitTime;
        this.visitClientCode = visitClientCode;
        this.visitClientName = visitClientName;
    }

    public Employee(String employeeCode, String employeeName, String employeePhone, String employeeUrl, String employeeEmail, String employeeRole, String shopCode) {
        this.employeeCode = employeeCode;
        this.employeeName = employeeName;
        this.employeePhone = employeePhone;
        this.employeeUrl = employeeUrl;
        this.employeeEmail = employeeEmail;
        this.employeeRole = employeeRole;
        this.shopCode = shopCode;
    }

    public Employee(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getShopCode() {
        return shopCode;
    }

    public String getEmployeeShift() {
        return employeeShift;
    }

    public String getDateOfWork() {
        return dateOfWork;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getEmployeePhone() {
        return employeePhone;
    }

    public String getEmployeeUrl() {
        return employeeUrl;
    }

    public String getMenuOrderName() {
        return menuOrderName;
    }

    public String getMenuOrderUrl() {
        return menuOrderUrl;
    }

    public String getVisitClientCode() {
        return visitClientCode;
    }

    public String getVisitTime() {
        return visitTime;
    }

    public String getVisitClientName() {
        return visitClientName;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public String getEmployeeMonthSale() {
        return employeeMonthSale;
    }

    public String getNearestSellDay() {
        return nearestSellDay;
    }

    public String getManagedBy() {
        return managedBy;
    }

    public String getEmployeePass() {
        return employeePass;
    }

    public String getEmployeeAddress() {
        return employeeAddress;
    }
}

