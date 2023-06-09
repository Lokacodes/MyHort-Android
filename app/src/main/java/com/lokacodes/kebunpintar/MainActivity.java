package com.lokacodes.kebunpintar;


import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lokacodes.kebunpintar.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    String greeting, name, nName[],id_user,nama_kebunInput,lokasi_kebunInput;
    String[] lokasi_kebun = {}, nama_kebun = {}, id_kebun = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        getUser();
        getKebun();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        TextView tnameOfUser = binding.tvHello;
        tnameOfUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });
        
        binding.addGardenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addGardenPop();
            }
        });
    }

    public void getUser() {
        String url = getString(R.string.api_server)+"getAuthUser";
        new Thread(new Runnable() {
            @Override
            public void run() {
                Http http = new Http(MainActivity.this, url);
                http.setAccess_token(true);
                http.send();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Integer code = http.getResponseCode();
                        if (code == 200){
                            try {
                                JSONObject response = new JSONObject(http.getResponse());
                                name = response.getString("name");
                                id_user = response.getString("id");
                                nName =  name.split(" ", 2);
                                name = nName[0];

                                greeting = "Hello, " + name + " !";
                                binding.tvHello.setText(greeting);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Failed to get user data" + code, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void getKebun() {
        String url = getString(R.string.api_server)+"kebuns";
        new Thread(new Runnable() {
            @Override
            public void run() {
                Http http = new Http(MainActivity.this, url);
                http.setAccess_token(true);
                http.send();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Integer code = http.getResponseCode();
                        if (code == 200){
                            try {
                                JSONObject response = new JSONObject(http.getResponse());
                                JSONArray jsonArray = response.getJSONArray("kebun");

                                if (jsonArray.toString().equals("[]")){
                                    Toast.makeText(MainActivity.this, "Data Kosong", Toast.LENGTH_SHORT).show();
                                }

                                ArrayList<Kebun> kebunArrayList = new ArrayList<>();

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    AddToArray addToArray = new AddToArray(lokasi_kebun,jsonObject.getString("lokasi_kebun"));
                                    lokasi_kebun = addToArray.getArrayNew();

                                    addToArray = new AddToArray(nama_kebun,jsonObject.getString("nama_kebun"));
                                    nama_kebun = addToArray.getArrayNew();

                                    ;
                                    id_user = jsonObject.getString("id_user");

                                    addToArray = new AddToArray(id_kebun,jsonObject.getString("id"));
                                    id_kebun = addToArray.getArrayNew();
                                    Kebun kebun = new Kebun(nama_kebun[i], lokasi_kebun[i], id_user, id_kebun[i]);
                                    kebunArrayList.add(kebun);

                                }

                                kebunListAdapter kebunlistadapter = new kebunListAdapter(MainActivity.this,kebunArrayList);

                                binding.listview.setAdapter(kebunlistadapter);
                                binding.listview.setClickable(true);
                                binding.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Intent intent = new Intent(MainActivity.this,GardenMonitorActivity.class);
                                        intent.putExtra("nama_kebun",nama_kebun[position]);
                                        intent.putExtra("lokasi_kebun",lokasi_kebun[position]);
                                        intent.putExtra("id_user",id_user);
                                        intent.putExtra("id_kebun",id_kebun[position]);
                                        startActivity(intent);
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Failed to get user data" + code, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();


    }

    private void addGardenPop() {
        Dialog dialog = new Dialog(this, R.style.DialogStyle);
        dialog.setContentView(R.layout.add_garden_popup);

        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_pop_up));

        EditText namaKebun = dialog.findViewById(R.id.etGardenName);
        EditText lokasiKebun = dialog.findViewById(R.id.etGardenLocation);

        Button addGardenButton = dialog.findViewById(R.id.btnAddGarden);
        addGardenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nama_kebunInput = namaKebun.getText().toString();
                lokasi_kebunInput = lokasiKebun.getText().toString();
                checkKebun(nama_kebunInput, nama_kebunInput);
            }
        });
        dialog.show();
    }

    private void checkKebun(String namaKebun, String lokasiKebun) {
        if(namaKebun.isEmpty() || lokasiKebun.isEmpty()){
            alertFail("Please fill out the fields!");
        }
        else {
            alertConf("Are you sure you want to add this garden?");
        }

    }

    private void sendAddGarden() {
        JSONObject params = new JSONObject();
        try {
            params.put("nama_kebun", nama_kebunInput);
            params.put("lokasi_kebun", lokasi_kebunInput);
            params.put("id_user", id_user);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String data = params.toString();
        String url = getString(R.string.api_server)+"kebuns";

        new Thread(new Runnable() {
            @Override
            public void run() {
                Http http = new Http(MainActivity.this, url);
                http.setMethod("POST");
                http.setAccess_token(true);
                http.setData(data);
                http.send();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Integer responseCode = http.getResponseCode();

                        if (responseCode == 200) {
                            try {
                                JSONObject response = new JSONObject(http.getResponse());
                                String arrResponse = response.getString("kebun");
                                JSONObject kebun = new JSONObject(arrResponse);
                                String id_kebun_baru = kebun.getString("id");
                                sendAddAlat(id_kebun_baru);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            alertSuccess("Add Garden Success!");
                        } else if (responseCode == 422) {
                            try {
                                JSONObject response = new JSONObject(http.getResponse());
                                String message = response.getString("message");
                                alertFail(message);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else if (responseCode == 401) {
                            try {
                                JSONObject response = new JSONObject(http.getResponse());
                                String message = response.getString("message");
                                alertFail(message);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "Something went wrong! code : " + responseCode, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void sendAddAlat(String id_kebun) {

        JSONObject params = new JSONObject();
        try {
            params.put("id_kebun", id_kebun);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String data = params.toString();
        String url = getString(R.string.api_server)+"alats";

        new Thread(new Runnable() {
            @Override
            public void run() {
                Http http = new Http(MainActivity.this, url);
                http.setMethod("POST");
                http.setAccess_token(true);
                http.setData(data);
                http.send();

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Integer responseCode = http.getResponseCode();
//                        if (responseCode == 200) {
//
//                        }
//                        else if (responseCode == 422) {
//                            try {
//                                JSONObject response = new JSONObject(http.getResponse());
//                                String message = response.getString("message");
//                                Toast toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT);
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//
//                        }
//                        else if (responseCode == 401) {
//                            try {
//                                JSONObject response = new JSONObject(http.getResponse());
//                                String message = response.getString("message");
//                                Toast toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT);
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//
//                        }
//                        else {
//                            Toast.makeText(MainActivity.this, "Something went wrong! code : " + responseCode, Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
            }
        }).start();
    }

    private void alertFail(String s) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Add Garden Failed")
                .setMessage(s)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void alertConf(String s) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Confirmation")
                .setMessage(s)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                sendAddGarden();
                            }
                        }
                )
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void alertSuccess(String s) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Success")
                .setMessage(s)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                startActivity(getIntent());
                            }
                        }
                )
                .show();
    }
}