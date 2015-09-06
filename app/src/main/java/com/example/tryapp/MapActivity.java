package com.example.tryapp;


import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.appdata.AppController;
import com.example.appdata.SaveManager;
import com.example.mapview.Map2;
import com.example.service.MyService;
import com.example.utils.Constant;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class MapActivity extends Map2 implements LocationListener,
        OnMarkerClickListener, OnInfoWindowClickListener, View.OnClickListener {

    // Location location; // location
    private double latitude; // latitude
    private double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    MarkerOptions markerOptions;

    private Button singOut;
    private ToggleButton toggleButton;
    Calendar cur_cal = Calendar.getInstance();

    private SaveManager saveManager;


    @Override
    protected void startActivity() {
        // TODO Auto-generated method stub

        saveManager = new SaveManager(this);

        init();

        getMap().setMyLocationEnabled(true);


        // BY THIS YOU CAN CHANGE MAP TYPE
        // mGoogleMap.setMapType(mGoogleMap.MAP_TYPE_SATELLITE);

        try {
            // Getting LocationManager object from System Service
            // LOCATION_SERVICE
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            // Getting Current Location From GPS
            Location location = locationManager.getLastKnownLocation(provider);

            if (location != null) {
                onLocationChanged(location);
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            locationManager.requestLocationUpdates(provider,
                    MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            placeMarkerOnMap();

            getMap().setOnInfoWindowClickListener(this);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void init() {
        singOut = (Button) findViewById(R.id.signOut);
        singOut.setOnClickListener(this);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String session_id = saveManager.getSessionToken();
                if (isChecked) {

                    String URL = Constant.URL_TRACKING + "sessionId=" + session_id + "&newStatus=ON";

                    Intent intent = new Intent(getApplicationContext(), MyService.class);
                    PendingIntent pintent = PendingIntent.getService(
                            getApplicationContext(), 0, intent, 0);
                    AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    cur_cal.setTimeInMillis(System.currentTimeMillis());
                    alarm.setRepeating(AlarmManager.RTC_WAKEUP, cur_cal.getTimeInMillis(),
                            50 * 100, pintent);

                    hitUrl(URL);
                } else {
                    String URL = Constant.URL_TRACKING + "sessionId=" + session_id + "&newStatus=OFF";
                    Intent intent = new Intent(getApplicationContext(), MyService.class);
                    PendingIntent pintent = PendingIntent.getService(
                            getApplicationContext(), 0, intent, 0);
                    AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarm.cancel(pintent);
                    hitUrl(URL);
                }
            }
        });

    }

    private void hitUrl(String url) {


        Log.d("DEBUG_Map",url);

        final JsonObjectRequest jsObjRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        String textResult = response.toString();

                       // Log.d("DEBUG_TRACKING", textResult);
                        try {
                            boolean status = response.getBoolean("status");
                            int id;
                            if (status) {
                                // id = response.getInt("status");
                                Log.d("TRACKING", String.valueOf(status));

                            }
                            else
                            {
                                Log.d("TRACKING", String.valueOf(status));
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

        AppController.getInstance().addToRequestQueue(jsObjRequest);
    }

    //sone changes

    private void placeMarkerOnMap() {
        // check for null in case it is null


        getMap().moveCamera(
                CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
        getMap().animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public boolean onMarkerClick(Marker arg0) {
        // TODO Auto-generated method stub

        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub


        double latitude = location.getLatitude(); // latitude
        double longitude = location.getLongitude(); // longitude

        getMap().moveCamera(
                CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
        getMap().animateCamera(CameraUpdateFactory.zoomTo(15));

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        // TODO Auto-generated method stub
        String title = marker.getTitle();

        AlertDialog.Builder alert = new AlertDialog.Builder(MapActivity.this);
        alert.setTitle(title);

        alert.setMessage("Do you Want see Direction");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="
                        + String.valueOf(marker.getPosition().latitude) + ","
                        + String.valueOf(marker.getPosition().longitude));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        alert.show();

    }

    @Override
    public void onClick(View v) {


        int id = v.getId();

        if (id == R.id.signOut) {
            deleteData();

            disableService();

            finish();
        }

    }

    private void disableService()
    {
        Intent intent = new Intent(getApplicationContext(), MyService.class);
        PendingIntent pintent = PendingIntent.getService(
                getApplicationContext(), 0, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pintent);
    }

    private void deleteData() {

        saveManager.setSessionToken("0");

    }
}
