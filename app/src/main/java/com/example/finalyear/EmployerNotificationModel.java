package com.example.finalyear;

public class EmployerNotificationModel {
    private final String docId;
    private final String type;          // "worker_interested" | "accepted" | "rejected"
    private final String workerName;
    private final String workerMobile;
    private final String jobId;         // used when type == worker_interested
    private final String inviteId;      // used when type == accepted/rejected
    private final String date;
    private final String from;
    private final String to;

    // Full constructor (recommended)
    public EmployerNotificationModel(String docId,
                                     String type,
                                     String workerName,
                                     String workerMobile,
                                     String jobId,
                                     String inviteId,
                                     String date,
                                     String from,
                                     String to) {
        this.docId = nz(docId);
        this.type = nz(type);
        this.workerName = nz(workerName);
        this.workerMobile = nz(workerMobile);
        this.jobId = nz(jobId);
        this.inviteId = nz(inviteId);
        this.date = nz(date);
        this.from = nz(from);
        this.to = nz(to);
    }

    // LEGACY 5-arg constructor to keep old call sites compiling:
    // (workerName, workerMobile, statusOrType, inviteId, docId)
    public EmployerNotificationModel(String workerName,
                                     String workerMobile,
                                     String statusOrType,
                                     String inviteId,
                                     String docId) {
        this(docId, statusOrType, workerName, workerMobile, "", inviteId, "", "", "");
    }

    // Getters
    public String getDocId()       { return docId; }
    public String getType()        { return type; }
    public String getWorkerName()  { return workerName; }
    public String getWorkerMobile(){ return workerMobile; }
    public String getJobId()       { return jobId; }
    public String getInviteId()    { return inviteId; }
    public String getDate()        { return date; }
    public String getFrom()        { return from; }
    public String getTo()          { return to; }

    private static String nz(String s) { return s == null ? "" : s; }
}


