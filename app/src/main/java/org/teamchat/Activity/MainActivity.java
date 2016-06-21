package org.teamchat.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.teamchat.Fragments.ChatListFragment;
import org.teamchat.Fragments.ChatRoomFragment;
import org.teamchat.Fragments.CreatePageFragment;
import org.teamchat.Fragments.GroupListFragment;
import org.teamchat.Fragments.SingleChatRoomFragment;
import org.teamchat.Fragments.UsersListFragment;
import org.teamchat.Helper.DbHelper;
import org.teamchat.Model.ChatRoom;
import org.teamchat.Model.Pages;
import org.teamchat.Model.Role;
import org.teamchat.R;
import org.teamchat.App.Config;
import org.teamchat.App.MyApplication;
import org.teamchat.GCM.GcmIntentService;
import org.teamchat.GCM.NotificationUtils;
import org.teamchat.Helper.DbIntentService;
import org.teamchat.Model.Message;

/**
 * Commit Test
 * Attenzione pulire idpage che non serve da per tutto
 * */
public class MainActivity extends AppCompatActivity
        implements
        GroupListFragment.OnFragmentInteractionListener,
        ChatListFragment.OnFragmentInteractionListener,
        ChatRoomFragment.OnFragmentInteractionListener,
        SingleChatRoomFragment.OnFragmentInteractionListener,
        CreatePageFragment.OnFragmentInteractionListener,
        UsersListFragment.OnFragmentInteractionListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    //Current Fragment
    public int CURRENT_FRAGMENT;
    //Used Fragments
    private GroupListFragment groupListFragment;
    private ChatListFragment chatListFragment;
    private ChatRoomFragment chatRoomFragment;
    private CreatePageFragment createPageFragment;
    private UsersListFragment usersListFragment;
    private SingleChatRoomFragment singleChatRoomFragment;
    //Variables
    private String idPage, oldIdPage, idChat;
    private int userListMode;
    private Role role;
    //root Element
    private FrameLayout rootElement;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    public FrameLayout getRootElement(){
        return rootElement;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null)
            RestoreInstanceState(savedInstanceState);
        /**
         * Controlla Prima Che L'Utente Sia Loggatto
         * Se No, lancia la Login Activity
         * */
        if ( MyApplication.getInstance().getPrefManager().getUser() == null) {
            launchLoginActivity();
        }

        setContentView(R.layout.activity_main);
        rootElement = (FrameLayout) findViewById(R.id.fragment_container);

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                shouldDisplayHomeUp();
            }
        });

        if (savedInstanceState == null)
            CheckForFragmentAndStart();

        /**
         * Broadcast receiver viene chiamato in due scenari
         * 1. Quando la registrazione del GCM è completa
         * 2. Quando un nuovo Push è arrivato
         * */
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    //this method is only inside GroupListFragment
                    if(CURRENT_FRAGMENT == Config.GROUPS_LIST_FRAGMENT)
                        groupListFragment.subscribeToAllTopics();

                } else if (intent.getAction().equals(Config.SENT_TOKEN_TO_SERVER)) {
                    // gcm registration id is stored in our server's MySQL
                    Log.e(TAG, "GCM registration id is sent to our server");

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    handlePushNotification(intent);
                } else if (intent.getAction().equals(Config.SYNC_COMPLETE)){
                    handleSyncComplete(intent);
                }
            }
        };

        /**
         * Always check for google play services availability before
         * proceeding further with GCM
         * */
        if (savedInstanceState == null && checkPlayServices()) {
            registerGCM();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        shouldDisplayHomeUp();
        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // register sync complete
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.SYNC_COMPLETE));

        // clearing the notification tray
        NotificationUtils.clearNotifications();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private void handleSyncComplete(Intent intent) {
        String action = intent.getStringExtra("action");
        switch (action){
            case DbIntentService.SYNC:
                if(CURRENT_FRAGMENT == Config.GROUPS_LIST_FRAGMENT && groupListFragment != null)
                    groupListFragment.LoadLocal();
                break;
            case DbIntentService.LEAVE_CHATROOM:
            case DbIntentService.DELETE_CHATROOM:
                if(CURRENT_FRAGMENT  == Config.CHAT_LIST_FRAGMENT && chatListFragment != null)
                    chatListFragment.LoadLocal();
                break;
            case DbIntentService.LEAVE_PAGE:
            case DbIntentService.DELETE_PAGE:
                PopFragment();
                break;
        }
    }

    private void handlePushNotification(Intent intent) {
        int type = intent.getIntExtra("type", -1);
        switch (type){
            case Config.PUSH_TYPE_USER:
            case Config.PUSH_TYPE_CHATROOM:
                handleMessage(intent, type);
                break;
            case Config.PUSH_FLAG_CHAT_DELETE:
            case Config.PUSH_FLAG_USER_LEFT_CHAT:
            case Config.PUSH_FLAG_CHAT_CREATED:
                handleChatRoomAction(intent, type);
                break;
            case Config.PUSH_FLAG_PAGE_DELETE:
            case Config.PUSH_FLAG_USER_LEFT_PAGE:
            case Config.PUSH_FLAG_USER_JOIN_PAGE:
                handlePageAction(intent, type);
                break;

        }
    }

    private void handlePageAction(Intent intent, int type){
        Pages page = (Pages)intent.getSerializableExtra(DbHelper.TABLE_PAGINE);
        //per ora idpage sotto non viene utilizzato, quindi va bene anche che sia null
        if(idPage != null && !idPage.equals(page.getId())) return;
        switch (type){
            case Config.PUSH_FLAG_PAGE_DELETE:
                switch (CURRENT_FRAGMENT){
                    case Config.GROUPS_LIST_FRAGMENT:
                        if(groupListFragment != null)
                            groupListFragment.LoadLocal();
                        break;
                    default:
                        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        PopFragment();
                        Toast.makeText(MainActivity.this, page.getTitle() + " has been deleted.", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            case Config.PUSH_FLAG_USER_LEFT_PAGE:
            case Config.PUSH_FLAG_USER_JOIN_PAGE:
                String predicate = type == Config.PUSH_FLAG_USER_LEFT_PAGE ? "left" : "joined";
                Toast.makeText(MainActivity.this,
                        String.format("%s %s %s", intent.getStringExtra(DbHelper.NOME), predicate, page.getTitle()),
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void handleChatRoomAction(Intent intent, int type){
        ChatRoom chatRoom = (ChatRoom) intent.getSerializableExtra(DbHelper.TABLE_CHAT);
        //se la modifica avviene per una pagina diversa da quella attuale, allora niente.
        if(idPage == null || !idPage.equals(chatRoom.getIdPage()))
            return;

        switch (type){
            case Config.PUSH_FLAG_CHAT_DELETE:
                switch (CURRENT_FRAGMENT){
                    case Config.CHAT_LIST_FRAGMENT:
                        if(chatListFragment != null)
                            chatListFragment.removeFromChatList(chatRoom);
                        break;
                    case Config.CHAT_ROOM_FRAGMENT:
                        //Qui siamo dentro un gruppo
                        if(idChat != null && idChat.equals(chatRoom.getId())) {
                            PopFragment();
                            Toast.makeText(MainActivity.this, "This chat has been deleted.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                break;
            case Config.PUSH_FLAG_USER_LEFT_CHAT:
                Toast.makeText(MainActivity.this, String.format("%s left %s",
                        intent.getStringExtra(DbHelper.NOME),
                        chatRoom.getName()), Toast.LENGTH_SHORT).show();
                break;
            case Config.PUSH_FLAG_CHAT_CREATED:
                switch (CURRENT_FRAGMENT){
                    case Config.CHAT_LIST_FRAGMENT:
                        if(chatListFragment != null)
                            chatListFragment.AddToChatList(chatRoom);
                        break;
                }
                break;
        }
    }

    private void handleMessage(Intent intent, int type){
        Message message = (Message) intent.getSerializableExtra("MESSAGGIO");
        if (message.getIdPage() != null) {
            switch (CURRENT_FRAGMENT){
                case Config.GROUPS_LIST_FRAGMENT:
                    if(groupListFragment != null)
                        groupListFragment.UpdateCounters(message.getIdPage());
                    break;
                case Config.CHAT_LIST_FRAGMENT:
                    String t;
                    if(type == Config.PUSH_TYPE_CHATROOM)
                        t = Config.GROUP_CHAT_ROOM;
                    else
                        t = Config.SINGLE_CHAT_ROOM;
                    if(chatListFragment != null)
                        chatListFragment.UpdateItem(message, t);
                    break;
                case Config.CHAT_ROOM_FRAGMENT:
                    if(type == Config.PUSH_TYPE_CHATROOM)
                        if(chatRoomFragment != null)
                            chatRoomFragment.UpdateView(message);
                    break;
                case Config.SINGLE_CHAT_ROOM_FRAGMENT:
                    if(type == Config.PUSH_TYPE_USER)
                        if(singleChatRoomFragment != null)
                            singleChatRoomFragment.UpdateView(message);
                    break;
            }
        }
    }

    private void launchLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //Sync BackEnd with SqlLite
    public void StartSync(){
        Intent intent = new Intent(this, DbIntentService.class);
        intent.putExtra(DbIntentService.KEY, DbIntentService.SYNC);
        startService(intent);
    }

    // starting the service to register with GCM
    private void registerGCM() {
        Intent intent = new Intent(this, GcmIntentService.class);
        intent.putExtra("key", "register");
        startService(intent);
    }

    //controllare sempre se ci sono i play services
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported. Google Play Services not installed!");
                Toast.makeText(getApplicationContext(), "This device is not supported. Google Play Services not installed!", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    //Modificare il menu in base al fragment
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();

        switch (CURRENT_FRAGMENT){
            case Config.GROUPS_LIST_FRAGMENT:
                inflater.inflate(R.menu.menu_main, menu);
                break;
            case Config.CHAT_ROOM_FRAGMENT:
                inflater.inflate(R.menu.menu_chatroom, menu);
                break;
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case android.R.id.home:
                //Trigger back Button
                this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                return true;
            case R.id.action_create:
                OpenCreateGroup();
                return true;
            case R.id.action_sync:
                StartSync();
                return true;
            case R.id.action_users:
                OpenUsers(idPage, idChat, UsersListFragment.LOOK_MODE);
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void onFragmentInteraction(Uri uri){

    }

    public Role getRole(){
        if(role != null && idPage.equals(oldIdPage))
            return this.role;
        else
        {
            oldIdPage = idPage;
            role = MyApplication.getInstance().getDbManager()
                    .getRole(idPage, MyApplication.getInstance().getPrefManager().getUser().getId());
            return role;
        }
    }

    /**
     * Metodi chiamati dalla Chat List
     * */
    public void OpenChatRoom(String A, String B, String C){
        idPage = A;
        idChat = B; //in caso di Single chat room, questo è l'id dell'utente
        if(C.equals(Config.GROUP_CHAT_ROOM))
            CURRENT_FRAGMENT = Config.CHAT_ROOM_FRAGMENT;
        else
            CURRENT_FRAGMENT = Config.SINGLE_CHAT_ROOM_FRAGMENT;
        startFragment(false, false);
    }
    public void OpenUsers(String A, String B, int mode){
        CURRENT_FRAGMENT = Config.USERS_LIST_FRAGMENT;
        idPage = A;
        idChat = B;
        userListMode = mode;
        startFragment(false, false);
    }

    /**
     * Metodi Chiamati dalla Group List
     * */
    public void OpenCreateGroup(){
        CURRENT_FRAGMENT = Config.CREATE_GROUP_FRAGMENT;
        startFragment(false, false);
    }
    public void OpenChatList(String A){
        idPage = A;
        CURRENT_FRAGMENT = Config.CHAT_LIST_FRAGMENT;
        startFragment(false, false);
    }


    //apre il fragment impostato nella CURRENT_FRAGMENT
    private void startFragment(boolean firstTime, boolean reverse){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(!firstTime) {
            transaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.pot_enter, R.anim.pop_exit);
            transaction.addToBackStack(String.valueOf(CURRENT_FRAGMENT));
        }else{
            if(reverse)
                transaction.setCustomAnimations(R.anim.pot_enter, R.anim.pop_exit, R.anim.enter, R.anim.exit);
        }
        switch (CURRENT_FRAGMENT){
            case Config.GROUPS_LIST_FRAGMENT: //This is Main Fragment, that never can be second.
                groupListFragment = new GroupListFragment();
                groupListFragment.setArguments(getIntent().getExtras());
                transaction.replace(R.id.fragment_container, groupListFragment);
                break;
            case Config.CHAT_LIST_FRAGMENT:
                chatListFragment = ChatListFragment.newInstance(idPage);
                transaction.replace(R.id.fragment_container, chatListFragment);
                break;
            case Config.CHAT_ROOM_FRAGMENT:
                chatRoomFragment = ChatRoomFragment.newInstance(idPage, idChat);
                transaction.replace(R.id.fragment_container, chatRoomFragment);
                break;
            case Config.CREATE_GROUP_FRAGMENT:
                createPageFragment = new CreatePageFragment();
                transaction.replace(R.id.fragment_container, createPageFragment);
                break;
            case Config.USERS_LIST_FRAGMENT:
                usersListFragment = UsersListFragment.newInstance(idPage, idChat, userListMode);
                transaction.replace(R.id.fragment_container, usersListFragment);
                break;
            case Config.SINGLE_CHAT_ROOM_FRAGMENT:
                //Attenzione: idChat si riferisce all'idutente
                singleChatRoomFragment = SingleChatRoomFragment.newInstance(idPage,idChat);
                transaction.replace(R.id.fragment_container, singleChatRoomFragment);
                break;
        }
        transaction.commit();
    }

    //Controlla se ci sono qualche degli extra passati
    private void CheckForFragmentAndStart(){
        Intent intent = getIntent();
        CURRENT_FRAGMENT = intent.getIntExtra(Config.FRAGMENT_ID, 1);
        switch (CURRENT_FRAGMENT){
            case Config.CHAT_LIST_FRAGMENT:
                idPage = intent.getStringExtra(Config.IDPAGINA);
                break;
            case Config.CHAT_ROOM_FRAGMENT:
                idPage = intent.getStringExtra(Config.IDPAGINA);
                idChat = intent.getStringExtra(Config.IDCHAT);
                break;
            case Config.SINGLE_CHAT_ROOM_FRAGMENT:
                idPage = intent.getStringExtra(Config.IDPAGINA);
                idChat = intent.getStringExtra(Config.IDUTENTE);
                break;
        }
        startFragment(true, false);
    }

    //Va indietro di un fragment
    public void PopFragment(){
        if(getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
        else
        {
            CURRENT_FRAGMENT = Config.GROUPS_LIST_FRAGMENT;
            startFragment(true, true);
        }
    }

    /**
     * Return true se non fatto nulla, false se ha fatto qualcosa.
     * */
    public boolean PopFragment(int ID) {
        return getSupportFragmentManager().getBackStackEntryCount() <= 0 || !getSupportFragmentManager().popBackStackImmediate(String.valueOf(ID), 0);
    }

    //Mostrare il back solo se ci sono piu di un fragment nella stack
    public void shouldDisplayHomeUp(){
        if(AllowBackButton() && getSupportActionBar() != null) {
            boolean canback = getSupportFragmentManager().getBackStackEntryCount() > 0;
            getSupportActionBar().setDisplayHomeAsUpEnabled(canback);
        }
    }

    private boolean AllowBackButton(){
        switch (CURRENT_FRAGMENT){
            case Config.CHAT_LIST_FRAGMENT:
                return false;
            default:
                return true;
        }
    }

    @Override
    public void onBackPressed() {
        boolean execSuper = true;
        switch (CURRENT_FRAGMENT){
            case Config.CHAT_LIST_FRAGMENT:
                if(chatListFragment != null)
                    execSuper = chatListFragment.onBackPressed();
                break;
            case Config.CHAT_ROOM_FRAGMENT:
            case Config.SINGLE_CHAT_ROOM_FRAGMENT:
                execSuper = PopFragment(Config.CHAT_LIST_FRAGMENT);
                break;
            case Config.USERS_LIST_FRAGMENT:
                if(usersListFragment != null)
                    execSuper = usersListFragment.onBackPressed();
                break;
        }
        if(execSuper)
            super.onBackPressed();
    }

    //in caso di cambiamenti l'activity viene distrutta e fatta ripartire percio dobbiamo salvare lo stato attuale
    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString(DbHelper.IDPAGINA, idPage);
        outState.putString("oldidpage", oldIdPage);
        outState.putString(DbHelper.IDCHAT, idChat);
        outState.putSerializable("role", role);
        outState.putInt("currentFrag", CURRENT_FRAGMENT);


        //I fragment
        if(groupListFragment != null && groupListFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, String.valueOf(Config.GROUPS_LIST_FRAGMENT), groupListFragment );
        if(chatListFragment != null && chatListFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, String.valueOf(Config.CHAT_LIST_FRAGMENT), chatListFragment );
        if(chatRoomFragment != null && chatRoomFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, String.valueOf(Config.CHAT_ROOM_FRAGMENT), chatRoomFragment );
        if(createPageFragment != null && createPageFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, String.valueOf(Config.CREATE_GROUP_FRAGMENT), createPageFragment );
        if(usersListFragment != null && usersListFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, String.valueOf(Config.USERS_LIST_FRAGMENT), usersListFragment );
        if(singleChatRoomFragment != null && singleChatRoomFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, String.valueOf(Config.SINGLE_CHAT_ROOM_FRAGMENT), singleChatRoomFragment );
    }
    public void RestoreInstanceState(Bundle inState){
        idPage = inState.getString(DbHelper.IDPAGINA);
        oldIdPage = inState.getString("oldidpage");
        idChat = inState.getString(DbHelper.IDCHAT);
        role = (Role)inState.getSerializable("role");
        CURRENT_FRAGMENT = inState.getInt("currentFrag");
        //Restore Fragments
        groupListFragment = (GroupListFragment) getSupportFragmentManager().getFragment(inState, String.valueOf(Config.GROUPS_LIST_FRAGMENT));
        chatListFragment = (ChatListFragment) getSupportFragmentManager().getFragment(inState, String.valueOf(Config.CHAT_LIST_FRAGMENT));
        chatRoomFragment = (ChatRoomFragment) getSupportFragmentManager().getFragment(inState, String.valueOf(Config.CHAT_ROOM_FRAGMENT));
        createPageFragment = (CreatePageFragment) getSupportFragmentManager().getFragment(inState, String.valueOf(Config.CREATE_GROUP_FRAGMENT));
        usersListFragment = (UsersListFragment) getSupportFragmentManager().getFragment(inState, String.valueOf(Config.USERS_LIST_FRAGMENT));
        singleChatRoomFragment = (SingleChatRoomFragment) getSupportFragmentManager().getFragment(inState, String.valueOf(Config.SINGLE_CHAT_ROOM_FRAGMENT));
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //MyApplication.getInstance().getDbManager().close();
    }
}
