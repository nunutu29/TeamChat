package org.teamchat.Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.teamchat.App.Config;
import org.teamchat.App.EndPoints;
import org.teamchat.App.MyApplication;
import org.teamchat.Helper.Utils;
import org.teamchat.R;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    private String id, name, email, username, password;
    private String GCM_ID;
    //Questa stringa verra usata per indicare quando un utente è entrato a far parte di un gruppo
    private String join_date;

    public User() {
    }

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
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

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getJoinDate(){
        return this.join_date;
    }
    public void setJoinDate(String joinDate){
        this.join_date = joinDate;
    }

    public String getGCM(){
        return this.GCM_ID;
    }
    public void setGCM(String id){
        this.GCM_ID = id;
    }

    public String getUsername(){
        return this.username;
    }
    public void setUsername(String username){
        this.username = username;
    }

    public String getPassword(){
        return this.password;
    }
    public void setPassword(String password){
        this.password = password;
    }

    public void setAvatar(final Context context, final ImageView view, boolean forceNew){

        Bitmap b = null;
        if(!forceNew)
            b =  Utils.loadFileFromStorage(context, Config.IMG_USER, getId().concat(".png"));
        else
            Utils.deleteFileFromStorage(context, Config.IMG_USER, getId().concat(".png"));

        if(b != null)
        {
            //se abbiamo trovato un avatar in locale, usare quello
            if(view != null)
                view.setImageBitmap(b);
        }
        else
        {
            //andiamo in remoto per cercare il suo avatar.
            //Quindi controlliamo prima l'internet
            if(!Utils.hasInternet(context)) {
                if(view != null)
                    view.setImageResource(R.mipmap.ic_launcher);
                return;
            }
            String url = EndPoints.USER_GET_AVATAR + "/"+ getId().concat(".png");
            //cerca avatar in remoto
            ImageRequest imgRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    //save the image for this user.
                    Utils.saveToInternalStorage(context, response,Config.IMG_USER, getId().concat(".png"));
                    if(view != null)
                            view.setImageBitmap(response);
                }
            }, 0, 0, null,
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if(view != null)
                                view.setImageResource(R.mipmap.ic_launcher);
                        }
                    });
            MyApplication.getInstance().addToRequestQueue(imgRequest);
        }
    }

    public static JSONObject creaListUtenti(ArrayList<User> users){
        JSONObject tmp;
        JSONArray tmpArray = new JSONArray();
        for (User user : users) {
            tmp = new JSONObject();
            try{
                tmp.put("id", user.getId());
            }
            catch (JSONException ex){
                ex.printStackTrace();
            }
            tmpArray.put(tmp);
        }
        JSONObject finalObj = new JSONObject();
        try {
            finalObj.put("user_list", tmpArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return finalObj;
    }
}
