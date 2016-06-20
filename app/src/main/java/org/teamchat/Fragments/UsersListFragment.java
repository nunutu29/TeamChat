package org.teamchat.Fragments;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.teamchat.Activity.MainActivity;
import org.teamchat.Adapter.UsersAdapter;
import org.teamchat.App.Config;
import org.teamchat.App.EndPoints;
import org.teamchat.App.MyApplication;
import org.teamchat.Helper.DbHelper;
import org.teamchat.Model.ChatRoom;
import org.teamchat.Model.Role;
import org.teamchat.R;
import org.teamchat.Helper.SimpleDividerItemDecoration;
import org.teamchat.Helper.Utils;
import org.teamchat.Model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class UsersListFragment extends Fragment {
    private static final String TAG = UsersListFragment.class.getSimpleName();
    //Modalita per decidere degli eventi
    public static final int WRITE_MODE = 1;
    public static final int LOOK_MODE = 2;
    //Modalita attuale
    private static final String F_MODE = "_mode_";
    private int MODE;

    private TextInputLayout textInputLayout;
    private EditText insertedTitle;
    private Button newPublicChat;
    private FloatingActionButton b;

    private RecyclerView recyclerView;
    private ArrayList<User> userArrayList;
    private ArrayList<String> selectedUsers;
    private UsersAdapter usersAdapter;

    private String idPage, idChat;
    boolean selectMode;



    private OnFragmentInteractionListener mListener;

    public UsersListFragment() {
        // Required empty public constructor
    }

    public static UsersListFragment newInstance(String idPage, String idChat, int mode) {
        UsersListFragment fragment = new UsersListFragment();
        Bundle args = new Bundle();
        args.putString(Config.IDPAGINA, idPage);
        args.putString(Config.IDCHAT, idChat);
        args.putInt(F_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idPage = getArguments().getString(Config.IDPAGINA);
            idChat = getArguments().getString(Config.IDCHAT);
            MODE = getArguments().getInt(F_MODE);
        }
        selectedUsers = new ArrayList<>();
        userArrayList = new ArrayList<>();
        usersAdapter = new UsersAdapter(getContext(), userArrayList);
        LocalLoad();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_users_list, container, false);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        if(MODE == LOOK_MODE)
            toolbar.setTitle(getResources().getString(R.string.users_list));
        else
            toolbar.setTitle(getResources().getString(R.string.new_chat_room));
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        insertedTitle = (EditText) rootView.findViewById(R.id.txtInsertTitle);
        textInputLayout = (TextInputLayout)rootView.findViewById(R.id.input_layout_titolo);
        textInputLayout.setVisibility(View.GONE);

        b = (FloatingActionButton) rootView.findViewById(R.id.btn_send);
        b.setVisibility(View.GONE);

        newPublicChat = (Button)rootView.findViewById(R.id.new_pub_chat_room);
        newPublicChat.setVisibility(View.GONE);


        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView = (RecyclerView)rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(usersAdapter);

        //Questo va fatto qui, in quanto le view vanno prima inizializzate
        switch (MODE){
            case WRITE_MODE:
                WriteEvents();
                break;
            case LOOK_MODE:
                break;
        }

        return  rootView;
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

    @Override
    public void onResume(){
        super.onResume();
        ((MainActivity)getActivity()).CURRENT_FRAGMENT = Config.USERS_LIST_FRAGMENT;
    }

    @Override
    public void onPause(){
        super.onPause();
        if(Utils.isSoftKeyboardShowing(getActivity()))
            Utils.hideSoftKeyboard(getActivity());
    }

    private void CreateChatRoom(final String title, final String EndPoint,final String type){
        //Type says if its private or public
        StringRequest strReq = new StringRequest(Request.Method.POST, EndPoint, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error
                    if (!obj.getBoolean("error")) {
                            String  a = obj.getString(DbHelper.IDPAGINA),
                                    b = obj.getString(DbHelper.IDCHAT),
                                    c = obj.getString(DbHelper.TITOLO);
                        ChatRoom chatRoom = new ChatRoom();
                        chatRoom.setIdPages(a);
                        chatRoom.setId(b);
                        chatRoom.setAuthor(MyApplication.getInstance().getPrefManager().getUser().getId());
                        chatRoom.setName(c);
                        chatRoom.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                        chatRoom.setPRIVATE(type);

                        MyApplication.getInstance().getDbManager().addChatRooms(chatRoom);

                        if(type.equals(Config.CHAT_ROOM_PRIVATE)){
                            //aggiungere me stesso prima
                            ContentValues values = new ContentValues();
                            values.put(DbHelper.IDPAGINA, a);
                            values.put(DbHelper.IDCHAT, b);
                            values.put(DbHelper.IDUTENTE, MyApplication.getInstance().getPrefManager().getUser().getId());
                            MyApplication.getInstance().getDbManager().execInsert(DbHelper.TABLE_CHAT_UTENTI, values);
                            values.clear();
                            //definire gli utenti della chatroom
                            for (String id : selectedUsers) {
                                values.put(DbHelper.IDPAGINA, a);
                                values.put(DbHelper.IDCHAT, b);
                                values.put(DbHelper.IDUTENTE, id);
                                MyApplication.getInstance().getDbManager()
                                        .execInsert(DbHelper.TABLE_CHAT_UTENTI, values);
                                values.clear();
                            }
                        }

                        mListener.OpenChatRoom(a,b,Config.GROUP_CHAT_ROOM);
                    } else
                    {
                        Toast.makeText(getContext(), "" + obj.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getContext(), "json parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                Toast.makeText(getContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("idpagina", idPage);
                params.put("titolo", title);
                params.put("idutente", MyApplication.getInstance().getPrefManager().getUser().getId());
                if(EndPoint.equals(EndPoints.CHAT_ROOM_CREATE_PRIV))
                   params.put("user_list", creaListUtenti().toString());
                Log.e(TAG, "Params: " + params.toString());
                return params;
            }
        };


        // disabling retry policy so that it won't make
        // multiple http calls
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        strReq.setRetryPolicy(policy);

        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }

    private JSONObject creaListUtenti(){
        JSONObject tmp;
        JSONArray tmpArray = new JSONArray();
        for (String user : selectedUsers) {
            tmp = new JSONObject();
            try{
                tmp.put("id", user);
            }
            catch (JSONException ex){
                ex.printStackTrace();
            }
            tmpArray.put(tmp);
        }
        JSONObject finalObj = new JSONObject();
        try {
            finalObj.put("user_list", tmpArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return finalObj;
    }

    public boolean onBackPressed(){
        if(!newPublicChat.isClickable()){
            newPublicChat.setClickable(true);
            if(selectedUsers.size() == 0) {
                textInputLayout.setVisibility(View.GONE);
                b.setVisibility(View.GONE);
            }
            recyclerView.setVisibility(View.VISIBLE);
            return false;
        }


        return true;
    }

    public void LocalLoad(){
        userArrayList.clear();
        if(idChat == null)
            userArrayList.addAll(MyApplication.getInstance().getDbManager().getUsersList(idPage));
        else {
            ChatRoom chatRoom = MyApplication.getInstance().getDbManager().getChatRoom(idPage, idChat);
            if(chatRoom.getPRIVATE().equals(Config.CHAT_ROOM_PRIVATE))
                userArrayList.addAll(MyApplication.getInstance().getDbManager().getUsersList(idPage, idChat));
            else
                userArrayList.addAll(MyApplication.getInstance().getDbManager().getUsersList(idPage));
        }
        usersAdapter.notifyDataSetChanged();
    }

    private void WriteEvents(){
        final Role role = ((MainActivity)getActivity()).getRole();

        usersAdapter.setClickListener(new UsersAdapter.ClickListener(){
            @Override
            public void onClick(View v, int pos){
                if(!selectMode) {
                    User utente = userArrayList.get(pos);
                    mListener.OpenChatRoom(idPage, utente.getId(), Config.SINGLE_CHAT_ROOM);
                }
                else {
                    if(onLongClick(v, pos))
                        selectMode = false;
                }
            }
            @Override
            public boolean onLongClick(View v, int pos){
                if(!role.getCanCreatePriv()){
                    return true;
                }
                selectMode = true;
                String id = userArrayList.get(pos).getId();
                if(selectedUsers.contains(id)){
                    v.setSelected(false);
                    selectedUsers.remove(id);

                    if(selectedUsers.size() == 0)
                    {
                        b.setVisibility(View.GONE);
                        textInputLayout.setVisibility(View.GONE);
                        return true;
                    }
                }
                else
                {
                    v.setSelected(true);
                    selectedUsers.add(id);
                    b.setVisibility(View.VISIBLE);
                    textInputLayout.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

        //se si puo fare private o publiche
        if(role.getCanCreatePriv() || role.getCanCreatePub()) {
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (insertedTitle.getText().toString().trim().equals("")) {
                        textInputLayout.setErrorEnabled(true);
                        textInputLayout.setError(getString(R.string.err_msg_generic));
                    } else {
                        textInputLayout.setErrorEnabled(false);
                        if (!newPublicChat.isClickable())
                            CreateChatRoom(insertedTitle.getText().toString().trim(), EndPoints.CHAT_ROOM_CREATE_PUB, Config.CHAT_ROOM_PUBLIC);
                        else {
                            CreateChatRoom(insertedTitle.getText().toString().trim(), EndPoints.CHAT_ROOM_CREATE_PRIV, Config.CHAT_ROOM_PRIVATE);
                        }
                    }
                }
            });
        }

        if(role.getCanCreatePub()) {
            newPublicChat.setVisibility(View.VISIBLE);
            newPublicChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newPublicChat.setClickable(false);
                    b.setVisibility(View.VISIBLE);
                    textInputLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);

                    if(textInputLayout.requestFocus())
                        Utils.showSoftKeyboard(getActivity(), insertedTitle);
                }
            });
        }

    }

    public interface OnFragmentInteractionListener {
        void OpenChatRoom(String idPage, String user, String type);
    }
}
