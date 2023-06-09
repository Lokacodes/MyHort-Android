package com.lokacodes.kebunpintar;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Http {
    Context context;
    private String url , method = "GET", data = null, response = null;
    private Integer responseCode = 0;
    private Boolean access_token = false;
    private LocalStorage localStorage;

    public Http(Context context, String url) {
        this.context = context;
        this.url = url;
        localStorage = new LocalStorage(context);
    }

    public void setMethod(String method) {
        this.method = method.toUpperCase();
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setAccess_token(Boolean access_token) {
        this.access_token = access_token;
    }

    public String getResponse() {
        return response;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void send(){
        try {
            URL serverUrl = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) serverUrl.openConnection();
            urlConnection.setRequestMethod(method);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            if(access_token){
                urlConnection.setRequestProperty("Authorization", "Bearer " + localStorage.getAccess_token());
            }
            if (!method.equals("GET")) {
                urlConnection.setDoOutput(true);
            }
            if (data != null) {
                OutputStream outputStream = urlConnection.getOutputStream();
                outputStream.write(data.getBytes());
                outputStream.flush();
                outputStream.close();
            }
            responseCode = urlConnection.getResponseCode();

            InputStreamReader inputStreamReader;
            if (responseCode >= 200 && responseCode <= 299) {
                inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
            } else {
                inputStreamReader = new InputStreamReader(urlConnection.getErrorStream());
            }

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            bufferedReader.close();
            response = stringBuffer.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
