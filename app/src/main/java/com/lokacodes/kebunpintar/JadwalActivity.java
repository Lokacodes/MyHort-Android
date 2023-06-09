package com.lokacodes.kebunpintar;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.lokacodes.kebunpintar.databinding.ActivityJadwalBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

public class JadwalActivity extends AppCompatActivity {
    ActivityJadwalBinding binding;
    String[] jam_on = {}, jam_off = {};
    String id_alat, timeStartS, timeEndS;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJadwalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = this.getIntent();
        if (intent != null){
            id_alat = intent.getStringExtra("id_alat");
        }

        binding.backButton.setOnClickListener(v -> onBackPressed());

        binding.addJadwalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSchedulePop();
            }
        });

        getjadwal();
    }

    private void addSchedulePop() {
        Dialog dialog = new Dialog(this, R.style.DialogStyle2);
        dialog.setContentView(R.layout.add_schedule_popup);

        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_pop_up));

        ImageView timeStartSelect = dialog.findViewById(R.id.TimeStartSelect);
        ImageView timeEndSelect = dialog.findViewById(R.id.TimeEndSelect);

        TextView timeStart = dialog.findViewById(R.id.timeStartString);
        TextView timeEnd = dialog.findViewById(R.id.timeEndString);

        Button addScheduleButton = dialog.findViewById(R.id.buttonAdd);

        timeStartSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                 on below line we are getting the
                // instance of our calendar.
                final Calendar c = Calendar.getInstance();

                // on below line we are getting our hour, minute.
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                // on below line we are initializing our Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(JadwalActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                // on below line we are setting selected time
                                // in our text view.
                                String waktu = hourOfDay + ":" + minute;
                                timeStart.setText(waktu);

                                timeStartS = waktu;
                            }
                        }, hour, minute, true);
                // at last we are calling show to
                // display our time picker dialog.
                timePickerDialog.show();
            }
        });

        timeEndSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                 on below line we are getting the
                // instance of our calendar.
                final Calendar c = Calendar.getInstance();

                // on below line we are getting our hour, minute.
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                // on below line we are initializing our Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(JadwalActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                // on below line we are setting selected time
                                // in our text view.
                                String waktu = hourOfDay + ":" + minute;
                                timeEnd.setText(waktu);

                                timeEndS = waktu;
                            }
                        }, hour, minute, true);
                // at last we are calling show to
                // display our time picker dialog.
                timePickerDialog.show();
            }
        });

        addScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String timeStartS = timeStart.getText().toString();
                String timeEndS = timeEnd.getText().toString();
                checkJam(timeStartS, timeEndS);
            }
        });
        dialog.show();
    }

    private void checkJam(String timeStart, String timeEnd) {
        if(timeStart.isEmpty() || timeEnd.isEmpty()){
            alertFail("Please select start and end time");
        }
        else {
            alertConf("Are you sure you want to add this jadwal?");
        }

    }

    private void alertFail(String s) {
        new AlertDialog.Builder(JadwalActivity.this)
                .setTitle("Add Schedule Failed")
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
        new AlertDialog.Builder(JadwalActivity.this)
                .setTitle("Confirmation")
                .setMessage(s)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                storeSchedule();
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

    private void storeSchedule() {
        JSONObject params = new JSONObject();
        try {
            params.put("id_alat", id_alat);
            params.put("jam_off", timeEndS);
            params.put("jam_on", timeStartS);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String data = params.toString();
        Log.v("data", data);
        String url = getString(R.string.api_server)+"jadwals";

        new Thread(new Runnable() {
            @Override
            public void run() {
                Http http = new Http(JadwalActivity.this, url);
                http.setMethod("POST");
                http.setAccess_token(true);
                http.setData(data);
                http.send();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Integer responseCode = http.getResponseCode();
                        if (responseCode == 200) {
                            alertSuccess("Add Garden Success!");
                            startActivity(getIntent());
                            finish();
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

                        } else if (responseCode == 500) {
                            try {
                                JSONObject response = new JSONObject(http.getResponse());
                                String message = response.getString("message");
                                alertFail(message);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(JadwalActivity.this, "Something went wrong! code : " + responseCode, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        }).start();
    }

    private void alertSuccess(String s) {

    }

    private void getjadwal() {
        String url = getString(R.string.api_server)+"jadwals?id_alat=" + id_alat;
        new Thread(() -> {
            Http http = new Http(JadwalActivity.this, url);
            http.setAccess_token(true);
            http.send();

            runOnUiThread(() -> {
                Integer code = http.getResponseCode();
                if (code == 200){
                    try {
                        JSONObject response = new JSONObject(http.getResponse());
                        JSONArray jsonArray = response.getJSONArray("Jadwals");

                        ArrayList<Jadwal> jadwalArrayList = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            String jam_onRef = hourNMinOnly(jsonObject.getString("jam_on"));
                            String jam_offRef = hourNMinOnly(jsonObject.getString("jam_off"));

                            AddToArray addToArray = new AddToArray(jam_on,jam_onRef);
                            jam_on = addToArray.getArrayNew();

                            addToArray = new AddToArray(jam_off,jam_offRef);
                            jam_off = addToArray.getArrayNew();

                            id_alat = jsonObject.getString("id_alat");

                            Jadwal jadwal = new Jadwal(jam_on[i], jam_off[i], id_alat);
                            jadwalArrayList.add(jadwal);
                        }

                        jadwalListAdapter jadwalListAdapter = new jadwalListAdapter(JadwalActivity.this,jadwalArrayList);

                        binding.listViewSchedule.setAdapter(jadwalListAdapter);
                        binding.listViewSchedule.setClickable(true);
                        binding.listViewSchedule.setOnItemClickListener((parent, view, position, id) -> {
                            //pilih jam ulang
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    Toast.makeText(JadwalActivity.this, "Failed to get user data" + code, Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    public String hourNMinOnly(String str) {
        if (str != null && str.length() > 0) {
            str = str.substring(0, str.length() - 3);
        }
        return str;
    }
}