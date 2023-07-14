package com.lokacodes.kebunpintar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lokacodes.kebunpintar.databinding.ActivityGardenMonitorBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class GardenMonitorActivity extends AppCompatActivity {
    ActivityGardenMonitorBinding binding;
    String id_kebun,idAlat;
    String sensor_kepekatan, sensor_ph, sensor_penuh, solenoid_tandon, solenoid_siram;

    Boolean SwitchWateringState, SwitchTandonState;

    Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //bindingblock
        binding = ActivityGardenMonitorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //intentblock : get data from the previous activity
        Intent intent = this.getIntent();
        if (intent != null){
            id_kebun = intent.getStringExtra("id_kebun");
        }

        getKebunData();


        //backbuttonblock
        binding.ivBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GardenMonitorActivity.this, MainActivity.class);
                timer.cancel();
                finish();
                startActivity(intent);
            }
        });

        binding.rightToSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GardenMonitorActivity.this, JadwalActivity.class);
                intent.putExtra("id_alat", idAlat);
                intent.putExtra("id_kebun", id_kebun);
                timer.cancel();
                finish();
                startActivity(intent);
            }
        });


        binding.switchWatering.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (solenoid_siram.equals("0")){
                    updateDataAlat(1,"solenoid_siram");
                }
                else {
                    updateDataAlat(0,"solenoid_siram");
                }

            }
        });

        binding.switchTandon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (solenoid_tandon.equals("0"))
                    updateDataAlat(1,"solenoid_tandon");
                else
                    updateDataAlat(0,"solenoid_tandon");
            }
        });

        binding.namaGarden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GardenMonitorActivity.this, GardenInfoActivity.class);
                intent.putExtra("id_alat", idAlat);
                intent.putExtra("id_kebun", id_kebun);
                timer.cancel();
                finish();
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        timer.cancel();
        Intent intent = new Intent(GardenMonitorActivity.this, MainActivity.class);
        finish();
        startActivity(intent);
    }
    private void getKebunData() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getDataAlat();
            }
        }, 0, 3000);

        String url = getString(R.string.api_server)+"kebuns/"+id_kebun;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Http http = new Http(GardenMonitorActivity.this, url);
                http.setAccess_token(true);
                http.send();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Integer code = http.getResponseCode();
                        if (code == 200){
                            try {
                                JSONObject response = new JSONObject(http.getResponse());
                                String arrResponse = response.getString("kebun");
                                JSONObject kebun = new JSONObject(arrResponse); //hanya (dan wajib) berisi satu index sajaaaaa
                                String namaKebun = kebun.getString("nama_kebun");
                                String lokasiKebun = kebun.getString("lokasi_kebun");

                                binding.namaGarden.setText(namaKebun);
                                binding.tvLocationGarden.setText(lokasiKebun);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            Toast.makeText(GardenMonitorActivity.this, "Failed to get user data " + code, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    //methodblock : to get dataAlat from api
    private void getDataAlat() {
        String url = getString(R.string.api_server)+"alats?id_kebun="+id_kebun;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Http http = new Http(GardenMonitorActivity.this, url);
                http.setAccess_token(true);
                http.send();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Integer code = http.getResponseCode();
                        if (code == 200){
                            try {
                                JSONObject response = new JSONObject(http.getResponse());
                                JSONArray jsonArray = response.getJSONArray("alats");
                                JSONObject jsonObject = jsonArray.getJSONObject(0);//hanya (dan wajib) berisi satu index sajaaaaa
//                                Log.v("jsonObject", jsonObject.toString());
                                sensor_kepekatan = jsonObject.getString("sensor_kepekatan");
                                sensor_ph = jsonObject.getString("sensor_ph");
                                sensor_penuh = jsonObject.getString("sensor_penuh");
                                solenoid_tandon = jsonObject.getString("solenoid_tandon");
                                solenoid_siram = jsonObject.getString("solenoid_siram");
                                idAlat = jsonObject.getString("id");

                                fullOrNot(sensor_penuh,binding.WaterTankValue);

                                onOrOff(solenoid_siram,binding.WateringValue);
                                onOrOff(solenoid_tandon,binding.FillTankValue);
                                binding.fertilizerValue.setText(sensor_kepekatan);

                                binding.switchWatering.setChecked(solenoid_siram.equals("1"));
                                binding.switchTandon.setChecked(solenoid_tandon.equals("1"));


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else if (code == 404) {
                            Toast.makeText(GardenMonitorActivity.this, "Failed to get garden data " + code, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GardenMonitorActivity.this, "Failed to get garden data " + code, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        }).start();
    }

    //methodblock : to change value of data in database (solenoidWatering and solenoidTandon)
    private void updateDataAlat(Integer value, String field) {
        JSONObject params = new JSONObject();
        try {
            params.put(field, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String data = params.toString();
        String url = getString(R.string.api_server) + "alats/" + idAlat;

        new Thread(new Runnable() {
            @Override
            public void run() {
                Http http = new Http(GardenMonitorActivity.this, url);
                http.setMethod("PATCH");
                http.setAccess_token(true);
                http.setData(data);
                http.send();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Integer code = http.getResponseCode();
                        if(code == 200||code == 201){
                            getDataAlat();
                        }
                        else if (code == 400|| code == 401 || code == 422 || code == 405){
                            try {
                                JSONObject response = new JSONObject(http.getResponse());
                                String msg = response.getString("message");
                                Toast.makeText(GardenMonitorActivity.this, msg, Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            Toast.makeText(GardenMonitorActivity.this, "Something went wrong! code : " + code, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    //methodblock : to change textview color and text based on the value (that is supposed to be on / off)
    private void onOrOff(String var, TextView TextView) {
        if (var.equals("1")){
            TextView.setText("ON");
            TextView.setTextColor(getResources().getColor(R.color.hijaubagus));
        }
        else {
            TextView.setText("OFF");
            TextView.setTextColor(getResources().getColor(R.color.abangnjreng));
        }
    }

    //methodblock : to change textview color and text based on the value (that is supposed to be full / not full)
    private void fullOrNot(String var, TextView TextView) {
        if (var.equals("1")){
            TextView.setText("Full");
            TextView.setTextColor(getResources().getColor(R.color.hijaubagus));
        }
        else {
            TextView.setText("Not\nFull");
            TextView.setTextColor(getResources().getColor(R.color.abangnjreng));
        }
    }
}