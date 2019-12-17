package vn.techlifegroup.wesell.model;

public class ChatHistoryModel {

    public ChatHistoryModel() {
    }

    public boolean isRead;

    //public String isRead;


    public String id;
    public String timeStamp;

    private String lastContent;
    private String lastTimeStamp;

    private String imageFr;
    private String nameFr;
    private String roleFr;

    public ChatHistoryModel(String id, String timeStamp) {
        this.id = id;
        this.timeStamp = timeStamp;
    }

    public ChatHistoryModel(boolean isRead, String lastContent, String lastTimeStamp) {
        this.isRead = isRead;
        this.lastContent = lastContent;
        this.lastTimeStamp = lastTimeStamp;
    }

    public String getId() {
        return id;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
    public String getLastContent() {
        return lastContent;
    }

    public String getLastTimeStamp() {
        return lastTimeStamp;
    }

    public String getImageFr() {
        return imageFr;
    }

    public String getNameFr() {
        return nameFr;
    }

    public String getRoleFr() {
        return roleFr;
    }

    public boolean isRead() {
        return isRead;
    }

}
