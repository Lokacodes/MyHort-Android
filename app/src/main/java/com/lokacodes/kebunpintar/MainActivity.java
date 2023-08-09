package com.lokacodes.kebunpintar;


import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lokacodes.kebunpintar.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    String greeting, name, nName[],id_user,nama_kebunInput,lokasi_kebunInput,id_alatInput;
    String[] lokasi_kebun = {}, nama_kebun = {}, id_kebun = {} , id_alat = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        getUser();
        getKebun();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        TextView tnameOfUser = binding.tvHello;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getWindow().setStatusBarColor(getResources().getColor(R.color.white));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        tnameOfUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                finish();
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

//    method to get (only) user name
//    even it's actually returning the whole user data
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
                            startActivity(getIntent());
                        }
                    }
                });
            }
        }).start();
    }

    // method to get kebun data and set it to listview
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
                                Boolean isClickable = true; // a var to help set the listview clickable to true

                                JSONObject response = new JSONObject(http.getResponse()); // get the response from server
                                JSONArray jsonArray = response.getJSONArray("kebun"); // get the "kebun" array from response

                                ArrayList<Kebun> kebunArrayList = new ArrayList<>();
                                if (jsonArray.toString().equals("[]")){
                                    Kebun kebun = new Kebun("Belum Ada Kebun !", "", "", "");
                                    kebunArrayList.add(kebun);
                                    isClickable = false;
                                    binding.listview.setClickable(isClickable);
                                }

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                                    id_user = jsonObject.getString("id_user");

                                    AddToArray addToArrayLokasiKebun = new AddToArray(lokasi_kebun,jsonObject.getString("lokasi_kebun"));
                                    lokasi_kebun = addToArrayLokasiKebun.getArrayNew();

                                    AddToArray addToArrayNamaKebun = new AddToArray(nama_kebun,jsonObject.getString("nama_kebun"));
                                    nama_kebun = addToArrayNamaKebun.getArrayNew();

                                    AddToArray addToArrayIDKebun = new AddToArray(id_kebun,jsonObject.getString("id"));
                                    id_kebun = addToArrayIDKebun.getArrayNew();

                                    AddToArray addToArrayIDAlat = new AddToArray(id_alat,jsonObject.getString("id_alat"));
                                    id_alat = addToArrayIDAlat.getArrayNew();

                                    Log.d("id_alat", id_alat[i]);

                                    Kebun kebun = new Kebun(nama_kebun[i], lokasi_kebun[i], id_user, id_alat[i]);
                                    kebunArrayList.add(kebun);
                                }

                                kebunListAdapter kebunlistadapter = new kebunListAdapter(MainActivity.this,kebunArrayList);

                                binding.listview.setAdapter(kebunlistadapter);

                                if (isClickable.equals(true)){
                                    binding.listview.setClickable(true);
                                binding.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Intent intent = new Intent(MainActivity.this,GardenMonitorActivity.class);
                                        intent.putExtra("id_kebun",id_kebun[position]);
                                        intent.putExtra("id_alat",id_alat[position]);

                                        Log.d("id_alat intent", id_alat[position]);
                                        Log.d("id_kebun intent", id_kebun[position]);

                                        finish();
                                        startActivity(intent);
                                    }
                                });
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Failed to get Gardens " + code, Toast.LENGTH_SHORT).show();
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
        EditText idAlat = dialog.findViewById(R.id.etDeviceID);

        Button addGardenButton = dialog.findViewById(R.id.btnSaveEditGarden);

        ImageView close = dialog.findViewById(R.id.closeButton);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        addGardenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nama_kebunInput = namaKebun.getText().toString();
                lokasi_kebunInput = lokasiKebun.getText().toString();
                id_alatInput = idAlat.getText().toString();
                Log.v("nama_kebun", nama_kebunInput);
                Log.v("lokasi_kebun", lokasi_kebunInput);
                Log.v("id_alat", id_alatInput);
                checkKebun(nama_kebunInput, lokasi_kebunInput, id_alatInput);
            }
        });
        dialog.show();
    }

    private void checkKebun(String namaKebun, String lokasiKebun, String id_alat) {
        if(namaKebun.isBlank() || lokasiKebun.isBlank() || id_alat.isBlank()){
            alertFail("Please fill out the fields!");
        }
        else {
            checkAlat(id_alat);
        }
    }

    //method to check if an alat is already registered
    private void checkAlat(String id_alat) {
        String url = getString(R.string.api_server)+"cekIDAlat?id_alat="+id_alat;

        new Thread(new Runnable() {
            @Override
            public void run() {
                Http http = new Http(MainActivity.this, url);
                http.setMethod("GET");
                http.setAccess_token(true);
                http.send();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Integer responseCode = http.getResponseCode();

                        if (responseCode == 200) {
                            alertConf("Are you sure you want to add this garden?");
                        } else if (responseCode == 403) {
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

    private void sendAddGarden() {
        JSONObject params = new JSONObject();
        try {
            params.put("nama_kebun", nama_kebunInput);
            params.put("lokasi_kebun", lokasi_kebunInput);
            params.put("id_user", id_user);
            params.put("id_alat", id_alatInput);
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
                        String arrResponse = "";
                        if (responseCode == 200) {
                            try {
                                JSONObject response = new JSONObject(http.getResponse());
                                arrResponse = response.getString("message");
//                                Log.d("response", arrResponse);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            alertSuccess(arrResponse);
                            Log.d("response", arrResponse);
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