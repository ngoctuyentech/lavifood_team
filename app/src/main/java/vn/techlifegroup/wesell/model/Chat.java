package vn.techlifegroup.wesell.model;

public class Chat{

    public Chat() {
    }


    private String isDate;

    public String id;
    private String content;
    private String timeStamp;



    private String nameFr;
    private String imageFr;
    private String roleFr;


    public Chat(String isDate, String timeStamp) {
        this.isDate = isDate;
        this.timeStamp = timeStamp;
    }

    public Chat(String id, String content, String timeStamp) {
        this.id = id;
        this.content = content;
        this.timeStamp = timeStamp;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getIsDate() {
        return isDate;
    }

    public String getNameFr() {
        return nameFr;
    }

    public String getImageFr() {
        return imageFr;
    }

    public String getRoleFr() {
        return roleFr;
    }
}
