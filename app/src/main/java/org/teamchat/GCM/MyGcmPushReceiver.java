/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.teamchat.GCM;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.teamchat.Activity.MainActivity;
import org.teamchat.App.Config;
import org.teamchat.App.MyApplication;
import org.teamchat.Helper.DbHelper;
import org.teamchat.Model.ChatRoom;
import org.teamchat.Model.Message;
import org.teamchat.Model.Pages;
import org.teamchat.Model.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class MyGcmPushReceiver extends FirebaseMessagingService {

    private static final String TAG = MyGcmPushReceiver.class.getSimpleName();

    private NotificationUtils notificationUtils;

    @Override
    public void onMessageReceived(RemoteMessage message) {
        //String from = message.getFrom();
        Map bundle = message.getData();

        String title = bundle.get("title").toString();
        Boolean isBackground = Boolean.valueOf(bundle.get("is_background").toString());
        String flag = bundle.get("flag").toString();
        String data = bundle.get("data").toString();

        if (flag == null)
            return;

        if(MyApplication.getInstance().getPrefManager().getUser() == null){
            // user is not logged in, skipping push notification
            Log.e(TAG, "user is not logged in, skipping push notification");
            return;
        }

        switch (Integer.parseInt(flag)) {
            case Config.PUSH_TYPE_CHATROOM:
                // push notification belongs to a chat room
                processChatRoomPush(title, isBackground, data);
                break;
            case Config.PUSH_TYPE_USER:
                // push notification is specific to user
                processUserMessage(title, isBackground, data);
                break;
            case Config.PUSH_FLAG_USER_LEFT_PAGE:
                processUserLeftPage(isBackground, data);
                break;
            case Config.PUSH_FLAG_USER_LEFT_CHAT:
                processUserLeftChat(isBackground, data);
                break;
            case Config.PUSH_FLAG_PAGE_DELETE:
                processPageDeleted(isBackground, data);
                break;
            case Config.PUSH_FLAG_CHAT_DELETE:
                processChatDelete(isBackground, data);
                break;
            case Config.PUSH_FLAG_CHAT_CREATED:
                processNewChat(isBackground, data);
                break;
            case Config.PUSH_FLAG_USER_JOIN_PAGE:
                processNewUserPage(isBackground, data);
                break;
            case Config.PUSH_FLAG_USER_AVATAR_CHANGE:
                processUserAvatar(isBackground, data);
                break;
        }
    }

    private void processUserAvatar(Boolean isBackground, String data) {
        if(isBackground){
            try {
                JSONObject mObj = new JSONObject(data);
                User user = new User();
                user.setId(mObj.getString(DbHelper.IDUTENTE));
                user.setAvatar(getApplicationContext(), null, true);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    private void processNewUserPage(Boolean isBackground, String data) {
        if(!isBackground){
            try {
                JSONObject mObj = new JSONObject(data);
                Pages page = MyApplication.getInstance().getDbManager()
                        .getPage(mObj.getString(DbHelper.IDPAGINA));

                JSONObject userObj = mObj.getJSONObject(DbHelper.TABLE_UTENTI);
                User user = new User();
                user.setId(userObj.getString(DbHelper.IDUTENTE));
                user.setName(userObj.getString(DbHelper.NOME));
                //Questo sono io
                if(MyApplication.getInstance().getPrefManager().getUser().getId().equals(user.getId()))
                    return;

                String data_creazione = mObj.getString(DbHelper.DATA_CREAZIONE);
                //Inserimento utente nella tabelaa utenti se non esiste gia.
                MyApplication.getInstance().getDbManager().addUser(user);
                MyApplication.getInstance().getDbManager()
                        .addUserToPage(user.getId(),page.getId(), data_creazione);

                if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                    pushNotification.putExtra("type", Config.PUSH_FLAG_USER_JOIN_PAGE);
                    pushNotification.putExtra(DbHelper.TABLE_PAGINE, page);
                    pushNotification.putExtra(DbHelper.NOME, user.getName());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Processing chat room push message
     * this message will be broadcasts to all the activities registered
     * */
    private void processChatRoomPush(String title, boolean isBackground, String data) {
        if (!isBackground) {

            try {
                JSONObject datObj = new JSONObject(data);
                JSONObject mObj = datObj.getJSONObject("MESSAGGIO");
                Message message = new Message();
                message.setIdPage(mObj.getString(DbHelper.IDPAGINA));
                message.setIdChat(mObj.getString(DbHelper.IDCHAT));
                message.setMessage(mObj.getString(DbHelper.DESCRIZIONE));
                message.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                message.setType(Config.GROUP_CHAT_ROOM);
                JSONObject uObj = datObj.getJSONObject("UTENTE");

                // skip the message if the message belongs to same user as
                // the user would be having the same message when he was sending
                // but it might differs in your scenario
                if (uObj.getString(DbHelper.IDUTENTE).equals(MyApplication.getInstance().getPrefManager().getUser().getId())) {
                   //Stesso utente -> Skip
                    return;
                }

                User user = new User();
                user.setId(uObj.getString(DbHelper.IDUTENTE));
                user.setName(uObj.getString(DbHelper.NOME));
                message.setUser(user);
                MyApplication.getInstance().getDbManager().addMessage(message);
                // verifying whether the app is in background or foreground

                if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {

                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                    pushNotification.putExtra("type", Config.PUSH_TYPE_CHATROOM);
                    pushNotification.putExtra("MESSAGGIO", message);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                }

                //app o è in background, oppure siamo in un altra activity diversa dalla chatRoom per la qualle è arrivato il messaggio
                if((MyApplication.getInstance().getCurrentActivity() == null) ||
                        !MyApplication.getInstance().getCurrentActivity().equals(message.getIdPage() + "_" + message.getIdChat() + "_" + Config.GROUP_CHAT_ROOM)){

                    MyApplication.getInstance().getPrefManager().storeUnReadCounter(message.getIdPage(), message.getIdChat(), Config.GROUP_CHAT_ROOM);
                    MyApplication.getInstance().getPrefManager().setMoreThanOneChat(message.getIdPage(), message.getIdChat(), Config.GROUP_CHAT_ROOM);
                    Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);

                    if(MyApplication.getInstance().getPrefManager().MoreThanOne())
                    {
                        //Se piu chat allora aprira la lista
                        //resultIntent.putExtra(Config.FRAGMENT_TITLE, mObj.getString("NOME_PAGINA"));
                        resultIntent.putExtra(Config.FRAGMENT_ID, Config.CHAT_LIST_FRAGMENT);
                    }
                    else
                    {
                        //se una sola chat, aprira la chat
                        //resultIntent.putExtra(Config.FRAGMENT_TITLE, mObj.getString("NOME_CHAT"));
                        resultIntent.putExtra(Config.FRAGMENT_ID, Config.CHAT_ROOM_FRAGMENT);
                        resultIntent.putExtra(Config.IDCHAT, message.getIdChat());
                    }
                    resultIntent.putExtra(Config.IDPAGINA, message.getIdPage());

                    showNotificationMessage(getApplicationContext(), title,
                            user.getName() + " : " + message.getMessage(),
                            message.getCreatedAt(), resultIntent,
                            MyApplication.getInstance().getPrefManager().checkSilentRoom(message.getIdPage(), message.getIdChat(), Config.GROUP_CHAT_ROOM));
                }
                else
                {
                    //come per la chat singola. suono dentro la chat
                    //NotificationUtils notificationUtils = new NotificationUtils();
                    //notificationUtils.playNotificationSound();
                }

            } catch (JSONException e) {
                Log.e(TAG, "json parsing error: " + e.getMessage());
                Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        } else {
            // the push notification is silent, may be other operations needed
            // like inserting it in to SQLite
        }
    }

    /**
     * Processing user specific push message
     * It will be displayed with / without image in push notification tray
     * */
    private void processUserMessage(String title, boolean isBackground, String data) {
        if (!isBackground) {

            try {
                JSONObject datObj = new JSONObject(data);
                JSONObject mObj = datObj.getJSONObject("MESSAGGIO");
                JSONObject uObj = datObj.getJSONObject("UTENTE");

                // skip the message if the message belongs to same user as
                // the user would be having the same message when he was sending
                // but it might differs in your scenario
                if (uObj.getString(DbHelper.IDUTENTE).equals(MyApplication.getInstance().getPrefManager().getUser().getId())) {
                    //Stesso utente -> Skip
                    return;
                }

                Message message = new Message();
                User user = new User();
                user.setId(uObj.getString(DbHelper.IDUTENTE));
                user.setName(uObj.getString(DbHelper.NOME));


                message.setIdPage(mObj.getString(DbHelper.IDPAGINA));
                message.setUser(user);
                message.setIdChat(MyApplication.getInstance().getPrefManager().getUser().getId());
                message.setType(Config.SINGLE_CHAT_ROOM);
                message.setMessage(mObj.getString(DbHelper.DESCRIZIONE));
                message.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

                MyApplication.getInstance().getDbManager().addMessage(message);


                // verifying whether the app is in background or foreground
                if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {

                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                    pushNotification.putExtra("type", Config.PUSH_TYPE_USER);
                    pushNotification.putExtra("MESSAGGIO", message);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                }

                //app o è in background, oppure siamo in un altra activity diversa dalla chatRoom per la qualle è arrivato il messaggio
                if((MyApplication.getInstance().getCurrentActivity() == null) ||
                        !MyApplication.getInstance().getCurrentActivity().equals(message.getIdPage() + "_" + message.getUser().getId() + "_" + Config.SINGLE_CHAT_ROOM)){

                    MyApplication.getInstance().getPrefManager().storeUnReadCounter(message.getIdPage(), message.getUser().getId(), Config.SINGLE_CHAT_ROOM);
                    MyApplication.getInstance().getPrefManager().setMoreThanOneChat(message.getIdPage(), message.getUser().getId(), Config.SINGLE_CHAT_ROOM);
                    Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);

                    if(MyApplication.getInstance().getPrefManager().MoreThanOne())
                    {
                        //Se piu chat allora aprira la lista
                        //resultIntent.putExtra(Config.FRAGMENT_TITLE, mObj.getString("NOME_PAGINA"));
                        resultIntent.putExtra(Config.FRAGMENT_ID, Config.CHAT_LIST_FRAGMENT);
                    }
                    else
                    {
                        //se una sola chat, aprira la chat
                        //resultIntent.putExtra(Config.FRAGMENT_TITLE, user.getName());
                        resultIntent.putExtra(Config.FRAGMENT_ID, Config.SINGLE_CHAT_ROOM_FRAGMENT);
                        resultIntent.putExtra(Config.IDUTENTE, user.getId());
                    }
                    resultIntent.putExtra(Config.IDPAGINA, message.getIdPage());

                    showNotificationMessage(getApplicationContext(), title,
                            user.getName() + " : " + message.getMessage(),
                            message.getCreatedAt(), resultIntent,
                            MyApplication.getInstance().getPrefManager().checkSilentRoom(message.getIdPage(), message.getUser().getId(), Config.SINGLE_CHAT_ROOM));
                }
                else
                {
                    //qui siamo dentro la chat, quindi magari aggiungere un suone diverso quando
                    //si ricevono messaggi
                    //NotificationUtils notificationUtils = new NotificationUtils();
                    //notificationUtils.playNotificationSound();
                }

            } catch (JSONException e) {
                Log.e(TAG, "json parsing error: " + e.getMessage());
                Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        } else {
            // the push notification is silent, may be other operations needed
            // like inserting it in to SQLite
        }
    }

    private void processChatDelete(boolean isBackground, String data){
        if(!isBackground){
            try {
                JSONObject mObj = new JSONObject(data);
                ChatRoom chatRoom = new ChatRoom();
                chatRoom.setIdPages(mObj.getString(DbHelper.IDPAGINA));
                chatRoom.setId(mObj.getString(DbHelper.IDCHAT));
                chatRoom.setType(Config.GROUP_CHAT_ROOM);

                MyApplication.getInstance().getDbManager().deleteChatRoom(chatRoom);
                if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {

                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                    pushNotification.putExtra("type", Config.PUSH_FLAG_CHAT_DELETE);
                    pushNotification.putExtra(DbHelper.TABLE_CHAT, chatRoom);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    private void processUserLeftChat(boolean isBackground, String data){
        if(!isBackground){
            try {
                JSONObject mObj = new JSONObject(data);
                ChatRoom chatRoom = MyApplication.getInstance().getDbManager()
                        .getChatRoom(mObj.getString(DbHelper.IDPAGINA), mObj.getString(DbHelper.IDCHAT));

                MyApplication.getInstance().getDbManager()
                        .removeUserFromChat(chatRoom, mObj.getString(DbHelper.IDUTENTE));

                if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                    pushNotification.putExtra("type", Config.PUSH_FLAG_USER_LEFT_CHAT);
                    pushNotification.putExtra(DbHelper.TABLE_CHAT, chatRoom);
                    pushNotification.putExtra(DbHelper.NOME, MyApplication.getInstance().getDbManager()
                                    .getUser(mObj.getString(DbHelper.IDUTENTE)).getName());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    private void processNewChat(boolean isBackground, String data){
        if(!isBackground){
            try {
                //error false.
                JSONObject mObj = new JSONObject(data);
                if(MyApplication.getInstance().getPrefManager().getUser().getId().equals(mObj.getString(DbHelper.IDUTENTE))){
                    return;
                }

                ChatRoom chatRoom = new ChatRoom();
                chatRoom.setIdPages(mObj.getString(DbHelper.IDPAGINA));
                chatRoom.setId(mObj.getString(DbHelper.IDCHAT));
                chatRoom.setName(mObj.getString(DbHelper.TITOLO));
                chatRoom.setTimestamp(mObj.getString(DbHelper.DATA_CREAZIONE));
                chatRoom.setType(Config.GROUP_CHAT_ROOM);
                chatRoom.setPRIVATE(mObj.getString(DbHelper.PRIVATE));
                chatRoom.setAuthor(mObj.getString(DbHelper.IDUTENTE));
                MyApplication.getInstance().getDbManager().addChatRooms(chatRoom);

                if(chatRoom.getPRIVATE().equals(Config.CHAT_ROOM_PRIVATE)){
                    //se la room che abbiamo inserite è privata, aggiungere anche i suoi membri
                    JSONArray arrJson = mObj.getJSONArray(DbHelper.TABLE_CHAT_UTENTI);
                    int len = arrJson.length();
                    for(int i = 0; i < len; i++){
                        JSONObject tmp = (JSONObject) arrJson.get(i);
                        MyApplication.getInstance().getDbManager().addUserToChat(chatRoom, tmp.getString(DbHelper.IDUTENTE));
                    }
                }
                if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                    pushNotification.putExtra("type", Config.PUSH_FLAG_CHAT_CREATED);
                    pushNotification.putExtra(DbHelper.TABLE_CHAT, chatRoom);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    private void processUserLeftPage(boolean isBackground, String data){
        if(!isBackground){
            try {
                JSONObject mObj = new JSONObject(data);
                Pages page = MyApplication.getInstance().getDbManager()
                        .getPage(mObj.getString(DbHelper.IDPAGINA));

                MyApplication.getInstance().getDbManager()
                        .removeUserFromPage(page, mObj.getString(DbHelper.IDUTENTE));

                if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                    pushNotification.putExtra("type", Config.PUSH_FLAG_USER_LEFT_PAGE);
                    pushNotification.putExtra(DbHelper.TABLE_PAGINE, page);
                    pushNotification.putExtra(DbHelper.NOME, MyApplication.getInstance().getDbManager()
                            .getUser(mObj.getString(DbHelper.IDUTENTE)).getName());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    private void processPageDeleted(boolean isBackground, String data){
        if(!isBackground){
            try {
                JSONObject mObj = new JSONObject(data);
                Pages page = MyApplication.getInstance().getDbManager()
                        .getPage(mObj.getString(DbHelper.IDPAGINA));

                MyApplication.getInstance().getDbManager().deletePage(page.getId());

                if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                    pushNotification.putExtra("type", Config.PUSH_FLAG_PAGE_DELETE);
                    pushNotification.putExtra(DbHelper.TABLE_PAGINE, page);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Showing notification with text only
     * */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent, boolean isSilent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, isSilent);
    }

    /**
     * Showing notification with text and image
     * */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl, boolean isSilent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl, isSilent);
    }
}
