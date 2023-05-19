package com.devtwist.mymessenger.Models;

public class CallListData {
    private String callId, userId, type, date, time;
    private double timeStamp;

    public CallListData() {
    }

    public CallListData(String callId, String userId, String type, String date, String time, double timeStamp) {
        this.callId = callId;
        this.userId = userId;
        this.type = type;
        this.date = date;
        this.time = time;
        this.timeStamp = timeStamp;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(double timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
