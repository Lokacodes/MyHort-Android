package com.lokacodes.kebunpintar;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class UserActivity extends AppCompatActivity {

    TextView tvEmail, tvName, tvJoined, tvSlogan, tvAddress,tvBirthDate;
    String email, name, joined,slogan, address, birthDate;
    TextView btnLogout;

    ImageView btnBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        tvEmail = findViewById(R.id.tvEmailUser);
        tvName = findViewById(R.id.namaUser);
        tvJoined = findViewById(R.id.tvJoinedSinceUser);
        tvSlogan = findViewById(R.id.SelfDesc);
        tvAddress = findViewById(R.id.tvAddressUser);
        tvBirthDate = findViewById(R.id.tvBirthDateUser);
        btnLogout = findViewById(R.id.tvLogout);
        btnBack = findViewById(R.id.backButton);

        getUser();
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                areYouSure();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void logout() {
        String url = getString(R.string.api_server)+"logout";
        new Thread(new Runnable() {
            @Override
            public void run() {
                Http http = new Http(UserActivity.this, url);
                http.setMethod("POST");
                http.setAccess_token(true);
                http.send();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Integer code = http.getResponseCode();
                        if (code == 200){
                            Toast.makeText(UserActivity.this, "Logout success", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(UserActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(UserActivity.this, "Failed to logout. code : " + code, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    public void getUser() {
        String url = getString(R.string.api_server)+"getAuthUser";
        new Thread(new Runnable() {
            @Override
            public void run() {
                Http http = new Http(UserActivity.this, url);
                http.setAccess_token(true);
                http.send();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Integer code = http.getResponseCode();
                        if (code == 200){
                            try {
                                JSONObject response = new JSONObject(http.getResponse());
                                nulOrNot(response.getString("slogan"),tvSlogan);
                                nulOrNot(response.getString("address"),tvAddress);
                                nulOrNot(response.getString("birth_date"),tvBirthDate);
                                name = response.getString("name");
                                email = response.getString("email");
                                joined = response.getString("created_at");
                                tvName.setText(name);
                                tvEmail.setText(email);
                                tvJoined.setText(joined);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            Toast.makeText(UserActivity.this, "Failed to get user data" + code, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    public void areYouSure() {
        AlertDialog.Builder alert = new AlertDialog.Builder(UserActivity.this);
        alert.setTitle("WARNING");
        alert.setMessage("You are about to log out. Are you sure?");
        alert.setPositiveButton("Yes", (dialog, which) -> {
            logout();
        });
        alert.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        alert.show();
    }

    public void nulOrNot(String text, TextView textView){
        if (text.equals("null")){
            textView.setText("-");
        }
        else {
            textView.setText(text);
        }
    }
    public String getNama(){
        return name;
    }
}