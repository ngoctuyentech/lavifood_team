package a1a4w.onhandsme.model;

/**
 * Created by toila on 08/06/2017.
 */

public class UserSetting {
    String userName;
    String userPhone;
    String userEmail;
    String userPass;
    String loginType;
    String dateBegin;

    public UserSetting() {
    }

    public UserSetting(String userName, String userPhone, String userEmail, String userPass, String dateBegin) {
        this.userName = userName;
        this.userPhone = userPhone;
        this.userEmail = userEmail;
        this.userPass = userPass;
        this.dateBegin = dateBegin;
    }

    public String getDateBegin() {
        return dateBegin;
    }
}
