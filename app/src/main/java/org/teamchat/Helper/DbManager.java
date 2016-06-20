package org.teamchat.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.teamchat.App.Config;
import org.teamchat.App.MyApplication;
import org.teamchat.Model.ChatRoom;
import org.teamchat.Model.Message;
import org.teamchat.Model.Pages;
import org.teamchat.Model.Role;
import org.teamchat.Model.User;
import org.teamchat.R;

import java.util.ArrayList;

/**
 * Created by Johnny on 30/04/2016.
 * TODO
 * Pagine
 * Update Page, getPageById
 * Utenti
 * Update Utente, getUserByID
 */
public class DbManager {
    private DbHelper dbHelper;
    private Context context;

    public DbManager(Context context){
        this.dbHelper = new DbHelper(context);
        this.context = context;
    }

    //General Insert
    public void execInsert(String table, ContentValues values){
        if(values == null) return;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insert(table, null, values);
        //db.close();
    }

    //Pagine
    public void deletePage(String idPage){
        //Delete pages
        //tramit le foreign keys verra cancellato tutto a catena..
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DbHelper.TABLE_PAGINE, DbHelper.IDPAGINA + "= ?", new String[]{ idPage });
        deleteMessages(idPage);
    }
    public Pages getPage(String idPage){
        Pages page = new Pages();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DbHelper.TABLE_PAGINE, null, DbHelper.IDPAGINA +"=?", new String[]{idPage}, null, null, null);
        if(cursor.moveToFirst()){
            page.setId(cursor.getString(0));
            page.setTitle(cursor.getString(1));
            page.setDescription(cursor.getString(2));
            page.setData_creazione(cursor.getString(3));
            page.setPass(cursor.getString(4));
            //page.setUnreadCount(MyApplication.getInstance().getPrefManager().getUnReadCounter(page.getId()));
        }
        cursor.close();
        //db.close();
        return page;
    }
    public void addPage(Pages page){
        ContentValues values = new ContentValues();
        values.put(DbHelper.IDPAGINA, page.getId());
        values.put(DbHelper.TITOLO, page.getTitle());
        values.put(DbHelper.DESCRIZIONE, page.getDescription());
        values.put(DbHelper.DATA_CREAZIONE, page.getData_creazione());
        values.put(DbHelper.PASSWORD, page.getPass());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insert(DbHelper.TABLE_PAGINE, null, values);
        //db.close();
    }
    public ArrayList<Pages> getPagesList(){
        ArrayList<Pages> pages = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String Table = String.format("%s as p inner join %s as pu on p.%s = pu.%3$s", DbHelper.TABLE_PAGINE, DbHelper.TABLE_PAGINE_UTENTI, DbHelper.IDPAGINA);
        String[] Columns = new String[]{ "p.*" };
        String Where = String.format("pu.%s=?", DbHelper.IDUTENTE);

        Cursor cursor = db.query(Table, Columns, Where, new String[]{MyApplication.getInstance().getPrefManager().getUser().getId()}, null, null, null);

        if(cursor.moveToFirst()){
            do{
                Pages page=new Pages();
                page.setId(cursor.getString(0));
                page.setTitle(cursor.getString(1));
                page.setDescription(cursor.getString(2));
                page.setData_creazione(cursor.getString(3));
                page.setPass(cursor.getString(4));
                page.setUnreadCount(MyApplication.getInstance().getPrefManager().getUnReadCounter(page.getId()));
                pages.add(page);
            }
            while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return pages;
    }

    //Utenti
    public void addUser(User user){
        ContentValues values = new ContentValues();
        values.put(DbHelper.IDUTENTE, user.getId());
        values.put(DbHelper.NOME, user.getName());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insert(DbHelper.TABLE_UTENTI, null, values);
        //db.close();
    }
    public User getUser(String id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DbHelper.TABLE_UTENTI, null, DbHelper.IDUTENTE+"=?", new String[]{id}, null,null,null);
        User user = new User();
        if(cursor.moveToFirst()){
            user.setId(cursor.getString(0));
            user.setName(cursor.getString(1));
        }
        cursor.close();
        return user;
    }
    public ArrayList<User> getUsersList(){
        ArrayList<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DbHelper.TABLE_UTENTI, null, null, null, null, null, null);
        if(cursor.moveToNext()){
            do{
                User user = new User();
                user.setId(cursor.getString(0));
                //user.setName(cursor.getString(1));
                users.add(user);
            }
            while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return users;
    }
    public ArrayList<User> getUsersList(String idPage){
        ArrayList<User> userArrayList =  new ArrayList<>();

        String table = String.format("%s as pu inner join %s as u on pu.%s= u.%3$s", DbHelper.TABLE_PAGINE_UTENTI, DbHelper.TABLE_UTENTI, DbHelper.IDUTENTE);
        String[] columns = new String[]{
                "u." + DbHelper.IDUTENTE,
                "u." + DbHelper.NOME,
                "pu." + DbHelper.DATA_CREAZIONE };
        String Where = String.format("%s=? and u.%s <> ?", DbHelper.IDPAGINA, DbHelper.IDUTENTE);
        String[] keys = new String[]{ idPage, MyApplication.getInstance().getPrefManager().getUser().getId() };



        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(table, columns, Where, keys, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                User user = new User();
                user.setId(cursor.getString(0));
                user.setName(cursor.getString(1));
                user.setJoinDate(cursor.getString(2));
                userArrayList.add(user);
            }
            while(cursor.moveToNext());
        }

        cursor.close();
        //db.close();
        return userArrayList;
    }
    public ArrayList<User> getUsersList(String idPage, String idChat){

        ArrayList<User> userArrayList =  new ArrayList<>();
        String table = String.format("%s as cu inner join %s as u on cu.%s= u.%3$s",
                DbHelper.TABLE_CHAT_UTENTI, DbHelper.TABLE_UTENTI, DbHelper.IDUTENTE);

        String[] columns = new String[]{ "u." + DbHelper.IDUTENTE, "u." + DbHelper.NOME };

        String Where = String.format("cu.%s=? and cu.%s=? and u.%s <> ?", DbHelper.IDPAGINA,
                DbHelper.IDCHAT, DbHelper.IDUTENTE);

        String[] keys = new String[]{ idPage, idChat,
                MyApplication.getInstance().getPrefManager().getUser().getId()};



        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(table, columns, Where, keys, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                User user = new User();
                user.setId(cursor.getString(0));
                user.setName(cursor.getString(1));
                userArrayList.add(user);
            }
            while(cursor.moveToNext());
        }

        cursor.close();
        //db.close();
        return userArrayList;
    }
    //Pagine Utenti
    public void removeUserFromPage(Pages page, String idUser){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DbHelper.TABLE_PAGINE_UTENTI,
                String.format("%s=? and %s=?", DbHelper.IDPAGINA, DbHelper.IDUTENTE),
                new String[]{page.getId(), idUser});
        //db.close();
    }
    public void addUserToPage(String idUser, String idPage, String data_creazione){
        ContentValues values = new ContentValues();
        values.put(DbHelper.IDPAGINA, idPage);
        values.put(DbHelper.IDUTENTE, idUser);
        values.put(DbHelper.DATA_CREAZIONE, data_creazione);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insert(DbHelper.TABLE_PAGINE_UTENTI, null, values);
        //db.close();
    }

    //ChatRooms
    public void addUserToChat(ChatRoom chatRoom, String idUser){
        ContentValues values = new ContentValues();

        values.put(DbHelper.IDPAGINA, chatRoom.getIdPage());
        values.put(DbHelper.IDCHAT, chatRoom.getId());
        values.put(DbHelper.IDUTENTE, idUser);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insert(DbHelper.TABLE_CHAT_UTENTI, null, values);
        //db.close();
    }
    public void removeUserFromChat(ChatRoom chatRoom, String idUser){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DbHelper.TABLE_CHAT_UTENTI,
                String.format("%s=? and %s=? and %s=?", DbHelper.IDPAGINA, DbHelper.IDCHAT, DbHelper.IDUTENTE),
                new String[]{chatRoom.getIdPage(), chatRoom.getId(), idUser});
        //db.close();
    }
    public void deleteChatRoom(ChatRoom chatRoom){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DbHelper.TABLE_CHAT, DbHelper.IDPAGINA + "= ? and " + DbHelper.IDCHAT + "= ?", new String[]{
                chatRoom.getIdPage(), chatRoom.getId()
        });
        //db.close();
        deleteMessages(chatRoom.getIdPage(),chatRoom.getId(), chatRoom.getType());
    }
    public void addChatRooms(ChatRoom chatRoom){
        ContentValues values = new ContentValues();

        values.put(DbHelper.IDPAGINA, chatRoom.getIdPage());
        values.put(DbHelper.IDCHAT, chatRoom.getId());
        values.put(DbHelper.IDUTENTE, chatRoom.getAuthor());
        values.put(DbHelper.TITOLO, chatRoom.getName());
        values.put(DbHelper.DATA_CREAZIONE, chatRoom.getTimestamp());
        values.put(DbHelper.PRIVATE, chatRoom.getPRIVATE());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insert(DbHelper.TABLE_CHAT, null, values);
       // db.close();
    }
    public ChatRoom getChatRoom(String idPage, String idChat){
        ChatRoom chatRoom = new ChatRoom();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DbHelper.TABLE_CHAT, null, String.format("%s=? and %s=?", DbHelper.IDPAGINA, DbHelper.IDCHAT), new String[]{idPage, idChat}, null, null, null);
        if(cursor.moveToFirst()){
            chatRoom.setIdPages(cursor.getString(0));
            chatRoom.setId(cursor.getString(1));
            chatRoom.setAuthor(cursor.getString(2));
            chatRoom.setName(cursor.getString(3));
            chatRoom.setTimestamp(cursor.getString(4));
            chatRoom.setPRIVATE(cursor.getString(5));
            chatRoom.setType(Config.GROUP_CHAT_ROOM);
        }
        cursor.close();
        //db.close();
        return chatRoom;
    }
    public ArrayList<ChatRoom> getChatRooms(String idPage){
        ArrayList<ChatRoom> chatRooms =  new ArrayList<>();

        String[] keys = new String[]{ idPage };

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String last_date = String.format(", (select max(%s) from %s where %s = c.%s and %s = c.%s and %s = _replace_) as last_date",
                DbHelper.DATA_CREAZIONE, DbHelper.TABLE_MESSAGES,
                DbHelper.IDPAGINA, DbHelper.IDPAGINA,
                DbHelper.ID_DESTINAZIONE, DbHelper.IDCHAT,
                DbHelper.TIPO_DESTINAZIONE);

        //DbHelper.TABLE_CHAT
        //Tutte le chat room publiche di quella pagina
        String Table1 = String.format("select c.*, %s as tipo" +
                last_date.replace("_replace_", "%1$s") +
                " from %s as c where c.%s = %s and c.%s = 0",
                Config.GROUP_CHAT_ROOM, DbHelper.TABLE_CHAT,
                DbHelper.IDPAGINA, idPage,
                DbHelper.PRIVATE);

        //Tutte le chat private del'utente logato dentro una specifica pagina
        String Table2 = String.format("select c.*, %s as tipo " +
                last_date.replace("_replace_", "%1$s") +
                " from %s as c inner join %s as cu on c.%4$s = cu.%4$s and c.%5$s = cu.%5$s where c.%4$s = %6$s and cu.%7$s = %8$s",
                Config.GROUP_CHAT_ROOM, DbHelper.TABLE_CHAT, DbHelper.TABLE_CHAT_UTENTI,
                DbHelper.IDPAGINA, DbHelper.IDCHAT, idPage, DbHelper.IDUTENTE,
                MyApplication.getInstance().getPrefManager().getUser().getId());

        //Tutte le chat singole di una specifica pagina
        //1) Tutti gli utenti che mi hanno scritto almeno un messaggio
        String qry1 = String.format("select %s, %s from %s where %1$s = %s and %s = %s",
                DbHelper.IDPAGINA, DbHelper.IDUTENTE,
                DbHelper.QRY_SINGLE_USERS_CHAT,
                idPage, DbHelper.ID_DESTINAZIONE, MyApplication.getInstance().getPrefManager().getUser().getId());

        //2) Tutti gli utenti ai qualli ho scritto almeno un messagio
        String qry2 = String.format("select %s, %s as %s from %s where %1$s = %s and %s = %s",
                DbHelper.IDPAGINA, DbHelper.ID_DESTINAZIONE, DbHelper.IDUTENTE,
                DbHelper.QRY_SINGLE_USERS_CHAT,
                idPage, DbHelper.IDUTENTE, MyApplication.getInstance().getPrefManager().getUser().getId());

        last_date = String.format(", (select max(%s) from %s where %s = t.%s and (%s = t.%6$s or %6$s = t.%6$s) and %7$s = _replace_) as last_date",
                DbHelper.DATA_CREAZIONE, DbHelper.TABLE_MESSAGES,
                DbHelper.IDPAGINA, DbHelper.IDPAGINA,
                DbHelper.ID_DESTINAZIONE, DbHelper.IDUTENTE,
                DbHelper.TIPO_DESTINAZIONE);

        //3) union le due qry precedenti join con utenti per il titolo
        String Table3 = String.format("select t.%s, t.%s as %s, '' as %2$s, u.%s as %s, null as %s, 0 as %s, %s as tipo " +
                last_date.replace("_replace_", "%8$s") +
                " from (%s union %s)t inner join %s u on t.%12$s = u.%12$s ",
                DbHelper.IDPAGINA,
                DbHelper.IDUTENTE, DbHelper.IDCHAT,
                DbHelper.NOME, DbHelper.TITOLO,
                DbHelper.DATA_CREAZIONE,
                DbHelper.PRIVATE,
                Config.SINGLE_CHAT_ROOM,
                qry1, qry2, DbHelper.TABLE_UTENTI, DbHelper.IDUTENTE);

        Cursor cursor = db.rawQuery("select * from (" + Table1 + " union " + Table2 + " union " + Table3 + ")t order by last_date desc", null);
        //Cursor cursor = db.rawQuery(Table2, null);
        if(cursor.moveToFirst()){
            do{
                ChatRoom chatRoom = new ChatRoom();
                chatRoom.setIdPages(cursor.getString(0));
                chatRoom.setId(cursor.getString(1));
                chatRoom.setAuthor(cursor.getString(2));
                chatRoom.setName(cursor.getString(3));
                chatRoom.setTimestamp(cursor.getString(4));
                chatRoom.setPRIVATE(cursor.getString(5));
                chatRoom.setType(cursor.getString(6));
                chatRoom.setUnreadCount(MyApplication.getInstance().getPrefManager().getUnReadCounter(chatRoom.getIdPage(), chatRoom.getId(), chatRoom.getType()));
                chatRoom.setLastMessage(getLastMessage(chatRoom));
                chatRooms.add(chatRoom);
            }
            while(cursor.moveToNext());
        }

        cursor.close();
        //db.close();
        return chatRooms;
    }

    //Messages
    public String getLastMessage(ChatRoom chatRoom){
        String msg = null;
        String where;
        if(chatRoom.getType().equals(Config.SINGLE_CHAT_ROOM))
            where = String.format("%s=? and (%s=? or %s=%s) and %s=?",
                    DbHelper.IDPAGINA, DbHelper.ID_DESTINAZIONE,
                    DbHelper.IDUTENTE, chatRoom.getId(), DbHelper.TIPO_DESTINAZIONE);
        else
            where = String.format("%s=? and %s=? and %s=?", DbHelper.IDPAGINA, DbHelper.ID_DESTINAZIONE, DbHelper.TIPO_DESTINAZIONE);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DbHelper.TABLE_MESSAGES,
                new String[]{DbHelper.DESCRIZIONE},
                where,
                new String[]{chatRoom.getIdPage(), chatRoom.getId(), chatRoom.getType()},
                null, null,
                String.format("%s desc", DbHelper.DATA_CREAZIONE),
                "1");
        if(cursor.moveToFirst())
            msg = cursor.getString(0);
        //db.close();
        return msg;
    }
    public int deleteMessages(String idPage, String idChat, String Type){
        String Where;
        if(Type.equals(Config.GROUP_CHAT_ROOM)){
            Where = String.format("%s = ? and %s = ? and %s = ?",
                    DbHelper.IDPAGINA,
                    DbHelper.ID_DESTINAZIONE,
                    DbHelper.TIPO_DESTINAZIONE);
        }
        else{
            Where = String.format("%s = ? and (%s = ? or %s = %s) and %s = ?",
                    DbHelper.IDPAGINA,
                    DbHelper.ID_DESTINAZIONE,
                    DbHelper.IDUTENTE,
                    idChat,
                    DbHelper.TIPO_DESTINAZIONE);
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DbHelper.TABLE_MESSAGES, Where, new String[]{
                idPage,idChat,Type
        });
        //db.close();
        return rows;
    }
    public int deleteMessages(String idPage){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DbHelper.TABLE_MESSAGES, DbHelper.IDPAGINA + "=?", new String[]{idPage});
        //db.close();
        return rows;
    }
    public long addMessage(Message message){
        ContentValues values = new ContentValues();
        values.put(DbHelper.IDPAGINA, message.getIdPage());
        values.put(DbHelper.IDUTENTE, message.getUser().getId());
        values.put(DbHelper.ID_DESTINAZIONE, message.getIdChat());
        values.put(DbHelper.TIPO_DESTINAZIONE, message.getType());
        values.put(DbHelper.DESCRIZIONE, message.getMessage());
        //values.put(DbHelper.DATA_CREAZIONE, message.getCreatedAt());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(DbHelper.TABLE_MESSAGES, null, values);
        //db.close();
        return id;
    }
    public ArrayList<Message> getMessages(String idPage, String idChat, String type, int offset){
        ArrayList<Message> messages = new ArrayList<>();
        String Table, Where, OrderBy, OffsetAndLimit;
        String[] Columns, Key;

        Columns = new String[]{
                "m." + DbHelper.IDPAGINA,
                "m." + DbHelper.IDUTENTE,
                "ifnull(u." + DbHelper.NOME + ", '" + context.getResources().getString(R.string.unknown) + "')",
                "m." + DbHelper.ID_DESTINAZIONE,
                "m." + DbHelper.IDMESSAGGIO,
                "m." + DbHelper.DESCRIZIONE,
                "datetime(m." + DbHelper.DATA_CREAZIONE + ", 'localtime') as " + DbHelper.DATA_CREAZIONE
        };
        OrderBy = String.format("m.%s desc", DbHelper.DATA_CREAZIONE);
        OffsetAndLimit = String.format("%s, %s", offset, Config.LIMIT);
        if(type.equals(Config.SINGLE_CHAT_ROOM)){
            //Singolo utente
            Table = String.format("%s as m inner join %s as u on m.%s = u.%3$s",
                    DbHelper.TABLE_MESSAGES, DbHelper.TABLE_UTENTI, DbHelper.IDUTENTE);

            Where = String.format("m.%s = ? and m.%s = ? and ((m.%3$s = %5$s and m.%4$s = %6$s) or (m.%3$s = %6$s and m.%4$s = %5$s)) ",
                    DbHelper.IDPAGINA,
                    DbHelper.TIPO_DESTINAZIONE,
                    DbHelper.ID_DESTINAZIONE,
                    DbHelper.IDUTENTE,
                    idChat, //he
                    MyApplication.getInstance().getPrefManager().getUser().getId()); //me

            Key = new String[]{ idPage, type };
        }
        else
        {
            //gruppo
            Key = new String[]{ idPage, idChat, type };

            Table = String.format("%1$s as c " +
                    "inner join %2$s as m on c.%3$s = m.%3$s and c.%4$s = m.%5$s " +
                    "left outer join utenti u on m.%6$s = u.%6$s",
                    DbHelper.TABLE_CHAT, DbHelper.TABLE_MESSAGES, //Table
                    DbHelper.IDPAGINA, DbHelper.IDCHAT, DbHelper.ID_DESTINAZIONE, DbHelper.IDUTENTE); // Fields
            Where = String.format("c.%s = ? and c.%s = ? and m.%s = ?",
                    DbHelper.IDPAGINA, DbHelper.IDCHAT, DbHelper.TIPO_DESTINAZIONE);



        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(Table, Columns, Where, Key, null, null, OrderBy, OffsetAndLimit);

        if(cursor.moveToFirst()){
            do{
                Message message = new Message();
                User user = new User();
                message.setIdPage(cursor.getString(0));
                user.setId(cursor.getString(1));
                user.setName(cursor.getString(2));

                message.setIdChat(cursor.getString(3));
                message.setId(cursor.getString(4));
                message.setMessage(cursor.getString(5));
                message.setCreatedAt(cursor.getString(6));
                message.setUser(user);
                messages.add(message);
            }
            while(cursor.moveToNext());
        }

        //db.close();
        cursor.close();
        return messages;
    }

    //Roles
    public ArrayList<Role> getRoleList(String idPage){
        return null;
        //TODO
    }
    public Role getRole(String idPage, String idUser){
        /*
        * Example:
        * select pr.* from pagineutenti pu inner join pagineruoli pr on pu.idpagina = pu.idpagina and pu.idruolo = pr.idruolo
        * where pu.idpagina = 1 and pu.idutente = 3
        * */
        Role role = new Role();
        String[] Columns = new String[]{"pr.*"};
        String Where = String.format("pu.%s = ? and pu.%s = ?", DbHelper.IDPAGINA,DbHelper.IDUTENTE);
        String[] Key = new String[]{idPage, idUser};
        String Table = String.format("%s pu inner join %s pr on pu.%s = pr.%3$s and pu.%4$s = pr.%4$s",
                DbHelper.TABLE_PAGINE_UTENTI, DbHelper.TABLE_PAGINE_RUOLI,
                DbHelper.IDPAGINA, DbHelper.IDRUOLO);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(Table, Columns, Where, Key, null, null, "1");
        if(cursor.moveToFirst()){
            do{
                role.setIdPage(cursor.getString(0));
                role.setId(cursor.getString(1));
                role.setDescrizione(cursor.getString(2));
                role.setCanCreatePub(cursor.getInt(3) > 0);
                role.setCanCreatePriv(cursor.getInt(4) > 0);
                role.setCanBan(cursor.getInt(5) > 0);
                role.setCanDeletePub(cursor.getInt(6) > 0);
                role.setCanDeletePriv(cursor.getInt(7) > 0);
            }
            while(cursor.moveToNext());
        }

        cursor.close();
        //db.close();
        return role;
    }

    @Nullable
    public ContentValues setValues(String table, JSONObject obj){
        try {
            ContentValues values = new ContentValues();
            switch (table) {
                case DbHelper.TABLE_PAGINE:
                    values.put(DbHelper.IDPAGINA, obj.getString(DbHelper.IDPAGINA));
                    values.put(DbHelper.TITOLO, obj.getString(DbHelper.TITOLO));
                    values.put(DbHelper.DESCRIZIONE, obj.getString(DbHelper.DESCRIZIONE));
                    values.put(DbHelper.DATA_CREAZIONE, obj.getString(DbHelper.DATA_CREAZIONE));
                    values.put(DbHelper.PASSWORD, obj.getString(DbHelper.PASSWORD));
                    break;
                case DbHelper.TABLE_PAGINE_RUOLI:
                    values.put(DbHelper.IDPAGINA, obj.getString(DbHelper.IDPAGINA));
                    values.put(DbHelper.IDRUOLO, obj.getString(DbHelper.IDRUOLO));
                    values.put(DbHelper.DESCRIZIONE, obj.getString(DbHelper.DESCRIZIONE));
                    values.put(DbHelper.CREATE_PUB_ROOM, obj.getString(DbHelper.CREATE_PUB_ROOM));
                    values.put(DbHelper.CREATE_PRIV_ROOM, obj.getString(DbHelper.CREATE_PRIV_ROOM));
                    values.put(DbHelper.CREATE_BAN, obj.getString(DbHelper.CREATE_BAN));
                    values.put(DbHelper.DEL_PUB_ROOM, obj.getString(DbHelper.DEL_PUB_ROOM));
                    values.put(DbHelper.DEL_PRIV_ROOM, obj.getString(DbHelper.DEL_PRIV_ROOM));
                    break;
                case DbHelper.TABLE_UTENTI:
                    values.put(DbHelper.IDUTENTE, obj.getString(DbHelper.IDUTENTE));
                    values.put(DbHelper.NOME, obj.getString(DbHelper.NOME));
                    break;
                case DbHelper.TABLE_PAGINE_UTENTI:
                    values.put(DbHelper.IDPAGINA, obj.getString(DbHelper.IDPAGINA));
                    values.put(DbHelper.IDUTENTE, obj.getString(DbHelper.IDUTENTE));
                    values.put(DbHelper.IDRUOLO, obj.getString(DbHelper.IDRUOLO));
                    values.put(DbHelper.DATA_CREAZIONE, obj.getString(DbHelper.DATA_CREAZIONE));
                    break;
                case DbHelper.TABLE_CHAT:
                    values.put(DbHelper.IDPAGINA, obj.getString(DbHelper.IDPAGINA));
                    values.put(DbHelper.IDCHAT, obj.getString(DbHelper.IDCHAT));
                    values.put(DbHelper.IDUTENTE, obj.getString(DbHelper.IDUTENTE));
                    values.put(DbHelper.TITOLO, obj.getString(DbHelper.TITOLO));
                    values.put(DbHelper.DATA_CREAZIONE, obj.getString(DbHelper.DATA_CREAZIONE));
                    values.put(DbHelper.PRIVATE, obj.getString(DbHelper.PRIVATE));
                    break;
                case DbHelper.TABLE_CHAT_UTENTI:
                    values.put(DbHelper.IDPAGINA, obj.getString(DbHelper.IDPAGINA));
                    values.put(DbHelper.IDCHAT, obj.getString(DbHelper.IDCHAT));
                    values.put(DbHelper.IDUTENTE, obj.getString(DbHelper.IDUTENTE));
                    break;
            }
            return values;
        }
        catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }

    public void Refresh(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DbHelper.TABLE_PAGINE, null, null);
        db.delete(DbHelper.TABLE_PAGINE_RUOLI, null, null);
        db.delete(DbHelper.TABLE_UTENTI, null, null);
        db.delete(DbHelper.TABLE_PAGINE_UTENTI, null, null);
        db.delete(DbHelper.TABLE_CHAT, null, null);
        db.delete(DbHelper.TABLE_CHAT_UTENTI, null, null);
    }
}
