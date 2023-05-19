package com.devtwist.mymessenger.Models;

public class ChatListData  {
    private String userId,lastMessage;
    private double timeStamp;

    public ChatListData() {
    }

    public ChatListData(String userId, String lastMessage, double timeStamp) {
        this.userId = userId;
        this.lastMessage = lastMessage;
        this.timeStamp = timeStamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(double timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
