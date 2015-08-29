package com.example.tryapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import com.example.tryapp.R;

import com.example.mapview.Map2;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Map2 implements LocationListener,
        OnMarkerClickListener, OnInfoWindowClickListener {

    // Location location; // location
    private double latitude; // latitude
    private double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 ; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    MarkerOptions markerOptions;

    @Override
    protected void startDemo() {
        // TODO Auto-generated method stub

        getMap().setMyLocationEnabled(true);

        // BY THIS YOU CAN CHANGE MAP TYPE
        // mGoogleMap.setMapType(mGoogleMap.MAP_TYPE_SATELLITE);

        try {
            // Getting LocationManager object from System Service
            // LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

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
        double longitude =location.getLongitude(); // longitude

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

}
