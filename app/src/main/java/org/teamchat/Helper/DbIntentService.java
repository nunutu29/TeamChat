package org.teamchat.Helper;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.teamchat.App.Config;
import org.teamchat.App.EndPoints;
import org.teamchat.App.MyApplication;
import org.teamchat.Model.ChatRoom;
import org.teamchat.Model.Pages;
import org.teamchat.Model.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Johnny on 02/05/2016.
 * Usato per fare il sync.
 */
public class DbIntentService extends IntentService {
    public static final String TAG = DbIntentService.class.getSimpleName();

    public static final String KEY = "key";
    public static final String SYNC = "sync";
    public static final String LEAVE_CHATROOM = "leavechatroom";
    public static final String DELETE_CHATROOM = "deletechatroom";
    public static final String LEAVE_PAGE = "leavepage";
    public static final String DELETE_PAGE = "deletepage";
    public static final String CHANGE_AVATAR = "changeavatar";

    private int REQUESTS;
    private User self;
    private String onCompleteAction;
    public DbIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ChatRoom chatRoom;
        Pages page;
        String key = intent.getStringExtra(KEY);
        this.onCompleteAction = key;
        REQUESTS = 0;
        switch (key){
            case SYNC:
                DoSync();
                break;
            case LEAVE_CHATROOM: //Only for group chat
                chatRoom =  (ChatRoom) intent.getSerializableExtra(DbHelper.TABLE_CHAT);
                PerformAction_Chat(chatRoom, EndPoints.LEAVE_CHATROOM);
                break;
            case DELETE_CHATROOM: //Only for group chat
                //delete also counters
                chatRoom =  (ChatRoom) intent.getSerializableExtra(DbHelper.TABLE_CHAT);
                PerformAction_Chat(chatRoom, EndPoints.CHAT_ROOM_DELETE);
                break;
            case LEAVE_PAGE:
                //delete alos counters
                page = new Pages();
                page.setId(intent.getStringExtra(DbHelper.IDPAGINA));
                PerformAction_Page(page, EndPoints.PAGE_LEAVE);
                break;
            case DELETE_PAGE:
                page = new Pages();
                page.setId(intent.getStringExtra(DbHelper.IDPAGINA));
                PerformAction_Page(page, EndPoints.PAGE_DELETE);
                break;
            case CHANGE_AVATAR:
                Bitmap bitmap = intent.getParcelableExtra("BitmapImage");
                if(bitmap != null)
                    ChangeAvatar(bitmap);
                break;
        }
    }

    private void DoSync(){
        Log.e(TAG, "init Sync");

        self = MyApplication.getInstance().getPrefManager().getUser();
        //prima svuota tutto
        REQUESTS = 1;
        StartLoad(
                new String[]{
                        DbHelper.TABLE_PAGINE,
                        DbHelper.TABLE_PAGINE_RUOLI,
                        DbHelper.TABLE_UTENTI,
                        DbHelper.TABLE_PAGINE_UTENTI,
                        DbHelper.TABLE_CHAT,
                        DbHelper.TABLE_CHAT_UTENTI,
                }
        );
    }

    private void PerformAction_Chat(final ChatRoom chatRoom, String EndPoint){
        try{

            StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            try{
                                JSONObject obj = new JSONObject(response);
                                if(!obj.getBoolean("error")){
                                    MyApplication.getInstance().getDbManager().deleteChatRoom(chatRoom);
                                    MyApplication.getInstance().getPrefManager().deleteUnreadCounter(chatRoom.getIdPage(), chatRoom.getId(), chatRoom.getType());
                                }
                                else
                                    Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                            catch (JSONException e){
                                e.printStackTrace();
                            }
                            finally {
                                onComplete(Config.SYNC_COMPLETE);
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            }){

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put(DbHelper.IDPAGINA, chatRoom.getIdPage());
                    params.put(DbHelper.IDCHAT, chatRoom.getId());
                    params.put(DbHelper.IDUTENTE, MyApplication.getInstance().getPrefManager().getUser().getId());
                    return params;
                }
            };
            MyApplication.getInstance().addToRequestQueue(stringRequest);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void PerformAction_Page(final Pages page, String EndPoint){
        try{

            StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try{
                                JSONObject obj = new JSONObject(response);
                                if(!obj.getBoolean("error")){
                                    MyApplication.getInstance().getDbManager().deletePage(page.getId());
                                    MyApplication.getInstance().getPrefManager().deleteUnreadCounter(page.getId(), null, null);
                                }
                                else
                                    Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                            catch (JSONException e){
                                e.printStackTrace();
                            }
                            finally {
                                onComplete(Config.SYNC_COMPLETE);
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            }){

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put(DbHelper.IDPAGINA, page.getId());
                    if(!onCompleteAction.equals(DELETE_PAGE))
                        params.put(DbHelper.IDUTENTE, MyApplication.getInstance().getPrefManager().getUser().getId());
                    return params;
                }
            };
            MyApplication.getInstance().addToRequestQueue(stringRequest);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void StartLoad(final String[] tables){
        try{
            StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoints.SYNC.replace(EndPoints.IDUTENTE, self.getId()),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            try{
                                //da modificare
                                //per ogni tabella, cancellare le righe con chiavi diversa da queste ritornate.
                                //per quelle rimaste eseguire Inserimento oppure update se la chiave esiste gia.

                                MyApplication.getInstance().getDbManager().Refresh();
                                JSONObject obj = new JSONObject(response);
                                JSONArray arr;
                                for (String table : tables) {
                                    arr = obj.getJSONArray(table);
                                    int len = arr.length();
                                    for (int i = 0; i < len; i++) {
                                        JSONObject tmp = (JSONObject) arr.get(i);
                                        ContentValues val = MyApplication.getInstance().getDbManager().setValues(table, tmp);
                                        MyApplication.getInstance().getDbManager().execInsert(table, val);
                                    }
                                }
                            }
                            catch (JSONException e){
                                e.printStackTrace();
                            }
                            finally {
                                REQUESTS--;
                                //Toast.makeText(getApplicationContext(), "Sync Done", Toast.LENGTH_SHORT).show();
                                onComplete(Config.SYNC_COMPLETE);
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    REQUESTS--;
                    error.printStackTrace();
                }
            });

            MyApplication.getInstance().addToRequestQueue(stringRequest);
        }
        catch (Exception ex){
            REQUESTS--;
            ex.printStackTrace();
        }

    }

    private void ChangeAvatar(final Bitmap bitmap){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoints.USER_SET_AVATAR, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //store bitmap
                Utils.saveToInternalStorage(getApplicationContext(),
                        bitmap,
                        Config.IMG_USER,
                        MyApplication.getInstance().getPrefManager().getUser().getId().concat(".png"));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("_image_", Utils.getStringImage(bitmap));
                params.put(DbHelper.IDUTENTE, MyApplication.getInstance().getPrefManager().getUser().getId());
                return params;
            }
        };
        MyApplication.getInstance().addToRequestQueue(stringRequest);
    }

    private void onComplete(String intentType){
        if(REQUESTS > 0) return;
        Intent complete = new Intent(intentType);
        complete.putExtra("action", this.onCompleteAction);
        LocalBroadcastManager.getInstance(this).sendBroadcast(complete);
    }
}
