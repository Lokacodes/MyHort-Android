package com.lokacodes.kebunpintar;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    String sensor_kepekatan, sensor_suhu, sensor_penuh, solenoid_tandon, solenoid_siram, sensor_kelembapan, tinggi_tandon;

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
            idAlat = intent.getStringExtra("id_alat");
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

        binding.btTankHeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPopUp();
            }
        });
        binding.switchWatering.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (solenoid_siram.equals("mati")){
                    updateDataAlat("hidup","solenoid_siram");
                }
                else {
                    updateDataAlat("mati","solenoid_siram");
                }

            }
        });

        binding.ivBackgroundWatering.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (solenoid_siram.equals("mati")){
                    updateDataAlat("hidup","solenoid_siram");
                }
                else {
                    updateDataAlat("mati","solenoid_siram");
                }

            }
        });

        binding.switchTandon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (solenoid_tandon.equals("mati"))
                    updateDataAlat("hidup","solenoid_tandon");
                else
                    updateDataAlat("mati","solenoid_tandon");
            }
        });

        binding.ivBackgroundFillTank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (solenoid_tandon.equals("mati"))
                    updateDataAlat("hidup","solenoid_tandon");
                else
                    updateDataAlat("mati","solenoid_tandon");
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
        String url = getString(R.string.api_server)+"alats?id_alat="+idAlat;
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
                                sensor_kepekatan = jsonObject.getString("sensor_kepekatan");
                                sensor_penuh = jsonObject.getString("sensor_penuh");
                                solenoid_tandon = jsonObject.getString("solenoid_tandon");
                                solenoid_siram = jsonObject.getString("solenoid_siram");
                                sensor_suhu = jsonObject.getString("sensor_suhu");
                                sensor_kelembapan = jsonObject.getString("sensor_kelembapan");
                                tinggi_tandon = jsonObject.getString("tinggi_tandon");


//                                fullOrNot(sensor_penuh,binding.WaterTankValue);
                                binding.WaterTankValue.setText(sensor_penuh + " %" );

                                onOrOff(solenoid_siram,binding.WateringValue);
                                onOrOff(solenoid_tandon,binding.FillTankValue);
                                binding.fertilizerValue.setText(sensor_kepekatan);
                                binding.tempValue.setText(sensor_suhu + " °C");
                                binding.humValue.setText(sensor_kelembapan + " %");

                                binding.switchWatering.setChecked(solenoid_siram.equals("hidup"));
                                binding.switchTandon.setChecked(solenoid_tandon.equals("hidup"));

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
    private void updateDataAlat(String value, String field) {
        JSONObject params = new JSONObject();
        try {
            params.put(field, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String data = params.toString();
        String url = getString(R.string.api_server) + "updateAlat?id_alat=" + idAlat;

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
        Log.d("onOrOff: ", var);
        if (var.equals("hidup")){
            TextView.setText("ON");
            TextView.setTextColor(getResources().getColor(R.color.hijaubagus));
        }
        else if (var.equals("mati")){
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

    private void editPopUp() {
        Dialog dialog = new Dialog(this, R.style.DialogStyle);
        dialog.setContentView(R.layout.edit_tinggi_tandon);

        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_pop_up));

        EditText ETtinggi_tandon = dialog.findViewById(R.id.etGardenName);

        ETtinggi_tandon.setText(tinggi_tandon);

        Button saveHeightButton = dialog.findViewById(R.id.btnSaveEditGarden);

        ImageView close = dialog.findViewById(R.id.closeButton);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        saveHeightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            String tinggi_tandon = ETtinggi_tandon.getText().toString();
                if (tinggi_tandon.isEmpty()){
                    ETtinggi_tandon.setError("Tinggi tandon tidak boleh kosong");
                    ETtinggi_tandon.requestFocus();
                }
                else {
                    updateDataAlat(tinggi_tandon,"tinggi_tandon");
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }
}