package com.lokacodes.kebunpintar;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.lokacodes.kebunpintar.databinding.ActivityGardenInfoBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class GardenInfoActivity extends AppCompatActivity {

    ActivityGardenInfoBinding binding;
    String id_kebun, nama_kebun, lokasi_kebun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGardenInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = this.getIntent();
        if (intent != null){
            id_kebun = intent.getStringExtra("id_kebun");
        }

        getKebunData();

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GardenInfoActivity.this, GardenMonitorActivity.class);
                intent.putExtra("id_kebun", id_kebun);
                startActivity(intent);
            }
        });

        binding.btEdit.setOnClickListener(v -> {
//            tampilkan dialog edit
            editPopUp();
//            setelah submit panggil method updateKebun()
        });

        binding.tvDeleteGarden.setOnClickListener(v -> {
            alertConfDelete("Are you sure want to delete this garden?");
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(GardenInfoActivity.this, GardenMonitorActivity.class);
        intent.putExtra("id_kebun", id_kebun);
        startActivity(intent);
    }

    private void getKebunData() {

        String url = getString(R.string.api_server)+"kebuns/"+id_kebun;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Http http = new Http(GardenInfoActivity.this, url);
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
                                nama_kebun = kebun.getString("nama_kebun");
                                lokasi_kebun = kebun.getString("lokasi_kebun");

                                binding.namaGarden.setText(nama_kebun);
                                binding.tvLocationGarden.setText(lokasi_kebun);
                                binding.tvDeviceIDGarden.setText(id_kebun);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            Toast.makeText(GardenInfoActivity.this, "Failed to get user data" + code, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void editPopUp() {
        Dialog dialog = new Dialog(this, R.style.DialogStyle);
        dialog.setContentView(R.layout.edit_garden_popup);

        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_pop_up));

        EditText namaKebun = dialog.findViewById(R.id.etGardenName);
        EditText lokasiKebun = dialog.findViewById(R.id.etGardenLocation);

        namaKebun.setText(nama_kebun);
        lokasiKebun.setText(lokasi_kebun);

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
                String nama_kebunInput = namaKebun.getText().toString();
                String lokasi_kebunInput = lokasiKebun.getText().toString();
                checkKebun(nama_kebunInput, lokasi_kebunInput);
            }
        });
        dialog.show();
    }

    private void checkKebun(String nama_kebunInput, String lokasi_kebunInput) {
        if (nama_kebunInput.isEmpty() || lokasi_kebunInput.isEmpty()){
            alertFail("Please fill all the field");
        } else if (nama_kebunInput.equals(nama_kebun) && lokasi_kebunInput.equals(lokasi_kebun)){
            alertFail("Nothing changed");
        } else {
            alertConf("Are you sure want to update this garden?",nama_kebunInput,lokasi_kebunInput);
        }
    }

    private void deleteKebun() {
//        http request delete kebun
//        JSONObject params = new JSONObject();
//        try {
//            params.put("nama_kebun", nama_kebunInput);
//            params.put("lokasi_kebun", lokasi_kebunInput);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        String data = params.toString();
        String url = getString(R.string.api_server)+"kebuns/"+id_kebun;

        new Thread(new Runnable() {
            @Override
            public void run() {
                Http http = new Http(GardenInfoActivity.this, url);
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
                                alertSuccessDelete(arrResponse);

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
                            Toast.makeText(GardenInfoActivity.this, "Something went wrong! code : " + responseCode, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
//        jika berhasil, redirect ke mainactivity
    }

    private void updateKebun(String nama_kebunInput, String lokasi_kebunInput) {
//        http request update kebun
        JSONObject params = new JSONObject();
        try {
            params.put("nama_kebun", nama_kebunInput);
            params.put("lokasi_kebun", lokasi_kebunInput);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String data = params.toString();
        String url = getString(R.string.api_server)+"kebuns/"+id_kebun;

        new Thread(new Runnable() {
            @Override
            public void run() {
                Http http = new Http(GardenInfoActivity.this, url);
                http.setMethod("PATCH");
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
                                String id_kebun = kebun.getString("id");
                                String nama_kebun = kebun.getString("nama_kebun");
                                String lokasi_kebun = kebun.getString("lokasi_kebun");

                                binding.namaGarden.setText(nama_kebun);
                                binding.tvLocationGarden.setText(lokasi_kebun);
                                binding.tvDeviceIDGarden.setText(id_kebun);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            alertSuccess("Edit Garden Success!");
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
                            Toast.makeText(GardenInfoActivity.this, "Something went wrong! code : " + responseCode, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
//        jika berhasil, redirect ke gardenmonitoractivity
    }

    private void alertFail(String s) {
        new AlertDialog.Builder(GardenInfoActivity.this)
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

    private void alertConf(String s,String nama_kebunInput, String lokasi_kebunInput) {
        new AlertDialog.Builder(GardenInfoActivity.this)
                .setTitle("Update Confirmation")
                .setMessage(s)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                updateKebun(nama_kebunInput,lokasi_kebunInput);
                                dialog.dismiss();
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

    private void alertConfDelete(String s) {
        new AlertDialog.Builder(GardenInfoActivity.this)
                .setTitle("Delete Confirmation")
                .setMessage(s)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteKebun();
                                dialog.dismiss();
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
        new AlertDialog.Builder(GardenInfoActivity.this)
                .setTitle("Success")
                .setMessage(s)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(GardenInfoActivity.this, GardenMonitorActivity.class);
                                intent.putExtra("id_kebun", id_kebun);
                                startActivity(intent);
                                dialog.dismiss();
                            }
                        }
                )
                .show();
    }

    private void alertSuccessDelete(String s) {
        new AlertDialog.Builder(GardenInfoActivity.this)
                .setTitle("Success")
                .setMessage(s)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(GardenInfoActivity.this, MainActivity.class);
                                startActivity(intent);
                                dialog.dismiss();
                            }
                        }
                )
                .show();
    }
}