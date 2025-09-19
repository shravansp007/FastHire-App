package com.example.finalyear;

public class notificationModel {
    private String employerName;
    private String employerMobile;
    private String date;
    private String fromTime;
    private String toTime;
    private String docId;

    // Full constructor
    public notificationModel(String employerName, String employerMobile,
                             String date, String fromTime, String toTime, String docId) {
        this.employerName = employerName;
        this.employerMobile = employerMobile;
        this.date = date;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.docId = docId;
    }

    // Getters
    public String getEmployerName() { return employerName; }
    public String getEmployerMobile() { return employerMobile; }
    public String getDate() { return date; }
    public String getFromTime() { return fromTime; }
    public String getToTime() { return toTime; }
    public String getDocId() { return docId; }
}

