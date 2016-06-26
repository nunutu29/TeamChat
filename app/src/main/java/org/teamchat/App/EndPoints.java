package org.teamchat.App;

public class EndPoints {
    //Digital Ocean IP
    public static final String BASE_URL = "http://188.166.167.140/TeamChat/v1";
    //Local Host Ips
    //public static final String BASE_URL = "http://78.12.68.241/TeamChat/v1";
    //public static final String BASE_URL = "http://192.168.1.100/TeamChat/v1";

    //Variabili Utili
    public static final String IDPAGINA = "_IDP_";
    public static final String IDCHAT = "_IDC_";
    public static final String IDUTENTE = "_IDU_";

    //Pagine
    public static final String PAGE = String.format("%s/page", BASE_URL);
    public static final String PAGE_CREATE = String.format("%s/create", PAGE);
    public static final String PAGE_LEAVE = String.format("%s/leave", PAGE);
    public static final String PAGE_DELETE = String.format("%s/delete", PAGE);
    public static final String PAGE_ADD = String.format("%s/addUser", PAGE);
    //public static final String PAGE_GET = String.format("%s/user/%s", PAGE, IDUTENTE);

    //Chat Rooms
    public static final String CHAT_ROOM = String.format("%s/ChatRoom", BASE_URL);
    public static final String CHAT_ROOM_CREATE_PUB = String.format("%s/create/pub", CHAT_ROOM);
    public static final String CHAT_ROOM_CREATE_PRIV = String.format("%s/create/priv", CHAT_ROOM);
    public static final String CHAT_ROOM_DELETE = String.format("%s/delete", CHAT_ROOM);
    //public static final String CHAT_ROOM_GET = String.format("%s/read/%s/%s", CHAT_ROOM, IDPAGINA, IDUTENTE);
    public static final String LEAVE_CHATROOM = String.format("%s/leave", CHAT_ROOM);
    //Utenti
    public static final String USER = String.format("%s/user", BASE_URL);
    public static final String USER_GET_AVATAR = String.format("%s/img/users", BASE_URL);
    public static final String USER_SET_AVATAR = String.format("%s/avatar/change", USER);
    public static final String USER_LOGIN = String.format("%s/login", USER);
    public static final String USER_REGISTER = String.format("%s/register", USER);
    public static final String USER_GCM = String.format("%s/gcm/%s", USER, IDUTENTE);

    //Messaggi
    //quando si fa un get bisonga aggiungere l'offset alla stringa
    //../message/50
    public static final String CHAT_ROOM_MESSAGE = String.format("%s/%s/ChatRoom/%s/message", PAGE, IDPAGINA, IDCHAT);
    public static final String SINGLE_CHAT_ROOM_SEND_MESSAGE = String.format("%s/%s/PrivateChatRoom/send/message", PAGE, IDPAGINA);
   // public static final String SINGLE_CHAT_ROOM_GET_MESSAGE = String.format("%s/%s/PrivateChatRoom/get", PAGE, IDPAGINA); //concatenare utenti e offset
    //public static final String CHAT_ROOM_MESSAGE_GET = CHAT_ROOM_MESSAGE + "/get";

    //per sync
    public static final String SYNC = String.format("%s/application/Sync/%s", BASE_URL, IDUTENTE);
}
