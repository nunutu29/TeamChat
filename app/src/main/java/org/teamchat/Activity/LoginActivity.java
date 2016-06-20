package org.teamchat.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.teamchat.App.Config;
import org.teamchat.Helper.DbHelper;
import org.teamchat.Helper.SquareImageView;
import org.teamchat.Helper.Utils;
import org.teamchat.R;
import org.teamchat.App.EndPoints;
import org.teamchat.App.MyApplication;
import org.teamchat.Model.User;

public class LoginActivity extends AppCompatActivity {
    private Bitmap bitmap;

    private String TAG = LoginActivity.class.getSimpleName();
    private EditText inputNickname, inputEmail, inputPassword, inputRepeatPassword, inputFullName;
    private Button btnEnter, btnRegister, btnCancel;
    private SquareImageView avatar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Check for login session. It user is already logged in
         * redirect him to main activity
         * */
        if (MyApplication.getInstance().getPrefManager().getUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //inputs
        inputNickname = (EditText) findViewById(R.id.input_name);
        inputPassword = (EditText) findViewById(R.id.input_password);
        inputRepeatPassword = (EditText)findViewById(R.id.input_repeat_password);
        inputFullName = (EditText)findViewById(R.id.input_full_name);
        inputEmail = (EditText) findViewById(R.id.input_email);
        avatar = (SquareImageView)findViewById(R.id.avatar);

        //Listener
        inputRepeatPassword.addTextChangedListener(new MyTextWatcher(inputRepeatPassword));
        inputPassword.addTextChangedListener(new MyTextWatcher(inputPassword));

        btnEnter = (Button) findViewById(R.id.btn_enter);
        btnRegister = (Button) findViewById(R.id.btn_register);
        btnCancel = (Button) findViewById(R.id.btn_cancel);

        if (btnEnter != null)
            btnEnter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   login();
                }
            });

        if(btnRegister != null)
            btnRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO
                    viewRegisterFields();
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            register();
                        }
                    });
                }
            });

        if(btnCancel != null)
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewLoginFields();
                    btnRegister.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            viewRegisterFields();
                        }
                    });
                }
            });

        if(avatar != null)
            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = Utils.OpenGalleryIntent();
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), Config.PICK_IMAGE_REQUEST);
                }
            });

        //Set Visibility
        viewLoginFields();
    }

    public void viewLoginFields(){
        inputRepeatPassword.setVisibility(View.GONE);
        inputEmail.setVisibility(View.GONE);
        inputFullName.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        avatar.setVisibility(View.GONE);
        btnEnter.setVisibility(View.VISIBLE);
    }

    public void viewRegisterFields(){
        inputRepeatPassword.setVisibility(View.VISIBLE);
        inputEmail.setVisibility(View.VISIBLE);
        inputFullName.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
        avatar.setVisibility(View.VISIBLE);
        btnEnter.setVisibility(View.GONE);
    }

    /**
     * logging in user. Will make http post request with name, email
     * as parameters
     */
    private void login() {
        if(!Utils.hasInternet(this))
            return;

        if(!validateField(inputNickname) || !validateField(inputPassword)){
            return;
        }

        final String username = inputNickname.getText().toString().trim();
        final String password = inputPassword.getText().toString();

        StringRequest strReq = new StringRequest(Request.Method.POST, EndPoints.USER_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (!obj.getBoolean("error")) {
                        // user successfully logged in
                        //add user to sqllite if not exists
                        User user = new User();
                        user.setId(obj.getString(DbHelper.IDUTENTE));
                        user.setUsername(obj.getString(DbHelper.USERNAME));
                        user.setPassword(obj.getString(DbHelper.PASSWORD));
                        user.setEmail(obj.getString("email"));
                        user.setName(obj.getString(DbHelper.NOME));

                        //inser in sql lite
                        MyApplication.getInstance().getDbManager().addUser(user);
                        // storing user in shared preferences
                        MyApplication.getInstance().getPrefManager().storeUser(user);

                        // start main activity
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();

                    } else {
                        // login error - simply toast the message
                        Toast.makeText(getApplicationContext(), "" + obj.getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(DbHelper.USERNAME, username);
                params.put(DbHelper.PASSWORD, password);

                Log.e(TAG, "params: " + params.toString());
                return params;
            }
        };

        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }

    private void register(){
        if(!Utils.hasInternet(this))
            return;
        if(!validateField(inputNickname) || !validateField(inputPassword) || !validateField(inputRepeatPassword)
                || !validateEmail() || !validateField(inputFullName)){
            return;
        }
        //Dichiarazione dati
        final String nickname = inputNickname.getText().toString().trim();
        final String password = inputPassword.getText().toString();
        final String email = inputEmail.getText().toString().trim();
        final String fullname = inputFullName.getText().toString().trim();

        final ProgressDialog loading = ProgressDialog.show(this,"Registrazione...","Attendere...",false,false);
        StringRequest strReq = new StringRequest(Request.Method.POST, EndPoints.USER_REGISTER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);
                    if(!obj.getBoolean("error")){
                        User user = new User();
                        user.setId(obj.getString(DbHelper.IDUTENTE));
                        user.setUsername(nickname);
                        user.setPassword(password);
                        user.setName(fullname);
                        user.setEmail(email);

                        Utils.saveToInternalStorage(getApplicationContext(), bitmap,
                                Config.IMG_USER, user.getId().concat(".png"));

                        loading.dismiss();
                        //user.setJoinDate();
                        //adding user to sqlite
                        MyApplication.getInstance().getDbManager().addUser(user);
                        //storing user into prefManger
                        MyApplication.getInstance().getPrefManager().storeUser(user);
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                    else
                    {
                        //Registration failed.
                        Toast.makeText(getApplicationContext(), "" + obj.getString("message"), Toast.LENGTH_LONG).show();
                    }
                }
                catch (JSONException e){
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
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put(DbHelper.USERNAME, nickname);
                params.put(DbHelper.PASSWORD, password);
                params.put(DbHelper.NOME, fullname);
                params.put("email", email);
                if(bitmap != null)
                    params.put("_image_", Utils.getStringImage(bitmap));
                return params;
            }
        };
        MyApplication.getInstance().addToRequestQueue(strReq);
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    // no empty fields
    private boolean validateField(EditText v) {
        if (v.getText().toString().trim().isEmpty()) {
            requestFocus(v);
            v.setError(getString(R.string.err_msg_generic));
            return false;
        }
        return true;
    }

    // Validating email
    private boolean validateEmail() {
        String email = inputEmail.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            requestFocus(inputEmail);
            inputEmail.setError(getString(R.string.err_msg_email));
            return false;
        }
        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK || data == null) return;

        if (requestCode == Config.PICK_IMAGE_REQUEST && data.getData() != null) {
            Uri filePath = data.getData();
            Intent intent = Utils.PerformCropIntent(filePath);
            if(intent != null)
                startActivityForResult(intent, Config.CROP_IMAGE_REQUEST);
            else
            {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                    bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true);
                    avatar.setImageBitmap(bitmap);
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        else if(requestCode == Config.CROP_IMAGE_REQUEST){
            Bundle extras = data.getExtras();
            bitmap = extras.getParcelable("data");
            avatar.setImageBitmap(bitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
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
            validatePassword();
        }

        private boolean validatePassword(){
            if(inputRepeatPassword.getText().toString().equals("") || inputPassword.getText().toString().equals("")){
                return true;
            }
            else
            {
                if(!inputPassword.getText().toString().equals(inputRepeatPassword.getText().toString())){
                    view.setError("Password diverse");
                    return false;
                }
                return true;
            }
        }
    }
}
