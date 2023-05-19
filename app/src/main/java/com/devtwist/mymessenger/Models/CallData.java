package com.devtwist.mymessenger.Models;

public class CallData {
    private String callStatus, type, senderId, receiverId, connId;

    public CallData(String callStatus, String type, String senderId, String receiverId, String connId) {
        this.callStatus = callStatus;
        this.type = type;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.connId = connId;
    }

    public CallData() {
    }

    public String getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getConnId() {
        return connId;
    }

    public void setConnId(String connId) {
        this.connId = connId;
    }

}
