package com.lokacodes.kebunpintar;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalStorage {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Context context;
    String access_token;


    public LocalStorage(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("local_storage", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public String getAccess_token() {
        access_token = sharedPreferences.getString("access_token", "");
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
        editor.putString("access_token", access_token);
        editor.commit();
    }
}
