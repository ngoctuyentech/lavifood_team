package a1a4w.onhandsme.model;

/**
 * Created by toila on 20/03/2017.
 */

public class Employee {
    String employeeCode;
    String employeeName;
    String employeePhone;
    String employeeUrl;
    String employeeEmail;
    String employeeRole;
    String shopCode;
    String employeeShift;
    String dateOfWork;


    public Employee() {
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
}
