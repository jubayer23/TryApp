package com.ips_sentry.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.FloatMath;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.ips_sentry.appdata.AppController;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.setting.ConnectionDetector;
import com.ips_sentry.setting.GPSTracker;
import com.ips_sentry.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MyServiceUpdate extends Service implements SensorEventListener {

    //Calendar cur_cal = Calendar.getInstance();

    Handler handler;

    private SaveManager saveManager;


    // Connection detector class
    ConnectionDetector cd;

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public LocationManager locationManager;
    public MyLocationListener listener;

    public Location previousBestLocation = null;
    public Location newBestLocation = null;

    private static int gps_interval;

    private Context _context;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;


    private static final String[] time_unit = {"day", "hours", "mins", "sec"};

    private static String time_string = " 0 mins";

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private static long lastUpdate = 0;
    float last_x = 0, last_y = 0, last_z = 0;

    //this is user for BRORCASR RECEIVER KEY
    public static final String KEY_USER_STATUS = "user_status";
    public static final String KEY_SENTRYINDIVIDUAL_RESPONSE = "sentryIndividuals_response";

    private static boolean new_start = true;

    public static final String KEY_MARKERUPDATE = "MARKERUPDATE";

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        new_start = true;

        this._context = this;

        saveManager = new SaveManager(this);

        cd = new ConnectionDetector(getApplicationContext());

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);


        // creating GPS Class object


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        new_start = true;

        locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
        listener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);

        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);



        findInBackground();
        return START_STICKY;
    }

    private void findInBackground() {
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                //cur_cal.setTimeInMillis(System.currentTimeMillis());


                java.util.Date date = new java.util.Date();
                System.out.println(new Timestamp(date.getTime()));

                //Log.d("TIME", String.valueOf(new Timestamp(date.getTime())));
                //Toast.makeText(MyService.this, String.valueOf(new Timestamp(date.getTime())) +  "{\"statusCodeId\":1}", Toast.LENGTH_SHORT).show();
            }

        };


        new Thread(new Runnable() {
            public void run() {
                // TODO Auto-generated method stub
                while (true) {


                    try {

                        if (saveManager == null) {
                            saveManager = new SaveManager(_context);
                        }

                        String session_id = saveManager.getSessionToken();

                        cd = new ConnectionDetector(_context);

                        // getting GPS status
                        isGPSEnabled = locationManager
                                .isProviderEnabled(LocationManager.GPS_PROVIDER);

                        // getting network status
                        isNetworkEnabled = locationManager
                                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                        // creating GPS Class object
                        // gps = new GPSTracker(getApplicationContext());


                        if (!session_id.equals("0")) {
                            //Log.d("DEBUG", "user enable");
                            if (cd.isConnectingToInternet()) {
                                //Log.d("DEBUG", "user connextion ok");
                                if (isGPSEnabled || isNetworkEnabled) {

                                    //Log.d("DEBUG", "user gps ok");

                                    String user_lat = saveManager.getUserLat();
                                    String user_lang = saveManager.getUserLang();


                                    hitUrl(saveManager.getGpsUrl(), session_id, user_lat, user_lang, saveManager.getUserCurrentActivity(), time_string);
                                }
                            }
                        }

                        // hitUrl("http://dev.ips-systems.com/Sentry/MobileAppUpdateLocation?sessionId=2ifkpgbzklmkygnhmpixcx24&lat=24.912785&lng=91.9535703");


                        try {
                            gps_interval = Integer.parseInt(saveManager.getGpsInterval()) * 1000;
                        } catch (Exception e) {
                            gps_interval = 10 * 1000;
                        }

                        Thread.sleep(gps_interval);


                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

            }
        }).start();
    }

    private void hitUrl(String url, final String session_id, final String lat, final String lng, final String user_activity, final String time_string) {
        // TODO Auto-generated method stub


        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);


                            int status = jsonObject.getInt("statusCodeId");
                            //  Log.d("DEBUG_CODE", String.valueOf(status));


                            int id;
                            if (status == 1) {
                                // id = response.getInt("status");
                                //Log.d("DEBUG_GPSUPDAE", String.valueOf(status));
                                //startAlarm();
                                //startAlarm();
                                //handler.sendEmptyMessage(0);
                                //Fire the intent with activity name & confidence
                                Intent i = new Intent(getPackageName() + KEY_MARKERUPDATE);
                                i.putExtra(KEY_SENTRYINDIVIDUAL_RESPONSE, response);
                                LocalBroadcastManager.getInstance(_context).sendBroadcast(i);


                            } else if (status == 2) {
                                Log.d("DEBUG_GPSUPDAE_ERROR", String.valueOf(status));
                            } else {
                                Log.d("DEBUG_GPSUPDAE_ERROR", "OtherError");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                Map<String, String> params = new HashMap<String, String>();
                params.put("sessionId", session_id);
                params.put("lat", lat);
                params.put("lng", lng);
                params.put("status", user_activity + ":" + time_string);
                //Log.d("DEBUG_selected",String.valueOf(selected));
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        //Log.d("DEBUG", "onBind");
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(final Location loc) {
            // Log.i("**************************************", "Location changed");
            if (isBetterLocation(loc, previousBestLocation)) {
                newBestLocation = loc;

                saveManager.setUserLat(String.valueOf(loc.getLatitude()));
                saveManager.setUserLang(String.valueOf(loc.getLongitude()));

                //loc.getLatitude();
                //loc.getLongitude();


            /*final Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            String Text = "";
            try {
                List<Address> addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                Text = "My current location is: "+addresses.get(0).getAddressLine(0);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Text = "My current location is: " +"Latitude = " + loc.getLatitude() + ", Longitude = " + loc.getLongitude();
            }
            */
                //Toast.makeText( getApplicationContext(), "Location polled to server", Toast.LENGTH_SHORT).show();
            }
        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }


        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }


        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

    }


    /**
     * SENSOR MANAGER
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];


            long curTime = System.currentTimeMillis();


            if ((curTime - lastUpdate) >= 1000) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;


                // Log.d("DEBUG", String.valueOf(Math.abs(x - last_x)) + " " + String.valueOf(Math.abs(y - last_y)) + " " + String.valueOf(Math.abs(z - last_z)));

                String user_activity;
                // if (Math.abs(x - last_x) >= 2 || Math.abs(y - last_y) >= 2 || Math.abs(z - last_z) >= 2) {
                //    Log.d("DEBUG_MOVE", String.valueOf(Math.abs(x - last_x)) + " " + String.valueOf(Math.abs(y - last_y)) + " " + String.valueOf(Math.abs(z - last_z)));
                //    user_activity = Constant.USER_ACTIVITY_MOVING;
                //
                // } else {
                //    user_activity = Constant.USER_ACTIVITY_IDLE;
                // }

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;
                //Log.d("DEBUG", String.valueOf(speed));

                if (new_start) {
                    new_start = false;
                    user_activity = saveManager.getUserCurrentActivity();
                } else if (speed <= 5) {
                    user_activity = Constant.USER_ACTIVITY_IDLE;

                } else {
                    user_activity = Constant.USER_ACTIVITY_MOVING;
                }

                if (saveManager.getUserCurrentActivity().equalsIgnoreCase(Constant.USER_ACTIVITY_STOPPED) && user_activity.equalsIgnoreCase(Constant.USER_ACTIVITY_IDLE)) {
                    user_activity = Constant.USER_ACTIVITY_STOPPED;
                }


                if (saveManager.getUserCurrentActivity().equalsIgnoreCase(user_activity) && saveManager.getRecordTime() > 0) {

                    long time_interval_between_user_activity = curTime - saveManager.getRecordTime();

                    int time_x;
                    int time_array[] = new int[4];

                    time_x = (int) (time_interval_between_user_activity / 1000);
                    time_array[3] = time_x % 60;
                    time_x /= 60;
                    time_array[2] = time_x % 60;
                    time_x /= 60;
                    time_array[1] = time_x % 24;
                    time_x /= 24;
                    time_array[0] = time_x;

                    //i.e. "Idle: 3 mins", "Stopped: 6 hours, 3 mins", "Moving: 36 mins", "Stopped: 1 hour"
                    time_string = "";
                    for (int loop = 0; loop < 3; loop++) {
                        if (time_array[loop] == 0) continue;
                        if (loop == 2) {
                            time_string = time_string + " " + String.valueOf(time_array[loop]) + " " + time_unit[loop];
                        } else {
                            time_string = time_string + " " + String.valueOf(time_array[loop]) + " " + time_unit[loop] + ",";
                        }
                    }


                    if ((time_array[0] >= 1 || time_array[1] >= 1 || time_array[2] >= 5) && saveManager.getUserCurrentActivity().equalsIgnoreCase(Constant.USER_ACTIVITY_IDLE)) {
                        time_string = " 0 mins";
                        saveManager.setRecordTime(curTime);
                        saveManager.setUserCurrentActivity(Constant.USER_ACTIVITY_STOPPED);
                    }

                    if (time_string.length() <= 1) time_string = " 0 mins";


                } else {
                    time_string = " 0 mins";
                    //recorded_time = curTime;
                    saveManager.setRecordTime(curTime);
                    saveManager.setUserCurrentActivity(user_activity);
                }

                //Fire the intent with activity name & confidence
                Intent i = new Intent(getPackageName() + "ImActive");
                i.putExtra(KEY_USER_STATUS, saveManager.getUserCurrentActivity() + ":" + time_string);
                LocalBroadcastManager.getInstance(this).sendBroadcast(i);

                //Log.d("onSensorChanged", System.currentTimeMillis() + "=speed=" + speed);

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        // Log.v("STOP_SERVICE", "DONE");
        locationManager.removeUpdates(listener);
        senSensorManager.unregisterListener(this);
    }
}
