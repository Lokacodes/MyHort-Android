package com.lokacodes.kebunpintar;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class UserActivity extends AppCompatActivity {

    TextView tvEmail, tvName, tvJoined, tvSlogan, tvAddress,tvBirthDate;
    String email, name, joined, slogan, address, birthDate;
    TextView btnLogout, btnDelete;

    Button btnEdit;
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
        btnEdit = findViewById(R.id.btEdit);
        btnDelete = findViewById(R.id.tvDeleteAccount);

        getUser();
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confLogout();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, MainActivity.class);
                finish();
                startActivity(intent);
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editUserPop();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confDeleteAccount();
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(UserActivity.this, MainActivity.class);
        finish();
        startActivity(intent);
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
                                name = response.getString("name");
                                email = response.getString("email");
                                joined = response.getString("created_at");
                                birthDate = response.getString("birth_date");
                                slogan = response.getString("slogan");
                                address = response.getString("address");

                                tvName.setText(name);
                                tvEmail.setText(email);
                                tvJoined.setText(joined);

                                nullOrNot(slogan,tvSlogan);
                                nullOrNot(address,tvAddress);
                                nullOrNot(birthDate,tvBirthDate);

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

    //update block
    private void editUserPop() {
        Dialog dialog = new Dialog(this, R.style.DialogStyle);
        dialog.setContentView(R.layout.edit_user_popup);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_pop_up));

        EditText name = dialog.findViewById(R.id.etUserName);
        EditText slogan = dialog.findViewById(R.id.etUserSlogan);
        EditText address = dialog.findViewById(R.id.etUserAddress);
        EditText birthDate = dialog.findViewById(R.id.etUserDoB);

        ImageView calendar = dialog.findViewById(R.id.calendarButton);


        name.setText(this.name);
        nullOrNot(this.slogan,slogan);
        nullOrNot(this.address,address);
        nullOrNot(this.birthDate,birthDate);

        ImageView close = dialog.findViewById(R.id.closeButton);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String DoB = birthDate.getText().toString();

                Date date;

                if (DoB.equals("-") || DoB.isEmpty()){
                    try {
                        DoB = java.time.LocalDate.now().toString();
                        date = new SimpleDateFormat("yyyy-MM-dd").parse(DoB);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    try {
                        date = new SimpleDateFormat("yyyy-MM-dd").parse(DoB);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }


                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int yearNew = calendar.get(Calendar.YEAR);
                int monthNew = calendar.get(Calendar.MONTH);
                int dayNew = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(UserActivity.this, (view, year, month, dayOfMonth) -> {
                    String dateNew = year + "-" + (month + 1) + "-" + dayOfMonth;
                    birthDate.setText(dateNew);
                }, yearNew, monthNew, dayNew);
                // at last we are calling show to
                // display our time picker dialog.
                datePickerDialog.show();
            }
        });

        Button btnSave = dialog.findViewById(R.id.btnSaveEditUser);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Name = name.getText().toString();
                String Slogan = slogan.getText().toString();
                String Address = address.getText().toString();
                String BirthDate = birthDate.getText().toString();

                checkUpdate(Name, Slogan, Address, BirthDate);
            }
        });
        dialog.show();
    }

    private void checkUpdate(String nameUp, String sloganUp, String addressUp, String birthDateUp) {
        if (nameUp.isEmpty() || sloganUp.isEmpty() || addressUp.isEmpty() || birthDateUp.isEmpty()){
            alertFail("Please fill all the fields");
        }
        else if (nameUp.equals(name) && sloganUp.equals(slogan) && addressUp.equals(address) && birthDateUp.equals(birthDate)){
            alertFail("No changes detected");
        }
        else {
            alertConfUpdate(nameUp, sloganUp, addressUp, birthDateUp);
        }

    }

    private void alertConfUpdate(String nameUp, String sloganUp, String addressUp, String birthDateUp) {
        new AlertDialog.Builder(UserActivity.this)
                .setTitle("Edit User Confirmation")
                .setMessage("Are you sure you want to edit your user data?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateUser(nameUp, sloganUp, addressUp, birthDateUp);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void updateUser(String name, String slogan, String address, String birthDate) {
        JSONObject params = new JSONObject();
        try {
            params.put("name", name);
            params.put("slogan", slogan);
            params.put("address", address);
            params.put("birth_date", birthDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String data = params.toString();
        String url = getString(R.string.api_server)+"update";

        new Thread(new Runnable() {
            @Override
            public void run() {
                Http http = new Http(UserActivity.this, url);
                http.setMethod("PATCH");
                http.setAccess_token(true);
                http.setData(data);
                http.send();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Integer responseCode = http.getResponseCode();

                        if (responseCode == 200) {
                            alertSuccessUpdate("Edit User Success!");
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
                            Toast.makeText(UserActivity.this, "Something went wrong! code : " + responseCode, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void alertSuccessUpdate(String s) {
        new AlertDialog.Builder(UserActivity.this)
                .setTitle("Success")
                .setMessage(s)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        startActivity(getIntent());
                        dialog.dismiss();
                    }
                })
                .show();
    }
    //end of update block


    //logout block
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
                            finish();
                            startActivity(intent);

                        }
                        else {
                            Toast.makeText(UserActivity.this, "Failed to logout. code : " + code, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    public void confLogout() {
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
    //end of logout block


    //delete block
    public void confDeleteAccount(){
        AlertDialog.Builder alert = new AlertDialog.Builder(UserActivity.this);
        alert.setTitle("WARNING");
        alert.setMessage("You are about to delete your account. Are you sure?");
        alert.setPositiveButton("Yes", (dialog, which) -> {
            deleteAccount();
        });
        alert.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        alert.show();
    }

    private void deleteAccount() {
        String url = getString(R.string.api_server)+"delete";

        new Thread(new Runnable() {
            @Override
            public void run() {
                Http http = new Http(UserActivity.this, url);
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
                            Toast.makeText(UserActivity.this, "Something went wrong! code : " + responseCode, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void alertSuccessDelete(String arrResponse) {
        new AlertDialog.Builder(UserActivity.this)
                .setTitle("Delete Account Success")
                .setMessage(arrResponse)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(UserActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        dialog.dismiss();
                    }
                })
                .show();
    }
    //end of delete block

    private void alertFail(String s) {
        new AlertDialog.Builder(UserActivity.this)
                .setTitle("Edit User Failed")
                .setMessage(s)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void nullOrNot(String text, TextView textView){
        if (text.equals("null")){
            textView.setText("-");
        }
        else {
            textView.setText(text);
        }
    }
}