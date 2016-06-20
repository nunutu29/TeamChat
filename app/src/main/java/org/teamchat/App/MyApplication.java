package org.teamchat.App;

/**
 * Created by Johnny
 */

import android.app.Application;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.teamchat.Helper.DbManager;
import org.teamchat.Helper.MyPreferenceManager;

public class MyApplication extends Application {

    public static final String TAG = MyApplication.class.getSimpleName();
    //Proprieta Private
    private RequestQueue mRequestQueue;
    private static MyApplication mInstance;
    private MyPreferenceManager pref;
    private String CurrentActivity;
    private DbManager manager;
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public void setCurrentActivity(String id){
        this.CurrentActivity = id;
    }
    public String getCurrentActivity(){
        return this.CurrentActivity;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public MyPreferenceManager getPrefManager() {
        if (pref == null) {
            pref = new MyPreferenceManager(this);
        }
        return pref;
    }

    public DbManager getDbManager(){
        if(manager == null)
            manager = new DbManager(this);
        return manager;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }


}
