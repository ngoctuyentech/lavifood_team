package a1a4w.onhandsme.model;

/**
 * Created by toila on 07/01/2017.
 */

public class Client {
    String clientCode;
    String clientName;
    String clientType;
    String clientStreet;
    String clientDistrict;
    String clientCity;
    String clientProvince;
    String clientPhone;
    String clientDeliveryName;
    String clientOrderInform;
    String clientDebt;
    String clientSale;
    String clientAddress;
    MapModel map;
    String timeStamp;
    String clientUrl;
    boolean isMet;
    String monthSale;
    String nearestBuyDay;
    String createBy;
    String contactName;

    public Client() {
    }

    public Client(String clientCode, String clientName, String clientType,
                  String clientStreet, String clientDistrict, String clientCity,
                  String clientProvince, String clientPhone,
                  String clientOrderInform, String clientDebt, String clientSale,MapModel map,String createBy,String contactName) {

        this.clientCode = clientCode;
        this.clientName = clientName;
        this.clientType = clientType;
        this.clientStreet = clientStreet;
        this.clientDistrict = clientDistrict;
        this.clientCity = clientCity;
        this.clientProvince = clientProvince;
        this.clientPhone = clientPhone;

        this.clientOrderInform = clientOrderInform;
        this.clientDebt = clientDebt;
        this.clientSale = clientSale;
        this.map = map;
        this.createBy = createBy;
        this.contactName = contactName;

    }

    public Client(String clientCode, String clientName, String clientType) {
        this.clientCode = clientCode;
        this.clientName = clientName;
        this.clientType = clientType;
    }

    public Client(String clientCode, String clientName, MapModel map, String clientStreet) {
        this.clientCode = clientCode;
        this.clientName = clientName;
        this.map = map;
        this.clientStreet = clientStreet;
    }

    public Client(String clientCode, String clientName, String clientType, String clientAddress, String timeStamp) {
        this.clientCode = clientCode;
        this.clientName = clientName;
        this.clientType = clientType;
        this.clientAddress = clientAddress;
        this.timeStamp = timeStamp;
    }

    public String getClientCode() {
        return clientCode;
    }

    public String getClientName() {
        return clientName;
    }

    public String getClientType() {
        return clientType;
    }

    public String getClientStreet() {
        return clientStreet;
    }

    public String getClientDistrict() {
        return clientDistrict;
    }

    public String getClientCity() {
        return clientCity;
    }

    public String getClientProvince() {
        return clientProvince;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public String getClientDeliveryName() {
        return clientDeliveryName;
    }

    public String getClientOrderInform() {
        return clientOrderInform;
    }

    public String getClientDebt() {
        return clientDebt;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public MapModel getMap() {
        return map;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getClientUrl() {
        return clientUrl;
    }

    public boolean isMet() {
        return isMet;
    }

    public String getMonthSale() {
        return monthSale;
    }

    public String getNearestBuyDay() {
        return nearestBuyDay;
    }

    public String getClientSale() {
        return clientSale;
    }

    public String getCreateBy() {
        return createBy;
    }
}
