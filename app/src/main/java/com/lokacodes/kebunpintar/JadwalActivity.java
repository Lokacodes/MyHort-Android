package com.lokacodes.kebunpintar;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lokacodes.kebunpintar.databinding.ActivityJadwalBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class JadwalActivity extends AppCompatActivity {
    ActivityJadwalBinding binding;
    String[] jam_on = {}, jam_off = {}, id_jadwal = {};
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

        binding.addJadwalButton.setOnClickListener(v -> addSchedulePop());

        getjadwal();
    }

    //used first time when the activity is started to get the schedule data collection
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
                        Boolean isClickable = true;
                        ArrayList<Jadwal> jadwalArrayList = new ArrayList<>();
                        if (jsonArray.length() == 0){
                            Jadwal jadwal = new Jadwal("Jadwal", "Kosong", "");
                            jadwalArrayList.add(jadwal);
                            isClickable = false;
                            binding.listViewSchedule.setClickable(isClickable);
                        }

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            String jam_onRef = hourNMinOnly(jsonObject.getString("jam_on"));
                            String jam_offRef = hourNMinOnly(jsonObject.getString("jam_off"));
                            String id_jadwalRef = jsonObject.getString("id");

                            AddToArray addToArray = new AddToArray(jam_on,jam_onRef);
                            jam_on = addToArray.getArrayNew();

                            addToArray = new AddToArray(jam_off,jam_offRef);
                            jam_off = addToArray.getArrayNew();

                            addToArray = new AddToArray(id_jadwal,id_jadwalRef);
                            id_jadwal = addToArray.getArrayNew();

                            id_alat = jsonObject.getString("id_alat");

                            Jadwal jadwal = new Jadwal(jam_on[i], jam_off[i], id_alat);
                            jadwalArrayList.add(jadwal);
                        }

                        jadwalListAdapter jadwalListAdapter = new jadwalListAdapter(JadwalActivity.this,jadwalArrayList);

                        binding.listViewSchedule.setAdapter(jadwalListAdapter);

                        if (isClickable.equals(true)){
                            binding.listViewSchedule.setClickable(true);
                            binding.listViewSchedule.setOnItemClickListener((parent, view, position, id) -> {
                                String off = jam_off[position];
                                String on = jam_on[position];
                                String id_jadwal_update = id_jadwal[position];

                                updateSchedulePop(on,off,id_jadwal_update);
                            });
                        }

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

    //used to show popup to add a schedule
    private void addSchedulePop() {
        Dialog dialog = new Dialog(this, R.style.DialogStyle);
        dialog.setContentView(R.layout.add_schedule_popup);

        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_pop_up));

        ImageView timeStartSelect = dialog.findViewById(R.id.TimeStartSelect);
        ImageView timeEndSelect = dialog.findViewById(R.id.TimeEndSelect);

        TextView timeStart = dialog.findViewById(R.id.timeStartString);
        TextView timeEnd = dialog.findViewById(R.id.timeEndString);

        Button addScheduleButton = dialog.findViewById(R.id.buttonUpdate);
        Button deleteButton = dialog.findViewById(R.id.buttonDelete);

        ImageView close = dialog.findViewById(R.id.closeButton);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        timeStartSelect.setOnClickListener(v -> {
//                 on below line we are getting the
            // instance of our calendar.
            final Calendar c = Calendar.getInstance();

            // on below line we are getting our hour, minute.
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // on below line we are initializing our Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(JadwalActivity.this,
                    (view, hourOfDay, minute1) -> {
                        // on below line we are setting selected time
                        // in our text view.
                        String waktu = hourOfDay + ":" + minute1;
                        timeStart.setText(waktu);

                        timeStartS = waktu;
                    }, hour, minute, true);
            // at last we are calling show to
            // display our time picker dialog.
            timePickerDialog.show();
        });

        timeEndSelect.setOnClickListener(v -> {
//                 on below line we are getting the
            // instance of our calendar.
            final Calendar c = Calendar.getInstance();

            // on below line we are getting our hour, minute.
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // on below line we are initializing our Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(JadwalActivity.this,
                    (view, hourOfDay, minute12) -> {
                        // on below line we are setting selected time
                        // in our text view.
                        String waktu = hourOfDay + ":" + minute12;
                        timeEnd.setText(waktu);

                        timeEndS = waktu;
                    }, hour, minute, true);
            // at last we are calling show to
            // display our time picker dialog.
            timePickerDialog.show();
        });

        addScheduleButton.setOnClickListener(v -> {
            String timeStartS = timeStart.getText().toString();
            String timeEndS = timeEnd.getText().toString();
            checkJam(timeStartS, timeEndS);
        });


        dialog.show();
    }

    //used to check the input from addSchedulePop (used only to add a schedule)
    private void checkJam(String timeStart, String timeEnd) {
        if(timeStart.isEmpty() || timeEnd.isEmpty()){
            alertFail("Please select start and end time");
        }
        else {
            alertConfAdd("Are you sure you want to add this jadwal?");
        }

    }

    //used to store the schedule after it's checked and the user confirms
    //it has http request to store the schedule
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

        new Thread(() -> {
            Http http = new Http(JadwalActivity.this, url);
            http.setMethod("POST");
            http.setAccess_token(true);
            http.setData(data);
            http.send();

            runOnUiThread(() -> {
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

            });
        }).start();
    }


    //UPDATE BLOCKS

    //used to show popup to update the clicked schedule
    private void updateSchedulePop(String on, String off, String id) {
        Dialog dialog = new Dialog(this, R.style.DialogStyle);
        dialog.setContentView(R.layout.edit_schedule_popup);

        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_pop_up));

        ImageView timeStartSelect = dialog.findViewById(R.id.TimeStartSelect);
        ImageView timeEndSelect = dialog.findViewById(R.id.TimeEndSelect);

        TextView timeStart = dialog.findViewById(R.id.timeStartString);
        TextView timeEnd = dialog.findViewById(R.id.timeEndString);

        timeStart.setText(on);
        timeEnd.setText(off);

        Button UpdateScheduleButton = dialog.findViewById(R.id.buttonUpdate);
        View DeleteScheduleButton = dialog.findViewById(R.id.buttonDelete);

        ImageView close = dialog.findViewById(R.id.closeButton);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        timeStartSelect.setOnClickListener(v -> {
            Date date;
            try {
                date = new SimpleDateFormat("HH:mm").parse(on);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
//                 on below line we are getting the
            // instance of our calendar.
//                final Calendar c = Calendar.getInstance();
//                // on below line we are getting our hour, minute.
//                int hour = c.get(Calendar.HOUR_OF_DAY);
//                int minute = c.get(Calendar.MINUTE);

            // on below line we are initializing our Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(JadwalActivity.this,
                    (view, hourOfDay, minute1) -> {
                        // on below line we are setting selected time
                        // in our text view.
                        String waktu = hourOfDay + ":" + minute1;
                        timeStart.setText(waktu);

                        timeStartS = waktu;
                    }, hour, minute, true);
            // at last we are calling show to
            // display our time picker dialog.
            timePickerDialog.show();
        });

        timeEndSelect.setOnClickListener(v -> {

            Date date;
            try {
                date = new SimpleDateFormat("HH:mm").parse(off);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
////                 on below line we are getting the
//                // instance of our calendar.
//                final Calendar c = Calendar.getInstance();
//
//                // on below line we are getting our hour, minute.
//                int hour = c.get(Calendar.HOUR_OF_DAY);
//                int minute = c.get(Calendar.MINUTE);

            // on below line we are initializing our Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(JadwalActivity.this,
                    (view, hourOfDay, minute12) -> {
                        // on below line we are setting selected time
                        // in our text view.
                        String waktu = hourOfDay + ":" + minute12;
                        timeEnd.setText(waktu);

                        timeEndS = waktu;
                    }, hour, minute, true);
            // at last we are calling show to
            // display our time picker dialog.
            timePickerDialog.show();
        });

        UpdateScheduleButton.setOnClickListener(v -> {
            String timeStartS = timeStart.getText().toString();
            String timeEndS = timeEnd.getText().toString();
            checkJamUpdate(timeStartS, timeEndS, id);
        });

        DeleteScheduleButton.setOnClickListener(v -> alertConfDelete("Are you sure you want to delete this jadwal?", id));
        dialog.show();
    }

    //used to check the time inputted by user from updateSchedulePop()
    private void checkJamUpdate(String timeStartS, String timeEndS, String id) {
        if(timeStartS.isEmpty() || timeEndS.isEmpty()){
            alertFail("Please select start and end time");
        }
        else {
            alertConfUpdate("Are you sure you want to update this jadwal?", timeStartS, timeEndS, id);
        }
    }

    //used to store the updated schedule
    //TODO: http request update
    private void updateSchedule(String timeStartS, String timeEndS, String id_jadwal) {
        JSONObject params = new JSONObject();
        try {
            params.put("jam_on", timeStartS);
            params.put("jam_off", timeEndS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String data = params.toString();
        String url = getString(R.string.api_server)+"jadwals/"+id_jadwal;

        new Thread(() -> {
            Http http = new Http(JadwalActivity.this, url);
            http.setMethod("PATCH");
            http.setAccess_token(true);
            http.setData(data);
            http.send();

            runOnUiThread(() -> {
                Integer responseCode = http.getResponseCode();

                if (responseCode == 200) {
                    try {
                        JSONObject response = new JSONObject(http.getResponse());
                        String message = response.getString("message");
                        alertSuccess(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
                    Toast.makeText(JadwalActivity.this, "Something went wrong! code : " + responseCode, Toast.LENGTH_SHORT).show();
                }
            });
        }).start();

//        http update request
    }

    //used to delete the clicked schedule
    //TODO: http request delete
    private void deleteSchedule(String id_alat) {
        String url = getString(R.string.api_server)+"jadwals/"+id_alat;

        new Thread(new Runnable() {
            @Override
            public void run() {
                Http http = new Http(JadwalActivity.this, url);
                http.setMethod("DELETE");
                http.setAccess_token(true);
                http.send();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Integer responseCode = http.getResponseCode();

                        if (responseCode == 200) {
                            try {
                                JSONObject response = new JSONObject(http.getResponse());
                                String arrResponse = response.getString("message");
                                alertSuccess(arrResponse);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


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
                            Toast.makeText(JadwalActivity.this, "Something went wrong! code : " + responseCode, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
//        http request delete
    }

    private void alertFail(String s) {
        new AlertDialog.Builder(JadwalActivity.this)
                .setTitle("Failed !")
                .setMessage(s)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void alertConfAdd(String s) {
        new AlertDialog.Builder(JadwalActivity.this)
                .setTitle("Confirmation")
                .setMessage(s)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    storeSchedule();
                }
                )
                .setNegativeButton("NO", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void alertConfUpdate(String s, String timeStartS, String timeEndS, String id) {
        new AlertDialog.Builder(JadwalActivity.this)
                .setTitle("Update Confirmation")
                .setMessage(s)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    updateSchedule(timeStartS, timeEndS, id);
                }
                )
                .setNegativeButton("NO", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void alertConfDelete(String s, String id) {
        new AlertDialog.Builder(JadwalActivity.this)
                .setTitle("Confirmation")
                .setMessage(s)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    deleteSchedule(id);
                }
                )
                .setNegativeButton("NO", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void alertSuccess(String s) {
        new AlertDialog.Builder(JadwalActivity.this)
                .setTitle("Success !")
                .setMessage(s)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                    startActivity(getIntent());
                }
                )
                .show();
    }

    public String hourNMinOnly(String str) {
        if (str != null && str.length() > 0) {
            str = str.substring(0, str.length() - 3);
        }
        return str;
    }
}