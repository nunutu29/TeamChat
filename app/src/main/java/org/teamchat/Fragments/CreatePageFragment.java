package org.teamchat.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.teamchat.Activity.MainActivity;
import org.teamchat.App.Config;
import org.teamchat.App.EndPoints;
import org.teamchat.App.MyApplication;
import org.teamchat.R;
import org.teamchat.Helper.Utils;

import java.util.HashMap;
import java.util.Map;

public class CreatePageFragment extends Fragment {
    private final int ACTION_ADD = 1;
    private final int ACTION_CREATE = 2;
    View rootView;
    private OnFragmentInteractionListener mListener;

    public CreatePageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_create_page, container, false);

        //Setting the toolbar
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.action_create));
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        Button btnCreate = (Button) rootView.findViewById(R.id.btn_create);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreatePage();
            }
        });
        Button btnAdd = (Button) rootView.findViewById(R.id.btn_Add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddPage();
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
        super.onResume();
        ((MainActivity)getActivity()).CURRENT_FRAGMENT = Config.CREATE_GROUP_FRAGMENT;
    }

    @Override
    public void onPause(){
        super.onPause();
        if(Utils.isSoftKeyboardShowing(getActivity()))
            Utils.hideSoftKeyboard(getActivity());
    }

    private void CreatePage(){
        EditText title = (EditText)rootView.findViewById(R.id.txtTitolo);
        EditText descr = (EditText)rootView.findViewById(R.id.txtDesrizione);
        EditText pwd = (EditText) rootView.findViewById(R.id.txtPassword);

        if(ValidateText(title, (TextInputLayout)rootView.findViewById(R.id.input_layout_Text))){
            if(ValidateText(descr, (TextInputLayout)rootView.findViewById(R.id.input_layout_Descr))){
                makeRequest(ACTION_CREATE, title.getText().toString().trim(),
                        pwd.getText().toString(),
                        descr.getText().toString().trim());
            }
        }
    }

    private void AddPage(){
        EditText id = (EditText)rootView.findViewById(R.id.txtID);
        EditText pwd = (EditText)rootView.findViewById(R.id.txtPassword_Add);
        if(ValidateText(id, (TextInputLayout)rootView.findViewById(R.id.input_layout_ID))){
            makeRequest(ACTION_ADD, id.getText().toString().trim(), pwd.getText().toString(), null);
        }
    }

    private void makeRequest(final int ACTION_TYPE, final String p1, final String p2, final String p3) {
        String endPoint = ACTION_TYPE == ACTION_ADD ? EndPoints.PAGE_ADD : EndPoints.PAGE_CREATE;
        StringRequest strReq = new StringRequest(Request.Method.POST, endPoint , new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error
                    if (!obj.getBoolean("error")) {
                        //DbManager manager = new DbManager(getContext());
                        //manager.execInsert(DbHelper.TABLE_PAGINE, manager.setValues(DbHelper.TABLE_PAGINE, obj));
                        //insert default roles


                        //ContentValues values = new ContentValues();
                        //values.put(DbHelper.IDPAGINA, obj.getString(DbHelper.IDPAGINA));
                        //values.put(DbHelper.IDUTENTE, MyApplication.getInstance().getPrefManager().getUser().getId());
                        //values.put(DbHelper.DATA_CREAZIONE, obj.getString(DbHelper.DATA_CREAZIONE));
                        //manager.execInsert(DbHelper.TABLE_PAGINE_UTENTI, values);
                        mListener.StartSync();
                        mListener.PopFragment();
                    }
                    else
                        Toast.makeText(getContext(), "" + obj.getString("message"), Toast.LENGTH_LONG).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                params.put("IDUTENTE", MyApplication.getInstance().getPrefManager().getUser().getId());
                params.put("PASSWORD", p2);
                switch (ACTION_TYPE){
                    case ACTION_ADD:
                        params.put("IDPAGINA", p1);
                        break;
                    case ACTION_CREATE:
                        params.put("TITOLO", p1);
                        params.put("DESCRIZIONE", p3);
                        break;
                }
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

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            InputMethodManager mgr = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private boolean ValidateText(EditText input, TextInputLayout layout){
        if (input.getText().toString().trim().isEmpty()) {
            layout.setErrorEnabled(true);
            layout.setError(getString(R.string.err_msg_generic));
            requestFocus(input);
            return false;
        }
        else
            layout.setErrorEnabled(false);
        return true;
    }

    public interface OnFragmentInteractionListener {
        void PopFragment();
        void StartSync();
        //void AddGroupToList(String id, String title, String descr);
    }
}
