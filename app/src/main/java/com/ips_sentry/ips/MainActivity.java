package com.ips_sentry.ips;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ips_sentry.appdata.AppController;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.dialog.AlertDialogManager;
import com.ips_sentry.service.MyServiceUpdate;
import com.ips_sentry.setting.ConnectionDetector;
import com.ips_sentry.setting.GPSTracker;
import com.ips_sentry.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity implements OnClickListener {

    //in master change

    EditText et_username, et_password;

    Button btn_submit, btn_logout, map_show;

    TextView tvNearByVenues;

    String username, password;

    private static String URL;

    private static String KEY_STATUS = "status";
    private static String KEY_SESSION_TOKEN = "sessionId";

    // Progress Dialog
    private ProgressDialog pDialog;


    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

    // GPS Location
    GPSTracker gps;

    ConnectionDetector cd;

    private SaveManager saveManager;

    private com.ips_sentry.AlarmManager.AlarmManager myAlarm;

    private TextView tv_power_by;

    private ImageView btn_setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        saveManager = new SaveManager(this);

        if (!saveManager.getSessionToken().equals("0")) {
            saveManager.setRecordTime(0);

            restartService();

            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);

            finish();

            return;


        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        init();




        myAlarm = new com.ips_sentry.AlarmManager.AlarmManager(this);


        //visibleInvisible();
        // creating GPS Class object


    }

    @Override
    public void onResume() {
        super.onResume();

        visibleInvisible();

        if (!saveManager.getSessionToken().equals("0")) {



        }

        cd = new ConnectionDetector(this);

        gps = new GPSTracker(this);


        if (!cd.isConnectingToInternet()) {
            //Internet Connection is not present
            alert.showAlertDialog(MainActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            //stop executing code by return
            return;
        }
        if (!gps.canGetLocation()) {
            gps.showSettingsAlert();

            return;
        }

        saveManager.setUserLat(String.valueOf(gps.getLatitude()));
        saveManager.setUserLang(String.valueOf(gps.getLongitude()));

    }

    private void restartService() {
        stopService(new Intent(MainActivity.this, MyServiceUpdate.class));

        startService(new Intent(MainActivity.this, MyServiceUpdate.class));
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

        tvNearByVenues = (TextView) findViewById(R.id.tvNearByVenues);
        tvNearByVenues.setOnClickListener(this);

        tv_power_by = (TextView) findViewById(R.id.tv_power_by);
        tv_power_by.setOnClickListener(this);

        btn_setting = (ImageView)findViewById(R.id.btn_usersetting);
        btn_setting.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();

        if (id == R.id.submit) {
            username = et_username.getText().toString();
            password = et_password.getText().toString();

            if (!validate(username, password)) {
                //onLoginFailed();
                return;
            }


            updateUserNameAndPassWord();

            setupApiUrl();


            gps = new GPSTracker(this);

            if (!cd.isConnectingToInternet()) {
                //Internet Connection is not present
                alert.showAlertDialog(MainActivity.this, "Internet Connection Error",
                        "Please connect to working Internet connection", false);
                //stop executing code by return
                return;
            }
            if (!gps.canGetLocation()) {
                gps.showSettingsAlert();

                return;
            }


            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading.... Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

            hitUrl();


            // Adding request to request queue


        }
        if (id == R.id.logout) {

            String URL = saveManager.getGpsUrlEnv() + Constant.URL_SIGNOUT + "sessionId=" + saveManager.getSessionToken();

            hitUrlSingOut(URL);

            stopService(new Intent(MainActivity.this, MyServiceUpdate.class));

            saveManager.setSignInOut(false);

            deleteData();

            myAlarm.stopAlarm();

            myAlarm.stopAlarmForChecking();

            visibleInvisible();
        }

        if (id == R.id.mapshow) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        }

        if (id == R.id.tvNearByVenues) {
            Intent intent = new Intent(MainActivity.this, NearByVenues.class);

            startActivity(intent);
        }

        if(id == R.id.tv_power_by)
        {
                Intent intent = new Intent(MainActivity.this,WebViewActivity.class);
                startActivity(intent);
        }

        if(id == R.id.btn_usersetting)
        {

            showDialogForSetting();



        }
    }

    private void updateUserNameAndPassWord() {
        saveManager.setUserName(username);
        saveManager.setUserPassword(password);
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
                            String session_id;
                            if (status) {


                                session_id = response.getString(KEY_SESSION_TOKEN);


                                saveManager.setSignInOut(true);

                                saveManager.setSessionToken(session_id);


                                gps.stopUsingGPS();
                                saveManager.setRecordTime(0);

                                //1800 sec
                                //myAlarm.startRepeatingAlarm(6);

                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);

                                Intent intent2 = new Intent(MainActivity.this,
                                        MyServiceUpdate.class);
                                startService(intent2);

                                visibleInvisible();

                                finish();
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
               // if (error != null)
                    //Log.e("MainActivity", error.getMessage());

            }
        });

        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(jsObjRequest);
    }

    private void hitUrlSingOut(String url) {
        // Log.d("DEBUG_signout", url);

        final JsonObjectRequest jsObjRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {


                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {


            }
        });

        AppController.getInstance().addToRequestQueue(jsObjRequest);
    }

    public void setupApiUrl() {
        URL = saveManager.getGpsUrlEnv() + Constant.URL_LOGIN + "username=" + username + "&password="
                + password;

        // Log.d("URL", URL);

    }


    private void visibleInvisible() {
        // TODO Auto-generated method stub


        if (saveManager.getSessionToken().equals("0")) {
            et_password.setVisibility(View.VISIBLE);
            et_username.setVisibility(View.VISIBLE);
            btn_submit.setVisibility(View.VISIBLE);
            btn_logout.setVisibility(View.GONE);
            map_show.setVisibility(View.GONE);

            if (!saveManager.getUserName().equalsIgnoreCase("0")) {
                et_username.setText(saveManager.getUserName());
            }
        } else {
            et_password.setVisibility(View.GONE);
            et_username.setVisibility(View.GONE);
            btn_submit.setVisibility(View.GONE);
            btn_logout.setVisibility(View.VISIBLE);
            map_show.setVisibility(View.VISIBLE);
        }
    }


    private void deleteData() {

        saveManager.setSessionToken("0");

    }

    public boolean validate(String username,String password) {
        boolean valid = true;


        if (username.isEmpty() ) {
            et_username.setError("Enter Username");
            valid = false;
        } else {
            et_username.setError(null);
        }

        if (password.isEmpty()) {
            et_password.setError("Enter Password");
            valid = false;
        } else {
            et_password.setError(null);
        }

        if(!(username.isEmpty()&&password.isEmpty()))
        {
            if(username.isEmpty()&&!password.isEmpty())
            {
                 et_username.requestFocus();
            }
            if(!username.isEmpty()&&password.isEmpty())
            {
                et_password.requestFocus();
            }
        }

        return valid;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public void showDialogForSetting()
    {
       final Dialog dialog = new Dialog(this,
                android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_settingpassword);


        final EditText et_dialog_password = (EditText)dialog.findViewById(R.id.dialog_password);

        Button btn_submit = (Button)dialog.findViewById(R.id.dialog_submit);

        btn_submit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = et_dialog_password.getText().toString().trim();


                if (password.isEmpty() ) {
                    et_dialog_password.setError("Enter Password");
                    return;
                }
                if(password.equals(Constant.ADMIN_PASSWORD))
                {
                    Intent intent = new Intent(MainActivity.this,UserSettingActivity.class);
                    startActivity(intent);

                    dialog.dismiss();
                }else
                {
                    et_dialog_password.setError("Wrong Password");
                }
            }
        });


        dialog.show();


    }

}
