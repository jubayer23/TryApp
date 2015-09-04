package com.example.tryapp;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.appdata.AppController;
import com.example.appdata.SaveManager;
import com.example.dialog.AlertDialogManager;
import com.example.service.MyService;
import com.example.setting.GPSTracker;
import com.example.utils.Constant;

public class MainActivity extends Activity implements OnClickListener {

    //in master change

    EditText et_username, et_password;

    Button btn_submit, btn_logout, map_show;

    String username, password;

    private static String URL;

    private static String KEY_STATUS = "status";
    private static String KEY_USER_ID = "id";

    // Progress Dialog
    private ProgressDialog pDialog;


    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

    // GPS Location
    GPSTracker gps;

    private SaveManager saveManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();


        saveManager = new SaveManager(this);
        //visibleInvisible();
        // creating GPS Class object
        gps = new GPSTracker(this);

    }

    private void init() {
        // TODO Auto-generated method stub
        et_username = (EditText) findViewById(R.id.username);
        et_password = (EditText) findViewById(R.id.password);

        btn_submit = (Button) findViewById(R.id.submit);
        btn_submit.setOnClickListener(this);

        btn_logout = (Button) findViewById(R.id.logout);
        btn_logout.setOnClickListener(this);

        map_show = (Button) findViewById(R.id.mapshow);
        map_show.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();

        if (id == R.id.submit) {
            username = et_username.getText().toString();
            password = et_password.getText().toString();

            if (username.length() > 0 && password.length() > 0) {
                setupApiUrl();


                if (gps.canGetLocation()) {
                    pDialog = new ProgressDialog(MainActivity.this);
                    pDialog.setMessage("Loading products. Please wait...");
                    pDialog.setIndeterminate(false);
                    pDialog.setCancelable(false);
                    pDialog.show();

                    hitUrl();
                } else {
                    alert.showAlertDialog(MainActivity.this, "GPS",
                            "PLEASE ENALE GPS FIRST", false);
                }

                // Adding request to request queue

            }

        }
        if (id == R.id.logout) {
            deleteData();

            disableService();

            visibleInvisible();
        }

        if (id == R.id.mapshow) {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        }
    }

    private void hitUrl() {

        final JsonObjectRequest jsObjRequest = new JsonObjectRequest(
                Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        pDialog.dismiss();
                        String textResult = response.toString();

                        // Log.d("DEBUG_SUCESS", textResult);
                        try {
                            boolean status = response.getBoolean(KEY_STATUS);
                            int id;
                            if (status) {
                                id = response.getInt(KEY_USER_ID);

                                saveManager.setUserId(String.valueOf(id));


                                gps.stopUsingGPS();

                                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                                startActivity(intent);

                                Intent intent2 = new Intent(MainActivity.this,
                                        MyService.class);
                                startService(intent2);

                                visibleInvisible();
                            } else {
                                alert.showAlertDialog(MainActivity.this, "Error", "Invalid credentials!", false);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                if (error != null)
                    Log.e("MainActivity", error.getMessage());

            }
        });
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(jsObjRequest);
    }

    public void setupApiUrl() {
        URL = Constant.URL_LOGIN + "username=" + username + "&password="
                + password;

        // Log.d("URL", URL);

    }


    @Override
    public void onResume() {
        super.onResume();
        visibleInvisible();
        ;
    }

    private void visibleInvisible() {
        // TODO Auto-generated method stub


        if (saveManager.getUserId().equals("0")) {
            et_password.setVisibility(View.VISIBLE);
            et_username.setVisibility(View.VISIBLE);
            btn_submit.setVisibility(View.VISIBLE);
            btn_logout.setVisibility(View.GONE);
            map_show.setVisibility(View.GONE);
        } else {
            et_password.setVisibility(View.GONE);
            et_username.setVisibility(View.GONE);
            btn_submit.setVisibility(View.GONE);
            btn_logout.setVisibility(View.VISIBLE);
            map_show.setVisibility(View.VISIBLE);
        }
    }

    private void disableService() {
        Intent intent = new Intent(getApplicationContext(), MyService.class);
        PendingIntent pintent = PendingIntent.getService(
                getApplicationContext(), 0, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pintent);
    }

    private void deleteData() {

        saveManager.setUserId("0");

    }
}
