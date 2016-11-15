package com.ips_sentry;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ips_sentry.appdata.AppController;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.dialog.AlertDialogManager;
import com.ips_sentry.ips.R;
import com.ips_sentry.service.MyServiceUpdate;
import com.ips_sentry.utils.ConnectionDetector;
import com.ips_sentry.utils.Constant;
import com.ips_sentry.utils.DeviceInfoUtils;
import com.ips_sentry.utils.LastLocationOnly;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnClickListener {


    //in master change

    private EditText et_username, et_password;

    private Button btn_submit, btn_logout, map_show;

    private TextView tvNearByVenues;

    private String username, password;

    private static String URL;

    private static String KEY_STATUS = "status";
    private static String KEY_SESSION_TOKEN = "sessionId";
    public static String KEY_TRAFFIC_INFO = "showTrafficInfo";
    private static final String KEY_GPS_INTERVAL = "gpsIntervals";
    private static final String KEY_GPS_STOP_THRESHOLD = "stoppedThreshold";
    public static String KEY_SHOWINDIVIDUAL_LABEL = "showIndividualLabels";

    // Progress Dialog
    private ProgressDialog pDialog;


    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();


    private ConnectionDetector cd;

    private SaveManager saveManager;


    private LastLocationOnly lastLocationOnly;

    private TextView tv_power_by;

    private ImageView btn_setting;

    private ImageView img_logo;

    private Runnable myRunnable;

    private boolean isOpen = true;

    private static int username_length = 0, password_length = 0;

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

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        init();


        et_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    //Log.d("DEBUG","Enter pressed");

                    processSubmitButton();
                }
                return false;
            }
        });


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

        lastLocationOnly = new LastLocationOnly(this);


        if (!cd.isConnectingToInternet()) {
            //Internet Connection is not present
            alert.showAlertDialog(MainActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            //stop executing code by return
            return;
        }
        if (!lastLocationOnly.canGetLocation()) {
            lastLocationOnly.showSettingsAlert();

            return;
        }

        saveManager.setUserLat(String.valueOf(lastLocationOnly.getLatitude()));
        saveManager.setUserLang(String.valueOf(lastLocationOnly.getLongitude()));


        if (!et_username.getText().toString().isEmpty())
            username_length = et_username.getText().toString().length();
        if (!et_password.getText().toString().isEmpty())
            password_length = et_password.getText().toString().length();

        startLogoAnimation();


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

        btn_setting = (ImageView) findViewById(R.id.btn_usersetting);
        btn_setting.setOnClickListener(this);

        img_logo = (ImageView) findViewById(R.id.logo);


    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();

        if (id == R.id.submit) {

            processSubmitButton();


        }
        if (id == R.id.logout) {

        }

        if (id == R.id.mapshow) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        }

        if (id == R.id.tvNearByVenues) {

            if (!cd.isConnectingToInternet()) {
                //Internet Connection is not present
                alert.showAlertDialog(MainActivity.this, "Internet Connection Error",
                        "Please connect to working Internet connection", false);
                //stop executing code by return
                return;
            }
            if (!lastLocationOnly.canGetLocation()) {
                lastLocationOnly.showSettingsAlert();

                return;
            }


            Intent intent = new Intent(MainActivity.this, NearByVenues2.class);

            startActivity(intent);
        }

        if (id == R.id.tv_power_by) {
            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
            startActivity(intent);
        }

        if (id == R.id.btn_usersetting) {

            showDialogForSetting();


        }


    }

    private void processSubmitButton() {

        username = et_username.getText().toString();
        password = et_password.getText().toString();
        lastLocationOnly = new LastLocationOnly(this);

        if (!validate(username, password)) {
            //onLoginFailed();
            return;
        }
        if (!cd.isConnectingToInternet()) {
            //Internet Connection is not present
            alert.showAlertDialog(MainActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            //stop executing code by return
            return;
        }
        if (!lastLocationOnly.canGetLocation()) {
            lastLocationOnly.showSettingsAlert();

            return;
        }


        pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setMessage("Loading.... Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();


        String phoneNumber = DeviceInfoUtils.getPhoneNumber(MainActivity.this);
        if (phoneNumber == null) {
            phoneNumber = "";
        }

        // Log.d("DEBUG",phoneNumber + " "+ ipAddress);

       hitUrl(saveManager.getUrlEnv() + Constant.URL_LOGIN, phoneNumber);

       // hitUrlForSessionCheck( Constant.URL_SESSION_TEST);

    }



    private void hitUrl(String url, final String PHONE) {
        // TODO Auto-generated method stub

        //Log.d("DEBUG",url);
        //url = "http://ips-systems.com/home/mobileappsignin?username=pca02&password=password";

        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pDialog.dismiss();

                        // Log.d("DEBUG",response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean status = jsonObject.getBoolean(KEY_STATUS);
                            String session_id;
                            if (status) {


                                session_id = jsonObject.getString(KEY_SESSION_TOKEN);

                               // Log.d("DEBUG",session_id);
                                saveManager.setUserName(username);

                                saveManager.setUserPassword(password);

                                saveManager.setSignInOut(true);

                                saveManager.setSessionToken(session_id);

                                saveManager.setTrafficInfo(jsonObject.getBoolean(KEY_TRAFFIC_INFO));

                                saveManager.setIndividualLabel(jsonObject.getBoolean(KEY_SHOWINDIVIDUAL_LABEL));

                                saveManager.setGpsInterval(jsonObject.getInt(KEY_GPS_INTERVAL));
                                saveManager.setPermanentGpsInterval(jsonObject.getInt(KEY_GPS_INTERVAL));

                                saveManager.setStoppedThreshold(jsonObject.getInt(KEY_GPS_STOP_THRESHOLD));

                                saveManager.setRecordTime(0);

                                //1800 sec
                                //myAlarm.startRepeatingAlarm(6);

                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);

                                Intent intent2 = new Intent(MainActivity.this,
                                        MyServiceUpdate.class);
                                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startService(intent2);

                                visibleInvisible();

                                finish();
                            } else {

                                String message = jsonObject.getString("message");
                                alert.showAlertDialog(MainActivity.this, "Error", message, false);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {
                    Toast.makeText(MainActivity.this, "errorMessage:" + response.statusCode, Toast.LENGTH_SHORT).show();
                } else {
                    String errorMessage = error.getClass().getSimpleName();
                    if (!TextUtils.isEmpty(errorMessage)) {
                        Toast.makeText(MainActivity.this, "errorMessage:" + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("password", password);
                params.put("Phone", PHONE);
                //Log.d("DEBUG_selected",String.valueOf(selected));
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
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

    public boolean validate(String username, String password) {
        boolean valid = true;


        if (username.isEmpty()) {
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

        if (!(username.isEmpty() && password.isEmpty())) {
            if (username.isEmpty() && !password.isEmpty()) {
                et_username.requestFocus();
            }
            if (!username.isEmpty() && password.isEmpty()) {
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

    public void showDialogForSetting() {
        final Dialog dialog = new Dialog(this,
                android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_settingpassword);


        final EditText et_dialog_password = (EditText) dialog.findViewById(R.id.dialog_password);

        Button btn_submit = (Button) dialog.findViewById(R.id.dialog_submit);

        btn_submit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = et_dialog_password.getText().toString().trim();


                if (password.isEmpty()) {
                    et_dialog_password.setError("Enter Password");
                    return;
                }
                if (password.equals(Constant.ADMIN_PASSWORD)) {
                    Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                    startActivity(intent);

                    dialog.dismiss();
                } else {
                    et_dialog_password.setError("Wrong Password");
                }
            }
        });


        dialog.show();


    }


    private void startLogoAnimation() {


        final int[] logo_now_using = {0};


        myRunnable = new Runnable() {
            int updateInterval = 1000; //=one second
            boolean stop = false;

            @Override
            public void run() {

                //Log.d("DEBUG","YES");


                // Any code which goes here will be executed every 'updateInterval'
                // change your background here


                if (logo_now_using[0] == 0) {


                    //Log.d("DEBUG",String.valueOf(et_username.getText().toString().length()) + " " +
                    //        String.valueOf(username_length));
                   // Log.d("DEBUG2",String.valueOf(et_password.getText().toString().length()) + " " +
                   //         String.valueOf(password_length));

                    if ((et_username.getText().toString().length() != username_length) || (et_password.getText().toString().length() != password_length)) {
                        isOpen = false;

                    } else {
                        isOpen = true;
                    }


                    img_logo.setImageResource(R.drawable.c);

                    logo_now_using[0] = 1;


                    updateInterval = 200;


                } else if (logo_now_using[0] == 1) {


                    // Log.d("DEBUG", String.valueOf(isOpen));


                    if (isOpen) {
                        img_logo.setImageResource(R.drawable.a);

                    } else {
                        img_logo.setImageResource(R.drawable.b);
                    }


                    logo_now_using[0] = 0;

                    updateInterval = 4000;
                }


                if (!stop) {
                    img_logo.postDelayed(this, updateInterval);
                }
            }
        };

        myRunnable.run();


    }


    @Override
    protected void onPause() {
        super.onPause();


        img_logo.removeCallbacks(myRunnable);
    }


}
