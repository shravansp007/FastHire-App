package com.example.finalyear;

public class JobFeedModel {
    private final String jobId;
    private final String employerName;
    private final String employerMobile;
    private final String date;
    private final String from;
    private final String to;
    private String interest; // "pending" | "interested" | "not_interested"

    public JobFeedModel(String jobId, String employerName, String employerMobile,
                        String date, String from, String to, String interest) {
        this.jobId = jobId;
        this.employerName = employerName;
        this.employerMobile = employerMobile;
        this.date = date;
        this.from = from;
        this.to = to;
        this.interest = interest;
    }

    public String getJobId() { return jobId; }
    public String getEmployerName() { return employerName; }
    public String getEmployerMobile() { return employerMobile; }
    public String getDate() { return date; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getInterest() { return interest; }
    public void setInterest(String interest) { this.interest = interest; }
}
