package org.teamchat.Model;

import java.io.Serializable;

public class Message implements Serializable {
    String idpage, idchat, type, idmessage, message, createdAt;
    User user;

    public Message() {
    }

    public Message(String idpage, String idchat, String idmessage, String message, String createdAt, User user) {
        this.idpage = idpage;
        this.idchat = idchat;
        this.idmessage = idmessage;
        this.message = message;
        this.createdAt = createdAt;
        this.user = user;
    }

    public String getIdPage() {
        return idpage;
    }
    public void setIdPage(String id) {
        this.idpage = id;
    }

    public String getIdChat() {
        return idchat;
    }
    public void setIdChat(String id) {
        this.idchat = id;
    }

    public String getId() {
        return idmessage;
    }
    public void setId(String id) {
        this.idmessage = id;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public String getType(){return type;}
    public void setType(String t){type=t;}
}
