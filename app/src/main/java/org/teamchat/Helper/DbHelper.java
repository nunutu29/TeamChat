package org.teamchat.Helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.teamchat.App.Config;

/**
 * Created by Johnny on 30/04/2016.
 * Tutte le tabelle tranne i messaggi sono solo a scopo di cache
 */
public class DbHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "teamchat.db";
    private static final int DATABASE_VERSION = 18;
    //TYPES
    public static final String VARCHAR_TYPE = " varchar";
    public static final String INT_TYPE = " integer";
    public static final String TINYINT_TYPE = " tinyint";
    public static final String BIT_TYPE = " bit";
    public static final String TIMESTAMP_TYPE = " timestamp";
    public static final String TEXT_TYPE = " text";
    public static final String _NULL = " null";
    public static final String _NOT_NULL = " not null";

    //Table
    public static final String TABLE_PAGINE = "pagine";
    public static final String TABLE_UTENTI = "utenti";
    public static final String TABLE_PAGINE_UTENTI = "pagineutenti";
    public static final String TABLE_CHAT = "chatrooms";
    public static final String TABLE_CHAT_UTENTI = "chatroomsutenti";
    public static final String TABLE_MESSAGES = "messages";
    public static final String QRY_SINGLE_USERS_CHAT = "QryAllSingleChatUser";
    public static final String TABLE_PAGINE_RUOLI = "pagineruoli";
    //Columns
    //public static final String ID = "_id";
    //Pagine
    public static final String IDPAGINA = "idpagina";
    public static final String TITOLO = "titolo";
    public static final String DESCRIZIONE = "descrizione";
    public static final String DATA_CREAZIONE = "data_creazione";
    public static final String PASSWORD = "password";
    //PagineRuoli
    //idpagina, descrizione
    public static final String IDRUOLO = "idruolo";
    public static final String CREATE_PUB_ROOM = "create_pub_room";
    public static final String CREATE_PRIV_ROOM  = "create_priv_room";
    public static final String CREATE_BAN = "create_ban";
    public static final String DEL_PUB_ROOM = "del_pub_room";
    public static final String DEL_PRIV_ROOM = "del_priv_room";

    //Utenti
    public static final String IDUTENTE= "idutente";
    public static final String NOME = "nome";
    public static final String USERNAME = "username";
    //PagineUtenti
    //idpagina, idutente, idruolo, data_creazione



    //ChatRooms
    //idpagina, idutente, titolo, descrizione, data_creazione
    public static final String IDCHAT = "idchat";
    public static final String PRIVATE = "private";
    //ChatRoomsUtenti
    //idpagina, idchat, idutente

    //Messaggi
    //idpagina, idutente, descrizione, data_creazione
    public static final String ID_DESTINAZIONE = "id_destinazione";
    public static final String TIPO_DESTINAZIONE = "tipo_destinazione"; //se utente o gruppo
    public static final String IDMESSAGGIO = "idmessaggio";

    //region Create Table
    private static final String PAGINE_CREATE = "create table " + TABLE_PAGINE
            + "("
            + IDPAGINA + INT_TYPE + _NOT_NULL + ","
            + TITOLO + VARCHAR_TYPE + "(100)" + _NOT_NULL + ","
            + DESCRIZIONE + VARCHAR_TYPE + "(255)" + _NULL + ","
            + DATA_CREAZIONE + TIMESTAMP_TYPE + _NOT_NULL + " default current_timestamp,"
            + PASSWORD + VARCHAR_TYPE + "(100)" + _NULL + ","
            + "primary key(" +IDPAGINA  + ")"
            + ")";

    private static final String PAGINE_RUOLI_CREATE = "create table " + TABLE_PAGINE_RUOLI
            + "("
            + IDPAGINA + INT_TYPE + _NOT_NULL + ","
            + IDRUOLO + TINYINT_TYPE + _NOT_NULL + ","
            + DESCRIZIONE + VARCHAR_TYPE + "20" + _NOT_NULL + ","
            + CREATE_PUB_ROOM + BIT_TYPE + _NOT_NULL + ","
            + CREATE_PRIV_ROOM + BIT_TYPE + _NOT_NULL + ","
            + CREATE_BAN + BIT_TYPE + _NOT_NULL + ","
            + DEL_PUB_ROOM + BIT_TYPE + _NOT_NULL + ","
            + DEL_PRIV_ROOM + BIT_TYPE + _NOT_NULL + ","
            + "primary key(" + IDPAGINA + ", " + IDRUOLO + ")"
            + "foreign key (" + IDPAGINA + ") references " + TABLE_PAGINE + " (" + IDPAGINA + ") on delete cascade on update cascade"
            + ")";

    private static final String UTENTI_CREATE = "create table " + TABLE_UTENTI
            + "("
            + IDUTENTE + INT_TYPE + _NOT_NULL + ","
            + NOME + VARCHAR_TYPE + "(255)" + _NOT_NULL + ","
            + "primary key(" + IDUTENTE  + ")"
            + ")";

    private static final String PAGINE_UTENTI_CREATE = "create table " + TABLE_PAGINE_UTENTI
            + "("
            + IDPAGINA + INT_TYPE + _NOT_NULL + ","
            + IDUTENTE + INT_TYPE + _NOT_NULL + ","
            + IDRUOLO + INT_TYPE + _NOT_NULL + " default 0,"
            + DATA_CREAZIONE + TIMESTAMP_TYPE + _NOT_NULL + ","
            + "primary key(" + IDPAGINA + ", " + IDUTENTE + ")"
            + "foreign key (" + IDPAGINA + ") references " + TABLE_PAGINE + " (" + IDPAGINA + ") on delete cascade on update cascade,"
            + "foreign key (" + IDUTENTE + ") references " + TABLE_UTENTI + " (" + IDUTENTE + ") on delete cascade on update cascade,"
            + "foreign key (" + IDPAGINA + ", " + IDRUOLO + ") references " + TABLE_PAGINE_RUOLI + " (" + IDPAGINA + ", " + IDRUOLO + ") on delete no action on update no action"
            + ")";

    private static final String CHAT_ROOMS_CREATE = "create table " + TABLE_CHAT
            + "("
            + IDPAGINA + INT_TYPE + _NOT_NULL + ","
            + IDCHAT + INT_TYPE + _NOT_NULL + ","
            + IDUTENTE + INT_TYPE + _NOT_NULL + ","
            + TITOLO + VARCHAR_TYPE + "(100)" + _NOT_NULL + ","
            + DATA_CREAZIONE + TIMESTAMP_TYPE + _NOT_NULL + ","
            + PRIVATE + INT_TYPE + _NOT_NULL + " default 0,"
            + "primary key(" + IDPAGINA + ", " + IDCHAT + ")"
            + "foreign key (" + IDPAGINA + ") references " + TABLE_PAGINE + " (" + IDPAGINA + ") on delete cascade on update cascade,"
            + "foreign key (" + IDUTENTE + ") references " + TABLE_UTENTI + " (" + IDUTENTE + ") on delete cascade on update cascade"
            + ")";

    private static final String CHAT_ROOMS_UTENTI_CREATE = "create table " + TABLE_CHAT_UTENTI
            + "("
            + IDPAGINA + INT_TYPE + _NOT_NULL + ","
            + IDCHAT + INT_TYPE + _NOT_NULL + ","
            + IDUTENTE + INT_TYPE + _NOT_NULL + ","
            + "primary key(" + IDPAGINA + ", " + IDCHAT + ", " + IDUTENTE + ")"
            + "foreign key (" + IDPAGINA + ", " + IDUTENTE + ") references " + TABLE_PAGINE_UTENTI + " (" + IDPAGINA + ", " + IDUTENTE + ") on delete cascade on update cascade,"
            + "foreign key (" + IDPAGINA + ", " + IDCHAT + ") references " + TABLE_CHAT + " (" + IDPAGINA + ", " + IDCHAT + ") on delete cascade on update cascade"
            + ")";

    //id destinazione puo essere o singolo utente, oppure un gruppo
    //va gestito a codice se si vuole cancellare una chatroom
    private static final String MESSAGGI_CREATE = "create table if not exists " + TABLE_MESSAGES
            + "("
            + IDPAGINA + INT_TYPE + _NOT_NULL + ","
            + IDUTENTE + INT_TYPE + _NOT_NULL + ","
            + ID_DESTINAZIONE + INT_TYPE + _NOT_NULL + ","
            + TIPO_DESTINAZIONE + INT_TYPE + _NOT_NULL + ","
            + IDMESSAGGIO + INT_TYPE + " primary key autoincrement,"
            + DESCRIZIONE + TEXT_TYPE + _NOT_NULL + ","
            + DATA_CREAZIONE + TIMESTAMP_TYPE + _NOT_NULL + " default current_timestamp"
            + ")";
    private static final String INDEX_MESSAGES = "create index if not exists messages_idpagina on " +
            DbHelper.TABLE_MESSAGES + "(" + DbHelper.IDPAGINA + ")";

    private static final String QRY_SINGLE_CHAT_CREATE = "create view " + QRY_SINGLE_USERS_CHAT + " as "
            + " select distinct "
            + IDPAGINA + "," + IDUTENTE + "," + ID_DESTINAZIONE
            + " from " + TABLE_MESSAGES
            + " where " + TIPO_DESTINAZIONE + " = " + Config.SINGLE_CHAT_ROOM;
    //endregion

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        //Creare le tabelle qui
        db.execSQL(PAGINE_CREATE);
        db.execSQL(PAGINE_RUOLI_CREATE);
        db.execSQL(UTENTI_CREATE);
        db.execSQL(PAGINE_UTENTI_CREATE);
        db.execSQL(CHAT_ROOMS_CREATE);
        db.execSQL(CHAT_ROOMS_UTENTI_CREATE);
        db.execSQL(MESSAGGI_CREATE);
        db.execSQL(INDEX_MESSAGES);
        db.execSQL(QRY_SINGLE_CHAT_CREATE);
    }

    @Override
    public void onOpen(SQLiteDatabase db){
        super.onOpen(db);
        //Chiavi esterne
        if(!db.isReadOnly())
            db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAGINE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAGINE_RUOLI);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UTENTI);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAGINE_UTENTI);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_UTENTI);
        db.execSQL("DROP VIEW IF EXISTS " + QRY_SINGLE_USERS_CHAT);
        onCreate(db);
    }

}