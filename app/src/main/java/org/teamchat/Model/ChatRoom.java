package org.teamchat.Model;

import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Johnny
 */
public class ChatRoom implements Serializable {
    String idpage, id, name, lastMessage, timestamp, type, author, PRIVATE;
    int unreadCount;

    public ChatRoom() {
    }

    public ChatRoom(String idpage, String id, String name, String lastMessage, String timestamp, int unreadCount, String type) {
        this.idpage = idpage;
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.unreadCount = unreadCount;
        this.type = type;
    }

    public String getIdPage() {
        return idpage;
    }

    public void setIdPages(String id) {
        this.idpage = id;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getLastMessage() {
        return lastMessage;
    }
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getUnreadCount() {
        return unreadCount;
    }
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void setType(String t){
        this.type = t;
    }
    public String getType(){
        return this.type;
    }

    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getAuthor(){return author;}
    public void setAuthor(String author){this.author = author;}

    public String getPRIVATE(){return this.PRIVATE;}
    public void setPRIVATE(String p){this.PRIVATE = p;}


    @Nullable
    public static ChatRoom getChatRoom(String idpage, String id, String type, ArrayList<ChatRoom> chatRooms){
        for(ChatRoom p : chatRooms){
            if(idpage.equals(p.getIdPage()) && id.equals(p.getId()) && type.equals(p.getType()))
                return  p;
        }
        return null;
    }
}
