package com.lokacodes.kebunpintar;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail,etPassword;
    Button btLogin;
    TextView btRegister;
    String email, password;
    LocalStorage localStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//            getWindow().setNavigationBarColor(getResources().getColor(R.color.white));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        localStorage = new LocalStorage(LoginActivity.this);
        etEmail = findViewById(R.id.emailorusername);
        etPassword = findViewById(R.id.password);
        btLogin = findViewById(R.id.loginButton);
        btRegister = findViewById(R.id.CreateAccount);

        btLogin.setOnClickListener(v -> checkLogin());

        btRegister.setOnClickListener(v -> {
            Intent myIntent = new Intent(LoginActivity.this, RegisterActivity.class);
            finish();
            startActivity(myIntent);
        });


    }

    private void checkLogin() {
        email = etEmail.getText().toString();
        password = etPassword.getText().toString();
        if(email.isEmpty() || password.isEmpty()){
            alertFail("Please Insert your Email and Password!");
        }
        else {
            sendLogin();
        }
    }

    private void sendLogin() {
        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
            params.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String data = params.toString();
        String url = getString(R.string.api_server)+"login";

        new Thread(new Runnable() {
            @Override
            public void run() {
                Http http = new Http(LoginActivity.this, url);
                http.setMethod("POST");
                http.setData(data);
                http.send();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Integer responseCode = http.getResponseCode();
                        if (responseCode == 200) {
                            try {
                                JSONObject response = new JSONObject(http.getResponse());
                                String access_token = response.getString("access_token");
                                localStorage.setAccess_token(access_token);
                                Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                                finish();
                                startActivity(myIntent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else if (responseCode == 422) {
                            try {
                                JSONObject response = new JSONObject(http.getResponse());
                                String message = response.getString("message");
                                alertFail(message);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                        else if (responseCode == 401) {
                            try {
                                JSONObject response = new JSONObject(http.getResponse());
                                String message = response.getString("message");
                                alertFail(message);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                        else {
                            Toast.makeText(LoginActivity.this, "Something went wrong! code : " + responseCode, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }


    private void alertFail(String s){
        AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
        alert.setTitle("Login Failed");
        alert.setMessage(s);
        alert.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
//            etEmail.setText("");
//            etPassword.setText("");
        });
        alert.show();
    }
}