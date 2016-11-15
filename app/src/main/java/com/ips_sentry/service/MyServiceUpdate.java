package com.ips_sentry.service;

import android.app.Service;
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
import android.location.LocationProvider;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.ips_sentry.appdata.AppController;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.ips.R;
import com.ips_sentry.utils.ConnectionDetector;
import com.ips_sentry.utils.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;


public class MyServiceUpdate extends Service implements SensorEventListener {
    public static final String KEY_BROADCAST_FOR_SCREEN_DIM = "screen_dem";
    public static final String KEY_SCREEN_DIM_ON_OFF = "screen_onoroff";

    public static final String KEY_BROADCAST_FOR_MESSAGE = "intent_message";
    public static final String KEY_MESSAGE = "key_message";


    //Calendar cur_cal = Calendar.getInstance();

    Handler handler;

    private SaveManager saveManager;


    // Connection detector class
    ConnectionDetector cd;

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public LocationManager locationManager;
    public MyLocationListener listener;

    public Location previousBestLocation = null, previousBestLocation2 = null;


    private Context _context;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;


    private static final String[] time_unit = {"day", "hours", "mins", "sec"};

    private static String time_string = " 0 mins";

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private static long lastUpdate = 0, lastUpdateGps = 0, lastUpdateUserCheckIn = 0, lastUpdateForGpsInterval = 0;
    //private static long lastUpdate_forGps = 0;
    float last_x = 0, last_y = 0, last_z = 0;

    //this is user for BRORCASR RECEIVER KEY
    public static final String KEY_USER_STATUS = "user_status";
    public static final String KEY_SENTRYINDIVIDUAL_RESPONSE = "sentryIndividuals_response";


    private static boolean new_start = true;

    public static final String KEY_ACTION_MARKERUPDATE = "ACTION_MARKERUPDATE";

    private PowerManager.WakeLock mWakeLock;

    String movement_speed, movement_direction;

    private static int counter_idle = 0, counter_moving = 0, counter_gps_moving = 0;

    private boolean gps_moving = false;

    private Thread mThread;


    private static int[] gps_moving_circle = {0, 0, 0, 0, 0, 0, 0};
    private static int gps_moving_circle_counter = 0;

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


        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        mWakeLock.acquire();


        //registerCustomReceiver();

        // creating GPS Class object


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        new_start = true;

        locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
        listener = new MyLocationListener();

        LocationProvider gpsProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
        LocationProvider networkProvider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER);
        LocationProvider passiveProvider = locationManager.getProvider(LocationManager.PASSIVE_PROVIDER);

        //Figure out if we have a location somewhere that we can use as a current best location
        if (gpsProvider != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        }

        if (networkProvider != null) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
        }

        if (passiveProvider != null) {
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, listener);
        }


        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
        // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);

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


        mThread = new Thread(new Runnable() {
            boolean abort = false;

            public void run() {
                // TODO Auto-generated method stub
                while (true && !abort) {


                    try {

                        if (saveManager == null) {
                            saveManager = new SaveManager(_context);
                        }

                        final long curTime = System.currentTimeMillis();

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


                        if (!session_id.equals("0") && cd.isConnectingToInternet()) {
                            //Log.d("DEBUG", "user enable");

                            //Log.d("DEBUG", "user connextion ok");
                            if ((isGPSEnabled || isNetworkEnabled) &&
                                    ((curTime - lastUpdateForGpsInterval) >= saveManager.getGpsInterval() * 1000)) {

                                //  Log.d("DEBUG", "user gps ok");
                                lastUpdateForGpsInterval = curTime;

                                String user_lat = saveManager.getUserLat();
                                String user_lang = saveManager.getUserLang();


                                hitUrlForGps(saveManager.getGpsUrl(), session_id, user_lat, user_lang, saveManager.getUserCurrentActivity(), time_string);


                            }

                            //HIT URL for message
                            hitUrlForMessage(saveManager.getUrlEnv() + Constant.URL_UserNewInComingMessage, session_id);


                            //HIT URL user check in
                            if ((curTime - lastUpdateUserCheckIn) >= 30000) {
                                //long diffTime = (curTime - lastUpdate);
                                //diffTime = diffTime / 1000;
                                // Log.d("DEBUG","Yes");
                                lastUpdateUserCheckIn = curTime;
                                hitUrlForUserChekIn(saveManager.getUrlEnv() + Constant.URL_UerCheckIn, session_id, getResources().getString(R.string.app_version));
                            }


                        }

                        // hitUrlForGps("http://dev.ips-systems.com/Sentry/MobileAppUpdateLocation?sessionId=2ifkpgbzklmkygnhmpixcx24&lat=24.912785&lng=91.9535703");


                        //try {
                        //    gps_interval = saveManager.getGpsInterval() * 1000;
                        //} catch (Exception e) {
                        //    gps_interval = 10 * 1000;
                        // }

                        java.util.Date date = new java.util.Date();
                        //System.out.println(new Timestamp(date.getTime()));

                        // Log.d("TIME", String.valueOf(new Timestamp(date.getTime())));

                        Thread.sleep(5000);


                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        abort = true;
                        e.printStackTrace();
                    }

                }

            }
        });
        mThread.start();
    }

    private void hitUrlForGps(String url, final String session_id, final String lat, final String lng, final String user_activity, final String time_string) {
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

                                Intent i = new Intent(getPackageName() + KEY_ACTION_MARKERUPDATE);
                                //response = "{\"statusCodeId\":1,\"sentryIndividuals\":[{\"Lat\" : 24.9174952,\n" +
                                //        "\t\"Lng\":91.9348939,\n" +
                                //        "\t\"Label\":\"abc\"}]}";
                                i.putExtra(KEY_SENTRYINDIVIDUAL_RESPONSE, response);
                              //  Log.d("DEBUG",response);
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

                if (user_activity.equalsIgnoreCase(Constant.USER_ACTIVITY_MOVING) && movement_speed != null && movement_direction != null) {
                    params.put("status", user_activity + ": " + movement_speed + " MPH" + " " + movement_direction + " " + time_string);
                } else if (user_activity.equalsIgnoreCase(Constant.USER_ACTIVITY_MOVING) && movement_speed != null && movement_direction == null) {
                    params.put("status", user_activity + ": " + movement_speed + " MPH" + " " + time_string);
                } else {
                    params.put("status", user_activity + ": " + time_string);
                }
                //Log.d("DEBUG_selected",String.valueOf(selected));
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }

    private void hitUrlForUserChekIn(String url, final String session_id, final String app_version) {
        // TODO Auto-generated method stub
        //Log.d("debug", "before");
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = _context.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int batteryPct = 100;
        if (level != -1 && scale != -1) {
            batteryPct = (int) ((level / (float) scale) * 100f);
        }
        // Log.d("DEBUG_battery",String.valueOf(batteryPct));

        final int finalBatteryPct = batteryPct;

        //Log.d("debug", String.valueOf(finalBatteryPct));

        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //Log.d("debug", "POST");

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    Log.e("Volley", "Error. HTTP Status Code:" + networkResponse.statusCode);
                }

                if (error instanceof TimeoutError) {
                    Log.e("Volley", "TimeoutError");
                } else if (error instanceof NoConnectionError) {
                    Log.e("Volley", "NoConnectionError");
                } else if (error instanceof AuthFailureError) {
                    Log.e("Volley", "AuthFailureError");
                } else if (error instanceof ServerError) {
                    Log.e("Volley", networkResponse.toString());
                } else if (error instanceof NetworkError) {
                    Log.e("Volley", "NetworkError");
                } else if (error instanceof ParseError) {
                    Log.e("Volley", "ParseError");
                }

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                Map<String, String> params = new HashMap<String, String>();
                params.put("sessionId", session_id);
                params.put("appVersion", app_version);
                params.put("batteryLevel", String.valueOf(finalBatteryPct));
                // Log.d("DEBUG_selected",String.valueOf(finalBatteryPct));
                return params;
            }


        };

        req.setRetryPolicy(new DefaultRetryPolicy(60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);

        //Log.d("debug", "past");
    }


    private void hitUrlForMessage(String url, final String session_id) {
        // TODO Auto-generated method stub
        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        //Log.d("DEBUG_size",String.valueOf(Constant.messageList.size()));
                        //String message = "test" + Constant.messageList.size();


                        try {
                            JSONArray jsonArray = new JSONArray(response);


                            //AppConstant.histories.clear();
                            // AppConstant.NUM_OF_UNSEEN_HISTORY = 0;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject tempObject = jsonArray.getJSONObject(i);

                                String message = tempObject.getString("body");
                                int id = tempObject.getInt("id");


                                com.ips_sentry.model.Message messageObj = new com.ips_sentry.model.Message(message, false);
                                messageObj.setId(id);
                                messageObj.setType(Constant.incomingMessage);
                                messageObj.setSent(tempObject.getString("sent"));
                                Constant.messageList.add(messageObj);

                               // saveManager.setMessageObjList(Constant.messageList);
                                //Constant.messageList = saveManager.getMessageObjList();


                                saveManager.setNumOfUnseenMessage(saveManager.getNumOfUnseenMessage() + 1);

                                Constant.isIncomingMessageDuringOnResume = true;


                                Intent fileter = new Intent(_context.getPackageName() + KEY_BROADCAST_FOR_MESSAGE);
                                fileter.putExtra(KEY_MESSAGE, true);
                                LocalBroadcastManager.getInstance(_context).sendBroadcast(fileter);

                            }


                            //Collections.reverse(AppConstant.histories);


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
                // Log.d("DEBUG_selected",String.valueOf(finalBatteryPct));
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

            // Log.d("DEBUG_out",String.valueOf(loc.getLatitude()));
            // Log.d("DEBUG_total_outside", "Location Changed");
            if (isBetterLocation(loc, previousBestLocation)) {

                long curTime = System.currentTimeMillis();


                if ((curTime - lastUpdateGps) >= 1000) {
                    //long diffTime = (curTime - lastUpdate);
                    //diffTime = diffTime / 1000;
                    // Log.d("DEBUG","Yes");
                    lastUpdateGps = curTime;
                    if (previousBestLocation2 != null) {

                        // Log.d("DEBUG_loc",String.valueOf(loc.getLatitude()));
                        // Log.d("DEBUG_prebloc",String.valueOf(previousBestLocation2.getLatitude()));


                        //DecimalFormat df = new DecimalFormat("#.######");
                        double loc_lat = (double) Math.round(loc.getLatitude() * 100000d) / 100000d;
                        double loc_lng = (double) Math.round(loc.getLongitude() * 100000d) / 100000d;
                        loc.setLatitude(loc_lat);
                        loc.setLongitude(loc_lng);
                        //Log.d("DEBUG_lo2",String.valueOf(loc.getLatitude()));

                        float distance = loc.distanceTo(previousBestLocation2);

                        // Log.d("DEBUG_InM",String.valueOf(distance));

                        double distance_inKm = distance / 1000;

                        // Log.d("DEBUG_InKm",String.valueOf(distance_inKm));

                        // time taken (in seconds)
                        float timeTaken = ((loc.getTime() - previousBestLocation2
                                .getTime()) / 1000);

                        // double timeTaken_inHour = (timeTaken / 60) / 60;

                        // calculate speed_mps(mps = METER PER SECOUND but mph = Miles Per Hour)
                        double speed_kph = 0;
                        int speed_mph = 0;
                        if (timeTaken > 0) {
                            //Log.d("DEBUG_time",String.valueOf(timeTaken));
                            //speed_mps = distance / timeTaken;

                            speed_kph = (distance_inKm * 3600) / timeTaken;
                            // Log.d("DEBUG_speed_kph",String.valueOf(speed_kph));

                            // Log.d("DEBUG",String.valueOf(speed_kph / 1.6));

                            speed_mph = (int) (speed_kph / 1.6);
                            //Log.d("DEBUG_speed_mph",String.valueOf(speed_mph));

                            //Log.d("DEBUG_mmh", String.valueOf(speed_mph));
                        }


                        //DecimalFormat df = new DecimalFormat("#.##");
                        //speed_mps = Double.parseDouble(df.format(speed_mps));
                        //speed_mph = Double.parseDouble(df.format(speed_mph));
                        // Log.d("DEBUG_speed", String.valueOf(speed_mph));

                        // Log.d("DEBUG_speed_2", String.valueOf(movement_speed));
                        // Log.d("DEBUG_mps", String.valueOf(df.format(speed_mps)));
                        // Log.d("DEBUG_mmh", String.valueOf(df.format(speed_mph)));
                        // Log.d("DEBUG", ".............................");

                        //info_text.setText("Speed: " + df.format(speed_mps) + " " + "km/h");
                        // info_text_mph.setText("  Speed: " + df.format(speed_mph) + " "
                        //         + "mph");

                        //Log.d("DEBUG",String.valueOf(distance));
                        movement_speed = String.valueOf(speed_mph);
                        // Log.d("DEBUG",String.valueOf(movement_speed));
                        gps_moving = false;


                        if (speed_mph >= 1 && locationManager.getProvider(LocationManager.GPS_PROVIDER).supportsBearing()) {
                            // double degree = previousBestLocation2.bearingTo(loc);
                            // Log.d("DEBUG", String.valueOf(speed_mph));

                            //direction_text.setVisibility(View.VISIBLE);

                            //Log.i(TAG, String.valueOf(degree));
                            // double lat1 = previousBestLocation2.getLatitude();
                            // double lat2 = loc.getLatitude();
                            // double lon1 = previousBestLocation2.getLatitude();
                            // double lon2 = loc.getLongitude();

                            //% convert to radians:
                            // double g2r = Math.PI / 180;
                            // double lat1r = lat1 * g2r;
                            // double lat2r = lat2 * g2r;
                            // double lon1r = lon1 * g2r;
                            //double lon2r = lon2 * g2r;
                            ///double dlonr = lon2r - lon1r;
                            //double y = Math.sin(dlonr) * Math.cos(lat2r);
                            //double x = Math.cos(lat1r) * Math.sin(lat2r) - Math.sin(lat1r) * Math.cos(lat2r) * Math.cos(dlonr);

                            //%compute bearning and convert back to degrees:
                            //double degree = Math.atan2(y, x) / g2r;


                            //Log.d("DeBUG_degree", String.valueOf(degree));

                            //NEW APPROACHES
                            LatLng point1 = new LatLng(previousBestLocation2.getLatitude(), previousBestLocation2.getLongitude());
                            LatLng point2 = new LatLng(loc.getLatitude(), loc.getLongitude());
                            double degree = SphericalUtil.computeHeading(point1, point2);
                            //movement_direction = String.valueOf(degree);


                            // double x1 = previousBestLocation2.getLatitude();
                            // double y1 = previousBestLocation2.getLongitude();
                            // double x2 = loc.getLatitude();
                            // double y2 = loc.getLongitude();
                            // double slope = (y2 - y1) / (x2 - x1);

                            //  if (slope > 2) {
                            //     movement_direction = "N";
                            // } else if (slope < -2) {
                            //     movement_direction = "S";
                            //  } else if (slope > -0.5 && slope <= 0.5 && (x2 - x1) < 0) {
                            //     movement_direction = "E";
                            // } else if (slope > -0.5 && slope <= 0.5 && (x2 - x1) > 0) {
                            //    movement_direction = "W";
                            //}


                            gps_moving_circle = makeShiftToOneDigit(gps_moving_circle, 1);


                            gps_moving = true;

                            movement_direction = "";

                            if (degree >= -22.5 && degree < 22.5) {
                                movement_direction = "N";

                                //direction_text.setText("You are: Northbound");
                            }

                            if (degree >= 22.5 && degree < 67.5) {
                                movement_direction = "NE";
                                /// direction_text.setText("You are: NorthEastbound");
                            }

                            if (degree >= 67.5 && degree < 112.5) {
                                movement_direction = "E";
                                //  direction_text.setText("You are: Eastbound");
                            }

                            if (degree >= 112.5 && degree < 157.5) {
                                movement_direction = "SE";
                                // direction_text.setText("You are: SouthEastbound");
                            }

                            if (degree >= 157.5 || degree < -157.5) {
                                movement_direction = "S";
                                // direction_text.setText("You are: SouthWestbound");
                            }

                            if (degree >= -157.5 && degree < -112.5) {
                                movement_direction = "SW";
                                //direction_text.setText("You are: Westbound");
                            }

                            if (degree >= -112.5 && degree < -67.5) {
                                movement_direction = "W";
                                //direction_text.setText("You are: NorthWestbound");
                            }
                            if (degree >= -67.5 && degree < -22.5) {
                                movement_direction = "NW";
                                //direction_text.setText("You are: NorthWestbound");
                            }

                        } else {
                            gps_moving = false;
                            movement_direction = null;

                            gps_moving_circle = makeShiftToOneDigit(gps_moving_circle, 0);
                        }

                        previousBestLocation2 = loc;

                        gps_moving_circle_counter++;
                        if (gps_moving_circle_counter > 6) gps_moving_circle_counter = 0;

                    } else {
                        // DecimalFormat df = new DecimalFormat("#.######");
                        double loc_lat = (double) Math.round(loc.getLatitude() * 100000d) / 100000d;
                        double loc_lng = (double) Math.round(loc.getLongitude() * 100000d) / 100000d;
                        loc.setLatitude(loc_lat);
                        loc.setLongitude(loc_lng);
                        previousBestLocation2 = loc;
                    }


                    if (numOfZero(gps_moving_circle) >= 4) {
                        calculateUserActivity(Constant.USER_ACTIVITY_IDLE, curTime);
                    } else {
                        saveManager.setGpsInterval(saveManager.getPermanentGpsInterval());
                        calculateUserActivity(Constant.USER_ACTIVITY_MOVING, curTime);
                    }

                }
                previousBestLocation = loc;
                // previousBestLocation = loc;

                saveManager.setUserLat(String.valueOf(loc.getLatitude()));
                saveManager.setUserLang(String.valueOf(loc.getLongitude()));

                //Log.d("DEBUG",String.valueOf(loc.getLatitude()));
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

            // double bearing = loc.getBearing();
            // Log.d("DEBUG",String.valueOf(bearing));


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


    private int numOfZero(int[] value) {
        int counter = 0;
        for (int value1 : value) {
            if (value1 == 0) counter++;
        }

        // Log.d("DEBUG_c",value[0] + " " + value[1] + " " + value[2] + " " + value[3] + " " + value[4] + " " + value[5] + " " + value[6]);
        return counter;
    }

    private int[] makeShiftToOneDigit(int[] value, int needToin) {
        for (int i = 6; i > 0; i--) {
            value[i] = value[i - 1];
        }
        value[0] = needToin;
        return value;

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

                String user_activity = Constant.USER_ACTIVITY_IDLE;
                // if (Math.abs(x - last_x) >= 2 || Math.abs(y - last_y) >= 2 || Math.abs(z - last_z) >= 2) {
                //    Log.d("DEBUG_MOVE", String.valueOf(Math.abs(x - last_x)) + " " + String.valueOf(Math.abs(y - last_y)) + " " + String.valueOf(Math.abs(z - last_z)));
                //    user_activity = Constant.USER_ACTIVITY_MOVING;
                //
                // } else {
                //    user_activity = Constant.USER_ACTIVITY_IDLE;
                // }

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;
                //Log.d("DEBUG_speed", String.valueOf(movement_speed));

                if (new_start) {
                    new_start = false;
                    user_activity = saveManager.getUserCurrentActivity();
                } else if (speed <= 1.5 && !gps_moving) {

                    user_activity = Constant.USER_ACTIVITY_IDLE;

                } else if (gps_moving && speed > 1.5) {
                    counter_idle = 0;
                    user_activity = Constant.USER_ACTIVITY_MOVING;
                }


                // calculateUserActivity(user_activity, curTime);
                //Log.d("onSensorChanged", System.currentTimeMillis() + "=movement_speed=" + movement_speed);

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }


    public void calculateUserActivity(String user_activity, long curTime) {


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


            if ((time_array[0] >= 1 || time_array[1] >= 1 ||
                    time_array[2] >= saveManager.getStoppedThreshold())
                    && saveManager.getUserCurrentActivity().equalsIgnoreCase(Constant.USER_ACTIVITY_IDLE)) {
                time_string = " 0 mins";
                saveManager.setRecordTime(curTime);
                saveManager.setUserCurrentActivity(Constant.USER_ACTIVITY_STOPPED);
                if (saveManager.getGpsInterval() < 60) {
                    //saveManager.setPermanentGpsInterval(saveManager.getGpsInterval());
                    //force gps interval to 60 sec
                    saveManager.setGpsInterval(60);
                }


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
        if (user_activity.equalsIgnoreCase(Constant.USER_ACTIVITY_MOVING)) {


            if (movement_speed != null && movement_direction != null) {
                String temp = saveManager.getUserCurrentActivity() + ": " + movement_speed + " MPH" + " " + movement_direction + " " + time_string;
                i.putExtra(KEY_USER_STATUS, temp);
            } else if (movement_speed != null && movement_direction == null) {
                String temp = saveManager.getUserCurrentActivity() + ": " + movement_speed + " MPH" + " " + time_string;
                i.putExtra(KEY_USER_STATUS, temp);
            } else {
                i.putExtra(KEY_USER_STATUS, saveManager.getUserCurrentActivity() + ": " + time_string);
            }
            //Moving: N 15 MPH


        } else {
            i.putExtra(KEY_USER_STATUS, saveManager.getUserCurrentActivity() + ": " + time_string);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        //Log.v("STOP_SERVICE", "DONE");
        locationManager.removeUpdates(listener);
        senSensorManager.unregisterListener(this);

        mWakeLock.release();

        mThread.interrupt();


        //stopSelf();
    }

    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }
}
