package org.teamchat.App;

/**
 * Created by Johnny
 */
public class Config {

    //Intents
    public static int PICK_IMAGE_REQUEST = 1;
    public static int CROP_IMAGE_REQUEST = 2;
    //Directory
    public static String IMG_USER = "img_users";

    // flag to identify whether to show single line
    // or multi line test push notification tray
    public static boolean appendNotificationMessages = true;

    // global topic to receive app wide push notifications
    //public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";
    public static final String SYNC_COMPLETE = "syncComplete";

    // type of push messages
    public static final int PUSH_TYPE_CHATROOM = 1;
    public static final int PUSH_TYPE_USER = 2;
    public static final int PUSH_FLAG_USER_LEFT_PAGE = 3;
    public static final int PUSH_FLAG_USER_LEFT_CHAT = 4;
    public static final int PUSH_FLAG_CHAT_DELETE = 5;
    public static final int PUSH_FLAG_PAGE_DELETE = 6;
    public static final int PUSH_FLAG_CHAT_CREATED = 7;
    public static final int PUSH_FLAG_USER_JOIN_PAGE = 8;
    public static final int PUSH_FLAG_USER_AVATAR_CHANGE = 9;


    // id to handle the notification in the notification try
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    //type of rooms
    public static final String GROUP_CHAT_ROOM = "1";
    public static final String SINGLE_CHAT_ROOM = "0";
    public static final String CHAT_ROOM_PRIVATE = "1";
    public static final String CHAT_ROOM_PUBLIC = "0";

    //My Fragments IDs
    public static final String FRAGMENT_ID = "_idFragment_";
    public static final int GROUPS_LIST_FRAGMENT = 1;
    public static final int CHAT_LIST_FRAGMENT = 2;
    public static final int CHAT_ROOM_FRAGMENT = 3;
    public static final int USERS_LIST_FRAGMENT = 4;
    public static final int CREATE_GROUP_FRAGMENT = 5;
    public static final int SINGLE_CHAT_ROOM_FRAGMENT = 6;
    //other
    public static final int LIMIT = 50;
    public static final String IDPAGINA = "_IDPAGINA_";
    public static final String IDCHAT = "_IDCHAT_";
    //public static final String FRAGMENT_TITLE = "_TITLE_";
    public static final String IDUTENTE = "_IDUTENTE_";

    //PopUp Keys
    public static final int POPUP_DELETE_HISTORY = 1;
    public static final int POPUP_LEAVE_CHAT = 2;
    public static final int POPUP_EDIT_CHAT = 3;
    public static final int POPUP_DELETE_CHAT = 4;
    public static final int POPUP_SILENT_CHAT = 5;
    public static final int POPUP_LOUD_CHAT = 6;
}
