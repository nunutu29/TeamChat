package org.teamchat.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.teamchat.Activity.MainActivity;
import org.teamchat.Adapter.ChatRoomsAdapter;
import org.teamchat.App.Config;
import org.teamchat.App.MyApplication;
import org.teamchat.Helper.DbHelper;
import org.teamchat.Helper.DbIntentService;
import org.teamchat.Helper.Utils;
import org.teamchat.Model.Message;
import org.teamchat.Model.Pages;
import org.teamchat.PopUp.MyPopUp;
import org.teamchat.R;
import org.teamchat.Helper.SimpleDividerItemDecoration;
import org.teamchat.Model.ChatRoom;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatListFragment extends Fragment {
    private String idPage;
    private String Title;
    private ImageView avatar;

    private OnFragmentInteractionListener mListener;
    private DrawerLayout drawerLayout;
    private ChatRoomsAdapter mAdapter;
    private ArrayList<ChatRoom> chatRoomArrayList;

    public ChatListFragment() {
        // Required empty public constructor
    }

    public static ChatListFragment newInstance(String pagina) {
        ChatListFragment fragment = new ChatListFragment();
        Bundle args = new Bundle();
        args.putString(Config.IDPAGINA, pagina);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idPage = getArguments().getString(Config.IDPAGINA);
            Title = MyApplication.getInstance().getDbManager().getPage(idPage).getTitle();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat_list, container, false);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setTitle(Title);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) rootView.findViewById(R.id.drawerLayout);
        NavigationView navigationMenu = (NavigationView) rootView.findViewById(R.id.NavigationView);
        ManageItems(navigationMenu);

        FloatingActionButton floatingActionButton = (FloatingActionButton)rootView.findViewById(R.id.btn_new_chat);
        if(floatingActionButton != null)
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.OpenUsers(idPage, null, UsersListFragment.WRITE_MODE);
                }
            });
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer){

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        chatRoomArrayList = new ArrayList<>();
        mAdapter = new ChatRoomsAdapter(chatRoomArrayList);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new ChatRoomsAdapter.RecyclerTouchListener(getContext(),
                recyclerView, new ChatRoomsAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                // when chat is clicked, launch full chat thread
                ChatRoom chatRoom = chatRoomArrayList.get(position);
                mListener.OpenChatRoom(chatRoom.getIdPage(),chatRoom.getId(), chatRoom.getType());
            }

            @Override
            public void onLongClick(View view, int position) {
                //Creare solo la prima volta..
                final ChatRoom chatRoom = chatRoomArrayList.get(position);
                MyPopUp popUp = beforeShowPopUp(chatRoom);
                popUp.show(((MainActivity)getActivity()).getRootElement());
            }
        }));
        return rootView;
    }

    private void ManageItems(NavigationView navigationMenu) {
        View headerView = navigationMenu.getHeaderView(0);
        avatar = (ImageView) headerView.findViewById(R.id.drawer_top_image);
        if(avatar != null) {
            MyApplication.getInstance().getPrefManager().getUser().setAvatar(getContext(), avatar, false);
            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = Utils.OpenGalleryIntent();
                    startActivityForResult(intent, Config.PICK_IMAGE_REQUEST);
                }
            });
        }

        TextView username = (TextView)headerView.findViewById(R.id.drawer_username);
        if(username != null)
            username.setText(MyApplication.getInstance().getPrefManager().getUser().getName());

        Menu menu = navigationMenu.getMenu();
        //hide leave button for admins.
        menu.findItem(R.id.drawer_leave_group).setVisible(
                !((MainActivity)getActivity()).getRole().isMain()
        );
        menu.findItem(R.id.drawer_delete_group).setVisible(
                ((MainActivity)getActivity()).getRole().isMain()
        );
        navigationMenu.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                //item.setChecked(!item.isChecked());
                drawerLayout.closeDrawers();
                return onOptionsItemSelected(item);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        ((MainActivity)getActivity()).CURRENT_FRAGMENT = Config.CHAT_LIST_FRAGMENT;
        LoadLocal();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void LoadLocal(){
        if(chatRoomArrayList == null) return;
        chatRoomArrayList.clear();
        chatRoomArrayList.addAll(MyApplication.getInstance().getDbManager().getChatRooms(idPage));
        mAdapter.notifyDataSetChanged();
    }

    public void UpdateItem(Message message, String type){
        String id = message.getIdChat();
        if(type.equals(Config.SINGLE_CHAT_ROOM))
            id = message.getUser().getId();

        ChatRoom chatRoom = ChatRoom.getChatRoom(idPage, id, type, chatRoomArrayList);
        if(chatRoom == null) {
            if(type.equals(Config.SINGLE_CHAT_ROOM)){
                ChatRoom newChatRoom = new ChatRoom();
                newChatRoom.setIdPages(message.getIdPage());
                newChatRoom.setId(message.getUser().getId());
                newChatRoom.setType(type);
                newChatRoom.setName(message.getUser().getName());
                newChatRoom.setUnreadCount(1);
                newChatRoom.setLastMessage(message.getMessage());
                AddToChatList(newChatRoom);
                return;
            }
            else
                return;
        }
        int index = chatRoomArrayList.indexOf(chatRoom);
        chatRoom.setUnreadCount(chatRoom.getUnreadCount() + 1);
        chatRoom.setLastMessage(message.getMessage());
        chatRoomArrayList.remove(index);
        mAdapter.notifyItemRemoved(index);
        AddToChatList(chatRoom);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog alertDialog;
        switch (item.getItemId()) {
            //case R.id.drawer_settings:
                //Toast.makeText(getContext(), "Settings", Toast.LENGTH_SHORT).show();
              //  return true;
            case R.id.drawer_pages:
                mListener.PopFragment();
                return true;
            case R.id.drawer_users_list:
                drawerLayout.closeDrawer(GravityCompat.START);
                mListener.OpenUsers(idPage, null, UsersListFragment.LOOK_MODE);
                return true;
            case R.id.drawer_group_credentials:
                Pages page = MyApplication.getInstance().getDbManager().getPage(idPage);
                alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle(getResources().getString(R.string.drawer_group_credentials));
                alertDialog.setMessage("ID: " + page.getId() + "\nPassword: " + page.getPass());
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                return true;
            case R.id.drawer_leave_group:
                alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setMessage("Sei sicuro di voler uscire ?");
                alertDialog.setTitle(getResources().getString(R.string.drawer_leave_group));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getContext(), DbIntentService.class);
                        intent.putExtra(DbIntentService.KEY, DbIntentService.LEAVE_PAGE);
                        intent.putExtra(DbHelper.IDPAGINA, idPage);
                        getActivity().startService(intent);
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
                return true;
            case R.id.drawer_delete_group:
                alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setMessage("Sei sicuro di voler eliminare la pagina ?");
                alertDialog.setTitle(getResources().getString(R.string.drawer_delete_group));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getContext(), DbIntentService.class);
                        intent.putExtra(DbIntentService.KEY, DbIntentService.DELETE_PAGE);
                        intent.putExtra(DbHelper.IDPAGINA, idPage);
                        getActivity().startService(intent);
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onBackPressed(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
            return false;
        }
        else
            return true;
    }

    public void AddToChatList(ChatRoom chatRoom){
        chatRoomArrayList.add(0, chatRoom);
        mAdapter.notifyItemInserted(0);
    }

    public void removeFromChatList(ChatRoom chatRoom){
        ChatRoom newChatRoom = ChatRoom.getChatRoom(chatRoom.getIdPage(), chatRoom.getId(), chatRoom.getType(), chatRoomArrayList);
        int index = chatRoomArrayList.indexOf(newChatRoom);
        if(index > -1) {
            chatRoomArrayList.remove(index);
            mAdapter.notifyItemRemoved(index);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK || data == null) return;

        if (requestCode == Config.PICK_IMAGE_REQUEST && data.getData() != null) {
            Uri filePath = data.getData();
            Intent intent = Utils.PerformCropIntent(filePath);
            if(intent != null)
                startActivityForResult(intent, Config.CROP_IMAGE_REQUEST);
            else
            {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                    bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true);
                    avatar.setImageBitmap(bitmap);
                    ChangeAvatar(bitmap);
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        else if(requestCode == Config.CROP_IMAGE_REQUEST){
            Bundle extras = data.getExtras();
            Bitmap bitmap = extras.getParcelable("data");
            if(bitmap != null && bitmap.getWidth() > 350)
                bitmap = Bitmap.createScaledBitmap(bitmap, 350, 350, true);
            avatar.setImageBitmap(bitmap);
            ChangeAvatar(bitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void ChangeAvatar(Bitmap bitmap){
        Intent intent = new Intent(getContext(), DbIntentService.class);
        intent.putExtra(DbIntentService.KEY, DbIntentService.CHANGE_AVATAR);
        intent.putExtra("BitmapImage", bitmap);
        getActivity().startService(intent);
    }

    private MyPopUp beforeShowPopUp(final ChatRoom chatRoom){
        final ArrayList<MyPopUp.Values> arrayList = new ArrayList<>();

        arrayList.add(new MyPopUp.Values(Config.POPUP_DELETE_HISTORY,
                getResources().getString(R.string.popup_delete_hisory)));


        //se silenzioso mostra Suona, altrimenti viceversa
        if(MyApplication.getInstance().getPrefManager()
                .checkSilentRoom(chatRoom.getIdPage(), chatRoom.getId(), chatRoom.getType())) {
            arrayList.add(new MyPopUp.Values(Config.POPUP_LOUD_CHAT,
                    getResources().getString(R.string.popup_loud_chat)));
        }
        else {
            arrayList.add(new MyPopUp.Values(Config.POPUP_SILENT_CHAT,
                    getResources().getString(R.string.popup_silent_chat)));
        }

        if(chatRoom.getType().equals(Config.GROUP_CHAT_ROOM) &&
                chatRoom.getPRIVATE().equals(Config.CHAT_ROOM_PRIVATE)) {
            arrayList.add(new MyPopUp.Values(Config.POPUP_LEAVE_CHAT,
                    getResources().getString(R.string.popup_leave_chat)));
        }
        //abilito l'elimina solo per i gruppi, e poi guardo il ruolo del'utente e le sue abilitazioni
        if(chatRoom.getType().equals(Config.GROUP_CHAT_ROOM) &&
                ((chatRoom.getPRIVATE().equals(Config.CHAT_ROOM_PRIVATE) &&
                        ((MainActivity)getActivity()).getRole().getCanDeletePriv()) ||
                        (chatRoom.getPRIVATE().equals(Config.CHAT_ROOM_PUBLIC) &&
                                ((MainActivity)getActivity()).getRole().getCanDeletePub())))
            arrayList.add(new MyPopUp.Values(Config.POPUP_DELETE_CHAT,
                    getResources().getString(R.string.popup_admin_delete_chat)));

        //aggiungere nuova azione Can Edit
        //arrayList.add(new MyPopUp.Values(Config.POPUP_EDIT_CHAT,
        // getResources().getString(R.string.popup_admin_edit_chat)));

        //Ora che l'elenco genero il popUp
        final MyPopUp popUp = new MyPopUp(getContext(), arrayList);

        popUp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                MyPopUp.Values item = arrayList.get(position);
                switch (item.getKey()){
                    case Config.POPUP_DELETE_HISTORY:
                        int res = MyApplication.getInstance().getDbManager()
                                .deleteMessages(chatRoom.getIdPage(), chatRoom.getId(), chatRoom.getType());
                        Toast.makeText(getContext(),
                                res > 0 ? String.format("Deleted %s rows", res) : "No rows deleted.",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Config.POPUP_LEAVE_CHAT:
                    case Config.POPUP_DELETE_CHAT:
                        String action = item.getKey() == Config.POPUP_LEAVE_CHAT ?
                                DbIntentService.LEAVE_CHATROOM : DbIntentService.DELETE_CHATROOM;
                        Intent intent = new Intent(getContext(), DbIntentService.class);
                        intent.putExtra(DbIntentService.KEY, action);
                        intent.putExtra(DbHelper.TABLE_CHAT, chatRoom);
                        getActivity().startService(intent);
                        break;
                    case Config.POPUP_EDIT_CHAT:
                        Toast.makeText(getContext(), "edit", Toast.LENGTH_SHORT).show();
                        break;
                    case Config.POPUP_SILENT_CHAT:
                        MyApplication.getInstance().getPrefManager().storeSilentRoom(chatRoom.getIdPage(),
                                chatRoom.getId(), chatRoom.getType());
                        break;
                    case Config.POPUP_LOUD_CHAT:
                        MyApplication.getInstance().getPrefManager().removeSilentRoom(chatRoom.getIdPage(),
                                chatRoom.getId(), chatRoom.getType());
                        break;
                }
                popUp.close();
            }
        });
        return popUp;
    }

    public interface OnFragmentInteractionListener {
        void OpenChatRoom(String idPage, String idChat, String type);
        void OpenUsers(String idPage, String idChat, int mode);
        void PopFragment();
    }
}
