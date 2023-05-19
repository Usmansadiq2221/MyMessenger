package com.devtwist.mymessenger.Models;

public class FriendsData {
    private String userId, username;
    private boolean isBlocked;

    public FriendsData() {
    }

    public FriendsData(String userId, boolean isBlocked, String username) {
        this.userId = userId;
        this.isBlocked = isBlocked;
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
