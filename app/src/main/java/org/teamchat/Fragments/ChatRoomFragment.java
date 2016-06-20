package org.teamchat.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.teamchat.Activity.MainActivity;
import org.teamchat.Adapter.ChatRoomThreadAdapter;
import org.teamchat.App.Config;
import org.teamchat.App.EndPoints;
import org.teamchat.App.InfiniteScroll;
import org.teamchat.App.MyApplication;
import org.teamchat.R;
import org.teamchat.Helper.Utils;
import org.teamchat.Model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatRoomFragment extends Fragment {
    private static final String TAG = ChatRoomFragment.class.getSimpleName();

    private String idPage, idChat, Title;
    private RecyclerView recyclerView;
    private ChatRoomThreadAdapter mAdapter;
    private ArrayList<Message> messageArrayList;
    private EditText inputMessage;
    private Button btnSend;
    private OnFragmentInteractionListener mListener;

    public ChatRoomFragment() {
        // Required empty public constructor
    }

    public static ChatRoomFragment newInstance(String pagina, String chat) {
        ChatRoomFragment fragment = new ChatRoomFragment();
        Bundle args = new Bundle();
        args.putString(Config.IDPAGINA, pagina);
        args.putString(Config.IDCHAT, chat);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idPage = getArguments().getString(Config.IDPAGINA);
            idChat = getArguments().getString(Config.IDCHAT);
            Title = MyApplication.getInstance().getDbManager().getChatRoom(idPage, idChat).getName();
        }
        //Adpater declaration and data Loading
        messageArrayList = new ArrayList<>();
        String selfUserId = MyApplication.getInstance().getPrefManager().getUser().getId();
        mAdapter = new ChatRoomThreadAdapter(messageArrayList, selfUserId, Config.GROUP_CHAT_ROOM);
        if(idChat != null) {
            MyApplication.getInstance().getPrefManager().deleteUnreadCounter(idPage, idChat, Config.GROUP_CHAT_ROOM);
            LocalLoad(0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat_room, container, false);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setTitle(Title);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);


        inputMessage = (EditText) rootView.findViewById(R.id.message);
        inputMessage.addTextChangedListener(new MyTextWatcher(inputMessage));
        btnSend = (Button) rootView.findViewById(R.id.btn_send);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(null);
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnScrollListener(new InfiniteScroll(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                LocalLoad(mAdapter.getItemCount());
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        return rootView;
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
        ((MainActivity)getActivity()).CURRENT_FRAGMENT = Config.CHAT_ROOM_FRAGMENT;
        super.onResume();
        if(idPage != null && idChat != null)
            MyApplication.getInstance().setCurrentActivity(idPage + "_" + idChat + "_" + Config.GROUP_CHAT_ROOM);
    }

    @Override
    public void onPause(){
        super.onPause();
        MyApplication.getInstance().setCurrentActivity(null);
        if(Utils.isSoftKeyboardShowing(getActivity()))
            Utils.hideSoftKeyboard(getActivity());
    }

    private void sendMessage() {
        //Check internet
        if(!Utils.hasInternet(getContext()))
            return;

        final Message message = new Message();
        message.setIdPage(idPage);
        message.setIdChat(idChat);
        message.setType(Config.GROUP_CHAT_ROOM);
        message.setMessage(this.inputMessage.getText().toString().trim());

        if (TextUtils.isEmpty(message.getMessage())) {
            return;
        }
        this.inputMessage.setText("");

        message.setUser(MyApplication.getInstance().getPrefManager().getUser());
        message.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        messageArrayList.add(0, message);

        mAdapter.notifyItemInserted(0);
        if (mAdapter.getItemCount() > 1)
            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, 0);

        //lettura indice di questo messaggio per aggiornamento dell'id
        //final int indexNew = messageArrayList.indexOf(message);
        final int indexNew = 0;
        long id = MyApplication.getInstance().getDbManager().addMessage(message);
        message.setId(String.valueOf(id));
        messageArrayList.remove(indexNew);
        messageArrayList.add(indexNew, message);
        //add new column need_push with default false. but here insert with true
        StringRequest strReq = new StringRequest(Request.Method.POST,
                EndPoints.CHAT_ROOM_MESSAGE.replace(EndPoints.IDPAGINA, idPage).replace(EndPoints.IDCHAT, idChat),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "response: " + response);

                        try {
                            JSONObject obj = new JSONObject(response);

                            // check for error
                            if (!obj.getBoolean("error")) {
                                //Update need_push to false

                            } else {
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
                inputMessage.setText(message.getMessage());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("IDUTENTE", MyApplication.getInstance().getPrefManager().getUser().getId());
                params.put("DESCRIZIONE", message.getMessage());

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

    private void LocalLoad(int offset){
        messageArrayList.addAll(MyApplication.getInstance().getDbManager()
                .getMessages(idPage, idChat, Config.GROUP_CHAT_ROOM, offset)
        );
        mAdapter.notifyItemRangeInserted(mAdapter.getItemCount(), messageArrayList.size() - 1);
    }

    public void UpdateView(Message message){
        String p = message.getIdPage();
        String cr = message.getIdChat();
        if (p.equals(idPage) && cr.equals(idChat)) {
            messageArrayList.add(0, message);
            mAdapter.notifyItemInserted(0);
            if (mAdapter.getItemCount() > 1)
                recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, 0);
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private EditText view;
        private MyTextWatcher(EditText view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            validateButton();
        }

        private void validateButton(){
            if(view.getText().toString().equals("")){
                if(Build.VERSION.SDK_INT < 23)
                    btnSend.setTextColor(getResources().getColor(R.color.colorPrimary));
                else
                    btnSend.setTextColor(getResources().getColor(R.color.colorPrimary, null));
            }
            else
            {
                if(Build.VERSION.SDK_INT < 23)
                    btnSend.setTextColor(getResources().getColor(R.color.white));
                else
                    btnSend.setTextColor(getResources().getColor(R.color.white, null));
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
