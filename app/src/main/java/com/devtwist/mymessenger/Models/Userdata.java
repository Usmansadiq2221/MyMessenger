package com.devtwist.mymessenger.Models;

public class Userdata {
    private String userId, username, profileUrl, token;

    public Userdata() {
    }

    public Userdata(String userId, String username, String profileUrl, String token) {
        this.userId = userId;
        this.username = username;
        this.profileUrl = profileUrl;
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
