package org.teamchat.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.teamchat.Model.User;

import java.util.Map;

/**
 * Created by Johnny
 */
public class MyPreferenceManager {

    private String TAG = MyPreferenceManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = Context.MODE_PRIVATE;

    // Sharedpref file name
    private static final String PREF_NAME = "TeamChat_GCM";

    // All Shared Preferences Keys
    private static final String KEY_USER_GCM = "GCM_ID";
    private static final String KEY_USER_ID = "IDUTENTE";
    private static final String KEY_USER_NAME = "NOME";
    private static final String KEY_USER_EMAIL = "EMAIL";
    private static final String KEY_NOTIFICATIONS = "notifications";

    // Constructor
    public MyPreferenceManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
    //region Utente
    public void storeUser(User user) {
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.commit();

        Log.e(TAG, "User is stored in shared preferences. " + user.getName() + ", " + user.getEmail());
    }

    public void storeGCM(String id){
        editor.putString(KEY_USER_GCM, id);
        editor.commit();
    }

    public String getGCM(){
        String token = pref.getString(KEY_USER_GCM, null);
        return token != null ? token : "";
    }

    public User getUser() {
        if (pref.getString(KEY_USER_ID, null) != null) {
            String id, name, email;
            id = pref.getString(KEY_USER_ID, null);
            name = pref.getString(KEY_USER_NAME, null);
            email = pref.getString(KEY_USER_EMAIL, null);
            return new User(id, name, email);
        }
        return null;
    }
    //endregion

    //in futuro mettere Counter e silent rooms nel Database

    //region Counter
    public void deleteUnreadCounter(String idPage, String idChat, String type){
        if(idChat == null){
            Map<String, ?> map = pref.getAll();
            for(Map.Entry<String, ?> entry : map.entrySet()){
                if(entry.getKey().contains(idPage + "_"))
                    editor.remove(entry.getKey());
            }
        }
        else
            editor.remove(idPage + "_" + idChat + "_" + type);
        editor.commit();
    }

    public void storeUnReadCounter(String idPage, String idChat, String type){
        int counter = pref.getInt(idPage + "_" + idChat + "_" + type, 0);
        editor.putInt(idPage + "_" + idChat + "_" + type, counter + 1);
        editor.commit();
    }

    public int getUnReadCounter(String idPage){
        return getUnReadCounter(idPage, null, null);
    }

    public int getUnReadCounter(String idPage, String idChat, String type){
        if(idChat == null){
            Map<String, ?> map = pref.getAll();
            int counter = 0;
            for(Map.Entry<String, ?> entry : map.entrySet()){
                if(entry.getKey().startsWith(idPage + "_")) {
                    if(entry.getValue() != null) {
                        counter += Integer.parseInt(entry.getValue().toString());
                    }
                }
            }
            return counter;
        }
        else
            return pref.getInt(idPage + "_" + idChat + "_" + type, 0);
    }
    //endregion Counter

    //region Notifiche
    public void setMoreThanOneChat(String idPage, String idChat, String type){
        if(!pref.getBoolean("MoreThanOne", false)) {
            String cO = pref.getString("ChatToBeOpened", null);
            if (cO != null && !cO.equals(idPage + "_" + idChat + "_" + type))
                editor.putBoolean("MoreThanOne", true);
            editor.putString("ChatToBeOpened", idPage + "_" + idChat + "_" + type);
            editor.commit();
        }

    }

    public boolean MoreThanOne(){
        return pref.getBoolean("MoreThanOne", false);
    }

    public void addNotification(String notification) {

        // get old notifications
        String oldNotifications = getNotifications();

        if (oldNotifications != null) {
            oldNotifications += "|" + notification;
        } else {
            oldNotifications = notification;
        }

        editor.putString(KEY_NOTIFICATIONS, oldNotifications);
        editor.commit();
    }

    public String getNotifications() {
        return pref.getString(KEY_NOTIFICATIONS, null);
    }

    public void clearNotifications(){
        editor.remove("ChatToBeOpened");
        editor.remove("MoreThanOne");
        editor.remove(KEY_NOTIFICATIONS);
        editor.commit();
    }
    //endregion Fine Notifiche

    //region silent rooms
    public void storeSilentRoom(String idPage, String idChat, String type){
        editor.putBoolean("silent_" + idPage + "_" + idChat + "_" + type, true);
        editor.commit();
    }
    public void removeSilentRoom(String idPage, String idChat, String type){
        editor.remove("silent_" + idPage + "_" + idChat + "_" + type);
        editor.commit();
    }
    public boolean checkSilentRoom(String idPage, String idChat, String type){
        return pref.getBoolean("silent_" + idPage + "_" + idChat + "_" + type, false);
    }
    //endregion

    public void clear() {
        editor.clear();
        editor.commit();
    }
}
