package vn.techlifegroup.wesell.model;

public class Program {

    public Program() {
    }

    String programUrl;
    String programKey;
    String applied;
    String startDate;
    String endDate;
    String content;
    String programName;
    String timeStamp;
    String displayUrl;
    String userPhone;
    String userUid;


    public Program(String timeStamp, String displayUrl, String userPhone, String userUid) {
        this.timeStamp = timeStamp;
        this.displayUrl = displayUrl;
        this.userPhone = userPhone;
        this.userUid = userUid;
    }

    public Program(String programKey, String programName) {
        this.programKey = programKey;
        this.programName = programName;
    }

    public String getProgramUrl() {
        return programUrl;
    }

    public String getApplied() {
        return applied;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getContent() {
        return content;
    }

    public String getProgramName() {
        return programName;
    }

    public String getProgramKey() {
        return programKey;
    }
}
