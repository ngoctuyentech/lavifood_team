package a1a4w.onhandsme.model;

/**
 * Created by toila on 19/04/2017.
 */

public class Shop {
    String shopName;
    String shopNameNotViet;
    String shopPhone;
    String shopAddress;
    String shopUrl;

    public Shop() {
    }

    public Shop(String shopName, String shopNameNotViet, String shopPhone) {
        this.shopName = shopName;
        this.shopNameNotViet = shopNameNotViet;
        this.shopPhone = shopPhone;
    }

    public Shop(String shopName, String shopPhone, String shopAddress, String shopUrl) {
        this.shopName = shopName;
        this.shopPhone = shopPhone;
        this.shopAddress = shopAddress;
        this.shopUrl = shopUrl;
    }

    public String getShopName() {
        return shopName;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public String getShopUrl() {
        return shopUrl;
    }

    public String getShopNameNotViet() {
        return shopNameNotViet;
    }
}
