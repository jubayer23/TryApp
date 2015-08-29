package com.example.mapview;


import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.example.tryapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by comsol on 8/28/2015.
 */
public abstract class Map2 extends FragmentActivity {

    /**
     * Note that this may be null if the Google Play services APK is not
     * available.
     */
    private GoogleMap mMap;

    double mLatitude = 0;
    double mLongitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map2);

        // Getting Google Play availability status
        int status = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getBaseContext());

        if (status != ConnectionResult.SUCCESS) { // Google Play Services are
            // not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
                    requestCode);
            dialog.show();

        } else {
            // Google Play Services are available
            setUpMapIfNeeded();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @SuppressWarnings("static-access")
    private void setUpMapIfNeeded() {
        if (mMap != null) {
            return;
        }

        GoogleMapOptions options = new GoogleMapOptions();

        options.mapType(GoogleMap.MAP_TYPE_NORMAL)
                .compassEnabled(true)
                .rotateGesturesEnabled(false)
                .tiltGesturesEnabled(false);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_2);
        mapFragment.newInstance(options);
        mMap = mapFragment.getMap();
        if (mMap != null) {
            startDemo();
        }
    }

    /**
     * Run the demo-specific code.
     */
    protected abstract void startDemo();

    protected GoogleMap getMap() {
        setUpMapIfNeeded();
        return mMap;
    }

}
