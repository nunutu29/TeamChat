package org.teamchat.Model;

import android.support.annotation.Nullable;

import org.teamchat.App.MyApplication;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Johnny
 */
public class Pages implements Serializable {
    String _ERROR;
    String id, title, description, pass, data_creazione;
    int unreadCount;


    public Pages(){
    }

    public Pages(String id,String title, String description, int unreadCount){
        this.id = id;
        this.title = title;
        this.description = description;
        this.unreadCount = unreadCount;
    }

    public String getError (){
        return _ERROR;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getId(){
        return id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }

    public int getUnreadCount() {
        return unreadCount;
    }
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getPass() {
        return pass;
    }
    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getData_creazione() {
        return data_creazione;
    }
    public void setData_creazione(String date) {
        this.data_creazione = date;
    }

    @Nullable
    public static Pages getPageByID(String idPage, ArrayList<Pages> pagesArray){
        for(Pages p : pagesArray){
            if(idPage.equals(p.getId()))
                return  p;
        }
        return null;
    }
}
