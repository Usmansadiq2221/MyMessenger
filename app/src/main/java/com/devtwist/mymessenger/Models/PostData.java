package com.devtwist.mymessenger.Models;

public class PostData {

    private String postId, postTime, postDate, postText, postImageUrl, userId;

    public PostData() {
    }

    public PostData(String postId, String postTime, String postDate, String postText, String postImageUrl, String userId) {
        this.postId = postId;
        this.postTime = postTime;
        this.postDate = postDate;
        this.postText = postText;
        this.postImageUrl = postImageUrl;
        this.userId = userId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public String getPostImageUrl() {
        return postImageUrl;
    }

    public void setPostImageUrl(String postImageUrl) {
        this.postImageUrl = postImageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
