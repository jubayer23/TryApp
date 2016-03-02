package com.ips_sentry.fragment;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.ips_sentry.ips.MainActivity;
import com.ips_sentry.ips.R;

import java.util.Timer;

public class MapFragment extends Fragment {

    private MapView mMapView;
    private GoogleMap mMap;
    private Bundle mBundle;


   // private SensorManager sensorManager;
    //private Sensor sensorLight;

    private boolean flag_traffic;


    //lifeCycle->
    //onCreate->onCreateView->onActivityCreated


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.map, container, false);

        MapsInitializer.initialize(getActivity());

        mMapView = (MapView) inflatedView.findViewById(R.id.map_2);
        mMapView.onCreate(mBundle);
        setUpMapIfNeeded(inflatedView);

        return inflatedView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;

        flag_traffic = getArguments().getBoolean(MainActivity.KEY_TRAFFIC_INFO);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
    }


    private void setUpMapIfNeeded(View inflatedView) {
        if (mMap == null) {
            mMap = ((MapView) inflatedView.findViewById(R.id.map_2)).getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {

        // TODO Auto-generated method stub


        //set Map Properties
        mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(flag_traffic);

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                double latitude = location.getLatitude(); // latitude
                double longitude = location.getLongitude(); // longitude

                mMap.moveCamera(
                        CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }
        });


        // BY THIS YOU CAN CHANGE MAP TYPE
        // mGoogleMap.setMapType(mGoogleMap.MAP_TYPE_SATELLITE);


    }



    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();


        // sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        // if (sensorLight != null) {
        //textLIGHT_available.setText("Sensor.TYPE_LIGHT Available");
        //      sensorManager.registerListener(
        //              LightSensorListener,
        //              sensorLight,
        //              SensorManager.SENSOR_DELAY_NORMAL);

        // } else {
        //     //textLIGHT_available.setText("Sensor.TYPE_LIGHT NOT Available");
        // }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();

        //sensorManager.unregisterListener(LightSensorListener, sensorLight);
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    SensorEventListener LightSensorListener
            = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                //Log.d("DEBUG_light",String.valueOf(event.values[0]));

                if (event.values[0] <= 1) {

                }
            }
        }

    };

}
