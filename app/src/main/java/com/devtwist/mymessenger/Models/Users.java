package com.devtwist.mymessenger.Models;

public class Users {

    private String username, phoneNumber, userId, profilephotoURL;

    public  Users(){}
    public Users(String username, String phoneNumber, String userId, String profilephotoURL) {
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.userId = userId;
        this.profilephotoURL = profilephotoURL;
    }

    public String getUsername() {
        return username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getUserId() {
        return userId;
    }

    public String getProfilephotoURL() {
        return profilephotoURL;
    }

}
