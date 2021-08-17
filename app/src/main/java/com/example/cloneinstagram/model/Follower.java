package com.example.cloneinstagram.model;

import java.io.Serializable;

public class Follower implements Serializable {

    String userId, userName, userXPhoto;

    public Follower() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserXPhoto() {
        return userXPhoto;
    }

    public void setUserXPhoto(String userXPhoto) {
        this.userXPhoto = userXPhoto;
    }
}
