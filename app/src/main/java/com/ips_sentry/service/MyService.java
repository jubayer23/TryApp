package com.ips_sentry.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;

import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ips_sentry.appdata.AppController;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.utils.ConnectionDetector;
import com.ips_sentry.utils.GPSTracker;
import com.ips_sentry.ips.MainActivity;
import com.ips_sentry.ips.R;
import com.ips_sentry.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class MyService extends Service {
    Calendar cur_cal = Calendar.getInstance();


    // GPS Location
    GPSTracker gps;

    // Connection detector class
    ConnectionDetector cd;

    private SaveManager saveManager;
    private static String KEY_STATUS = "status";
    private static String KEY_SESSION_TOKEN = "sessionId";
    private com.ips_sentry.AlarmManager.AlarmManager myAlarm;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        saveManager = new SaveManager(this);

        myAlarm = new com.ips_sentry.AlarmManager.AlarmManager(getApplicationContext());

        if(saveManager.getSignInOut())
        {
            Intent intent = new Intent(this, MyService.class);
            PendingIntent pintent = PendingIntent.getService(
                    getApplicationContext(), 0, intent,  PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            cur_cal.setTimeInMillis(System.currentTimeMillis());
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, cur_cal.getTimeInMillis(),
                    4 * 1000, pintent);
        }else
        {
                myAlarm.stopAlarm();
                myAlarm.stopAlarmForChecking();
        }





        cd = new ConnectionDetector(getApplicationContext());

        // creating GPS Class object
        gps = new GPSTracker(this);





    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        // your code for background process
        String session_id = saveManager.getSessionToken();


        Log.d("DEBUG", "onStart");

        if (!session_id.equals("0")) {
            //Log.d("DEBUG", "user enable");
            if (cd.isConnectingToInternet()) {
                //Log.d("DEBUG", "user connextion ok");
                if (gps.canGetLocation()) {

                    //Log.d("DEBUG", "user gps ok");

                    String user_lat = String.valueOf(gps.getLatitude());
                    String user_lang = String.valueOf(gps.getLongitude());

                    String URL = Constant.URL_GPSUpdate
                            + "sessionId="
                            + session_id
                            + "&lat="
                            + user_lat
                            + "&lng="
                            + user_lang;

                    //Log.d("DEBUG", URL);

                    hitUrl(URL);
                }
            }
        }

    }

    private void hitUrl(String uRL) {
        // TODO Auto-generated method stub
        final JsonObjectRequest jsObjRequest = new JsonObjectRequest(
                Request.Method.GET, uRL, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        //String textResult = response.toString();

                        //Log.d("DEBUG_SUCESS", textResult);
                        try {
                            int status = response.getInt("statusCodeId");
                            //  Log.d("DEBUG_CODE", String.valueOf(status));

                            int id;
                            if (status == 1) {
                                // id = response.getInt("status");
                                Log.d("DEBUG_GPSUPDAE", String.valueOf(status));
                                //startAlarm();
                                //startAlarm();


                            } else if (status == 2) {


                                myAlarm.stopAlarm();

                                myAlarm.stopAlarmForChecking();

                                deleteSession();

                                String reLoginUrl = Constant.URL_LOGIN +
                                        "username=" + saveManager.getUserName() + "&password=" + saveManager.getUserPassword();


                                TryAutoLogin(reLoginUrl);
                            } else {
                                Log.d("DEBUg", "OtherError");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {


            }
        });
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(jsObjRequest);
    }


    private void TryAutoLogin(String url) {

        final JsonObjectRequest jsObjRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {


                        // Log.d("DEBUG_SUCESS", textResult);
                        try {
                            boolean status = response.getBoolean(KEY_STATUS);
                            String session_id;
                            if (status) {


                                session_id = response.getString(KEY_SESSION_TOKEN);

                                saveSession(session_id);

                                myAlarm.startOneTimeAlrm();

                            } else {


                                setupNotification();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                if (error != null)
                    Log.e("MainActivity", error.getMessage());

            }
        });

        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(jsObjRequest);

    }

    private void deleteSession() {


        saveManager.setSessionToken("0");


    }

    private void saveSession(String session_id) {
        saveManager.setSessionToken(session_id);
    }


    private void setupNotification() {
        PowerManager pm = (PowerManager) getApplicationContext()
                .getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");
        // Acquire the lock
        wl.acquire();


        Uri notification = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();

        showNotification(getApplicationContext());

        wl.release();
    }

    private void showNotification(Context context) {
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("ISP SENTRY")
                        .setContentText("Please Lgoin Again");
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        //Log.d("DEBUG", "onBind");
        return null;
    }
}
