package org.teamchat.Model;

import android.content.Context;
import android.support.annotation.BoolRes;

import org.teamchat.Helper.DbManager;

import java.io.Serializable;

/**
 * Created by Johnny
 */
public class Role implements Serializable {
    private String descrizione, idPage, id;
    private boolean cPub, cPriv, cBan, dPub, dPriv;

    public Role() {
    }

    public boolean isMain(){
        return this.id.equals("1");
    }

    public void setDescrizione(String s){
        this.descrizione = s;
    }
    public String getDescrizione(){
        return this.descrizione;
    }

    public void setIdPage(String s){
        this.idPage = s;
    }
    public String getIdPage(){
        return this.idPage;
    }

    public void setId(String s){
        this.id = s;
    }
    public String getId(){
        return this.id;
    }

    public void setCanCreatePub(boolean b){
        this.cPub = b;
    }
    public boolean getCanCreatePub(){
        return this.cPub;
    }

    public void setCanCreatePriv(boolean b){
        this.cPriv = b;
    }
    public boolean getCanCreatePriv(){
        return this.cPriv;
    }

    public void setCanBan(boolean b){
        this.cBan = b;
    }
    public boolean getCanBan(){
        return this.cBan;
    }

    public void setCanDeletePub(boolean b){
        this.dPub = b;
    }
    public boolean getCanDeletePub(){
        return this.dPub;
    }

    public void setCanDeletePriv(boolean b){
        this.dPriv = b;
    }
    public boolean getCanDeletePriv(){
        return this.dPriv;
    }
}
