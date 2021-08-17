package com.example.cloneinstagram.model;

import java.io.Serializable;

public class CommentMessage implements Serializable {

    private String userId, userName, userxPhoto, message;

    public CommentMessage() {
    }

    public String getUserName() {        return userName;    }

    public void setUserName(String userName) {        this.userName = userName;    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserxPhoto() {
        return userxPhoto;
    }

    public void setUserxPhoto(String userxPhoto) {
        this.userxPhoto = userxPhoto;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
