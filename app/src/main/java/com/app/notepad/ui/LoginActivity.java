package com.app.notepad.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.notepad.AppController;
import com.app.notepad.NetworkReceiver;
import com.app.notepad.R;
import com.app.notepad.databinding.ActivityLoginBinding;
import com.app.notepad.model.DataNoteResponse;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {
    public static int APP_REQUEST_CODE = 100;
    ActivityLoginBinding binding;
    private String TAG = "LoginActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);


        binding.mobileLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPhoneLogin("+919876543210");
            }
        });
    }


    private void verifyMobileNumber() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build());
        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.OTPTheme)
                        .build(),
                APP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                String countrynum = user.getPhoneNumber();

                Log.i(TAG, "onActivityResultwelco: " + countrynum);
                getPhoneLogin(countrynum);
            }
        }
    }

    private void getPhoneLogin(String mobileNo) {
        if (NetworkReceiver.isConnected()) {
            showLoading();
            String URL=AppController.GET_NOTE+"?mobileno="+mobileNo;
            StringRequest request=new StringRequest(Request.Method.GET, URL, new com.android.volley.Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "onResponse: "+response);
                    DataNoteResponse noteResponse=new Gson().fromJson(response,DataNoteResponse.class);
                    if (noteResponse.getStatusCode().equals("200")){
                        appController.setValues("logged",true);
                        appController.setValues("user_mobile",mobileNo);
                        dismissLoading();
                        startActivity(new Intent(LoginActivity.this,MainActivity.class));
                        finish();
                    }
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    dismissLoading();
                    Toast.makeText(LoginActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            appController.addToRequestQueue(request);
        }else
            Toast.makeText(LoginActivity.this, getString(R.string.internet_error), Toast.LENGTH_SHORT).show();
    }
}
