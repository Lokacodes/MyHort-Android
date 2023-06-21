package com.lokacodes.kebunpintar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//            getWindow().setNavigationBarColor(getResources().getColor(R.color.white));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        getUser();
    }

    public void getUser() {
        String url = getString(R.string.api_server)+"getAuthUser";
        new Thread(new Runnable() {
            @Override
            public void run() {
                Http http = new Http(SplashScreen.this, url);
                http.setAccess_token(true);
                http.send();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Integer code = http.getResponseCode();
                        if (code == 200){
                            try {
                                JSONObject response = new JSONObject(http.getResponse());
                                if (response.equals(null)){
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            startActivity(new Intent(SplashScreen.this,LoginActivity.class));
                                            finish();
                                        }
                                    },3000);
                                }
                                else {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            startActivity(new Intent(SplashScreen.this,MainActivity.class));
                                            finish();
                                        }
                                    },3000);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(SplashScreen.this,LoginActivity.class));
                                    finish();
                                }
                            },3000);
                        }
                    }
                });
            }
        }).start();
    }
}