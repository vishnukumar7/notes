package com.app.notepad.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.notepad.AppController;
import com.app.notepad.R;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
setContentView(R.layout.activty_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
        openActivity();
            }
        },3000);
    }
    private void openActivity(){
        if(AppController.getInstance().userLogged()){
            startActivity(new Intent(SplashActivity.this,MainActivity.class));
        }else{
            startActivity(new Intent(SplashActivity.this,LoginActivity.class));
        }
        finish();
    }
}
