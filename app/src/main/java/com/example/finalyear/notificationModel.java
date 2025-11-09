package com.example.finalyear;

public class notificationModel {
    private final String employerName;
    private final String employerMobile;
    private final String date;
    private final String fromTime;
    private final String toTime;
    private final String docId;
    private String status; // pending | accepted | rejected (mutable for local UI update)

    public notificationModel(String employerName, String employerMobile,
                             String date, String fromTime, String toTime,
                             String docId, String status) {
        this.employerName = employerName;
        this.employerMobile = employerMobile;
        this.date = date;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.docId = docId;
        this.status = status;
    }

    public String getEmployerName() { return employerName; }
    public String getEmployerMobile() { return employerMobile; }
    public String getDate() { return date; }
    public String getFromTime() { return fromTime; }
    public String getToTime() { return toTime; }
    public String getDocId() { return docId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}


