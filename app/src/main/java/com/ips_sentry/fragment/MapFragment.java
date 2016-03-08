package com.ips_sentry.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.maps.android.ui.IconGenerator;
import com.ips_sentry.ips.MainActivity;
import com.ips_sentry.ips.R;
import com.ips_sentry.model.SentryIndividual;
import com.ips_sentry.service.MyServiceUpdate;
import com.ips_sentry.setting.GPSTracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private MapView mMapView;
    private GoogleMap mMap;
    private Bundle mBundle;

    private boolean flag_traffic, flag_individual_label;

    private BroadcastReceiver receiver;

    private Gson gson;

    private List<SentryIndividual> sentryIndividuals;

    private int myLocationButtonToggle = 0;

    private ImageView myLocationButton;

    //lifeCycle->
    //onCreate->onCreateView->onActivityCreated


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.map, container, false);

        MapsInitializer.initialize(getActivity());

        mMapView = (MapView) inflatedView.findViewById(R.id.map_2);
        myLocationButton = (ImageView) inflatedView.findViewById(R.id.myLocationButton);
        mMapView.onCreate(mBundle);
        setUpMapIfNeeded(inflatedView);

        return inflatedView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;

        flag_traffic = getArguments().getBoolean(MainActivity.KEY_TRAFFIC_INFO);
        flag_individual_label = getArguments().getBoolean(MainActivity.KEY_SHOWINDIVIDUAL_LABEL);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        gson = new Gson();

        sentryIndividuals = new ArrayList<>();

        registerCustomReceiver();

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
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {

                if (myLocationButtonToggle == 2) return;

                double latitude = location.getLatitude(); // latitude
                double longitude = location.getLongitude(); // longitude

                CameraPosition cameraPosition = new CameraPosition.Builder().
                        target(new LatLng(latitude, longitude)).
                        tilt(0).
                        zoom(15).
                        bearing(0).
                        build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myLocationButtonToggle < 2) {

                    myLocationButton.setImageResource(R.drawable.mylocation_icon);

                    myLocationButtonToggle = 2;
                } else {
                    GPSTracker gps = new GPSTracker(getActivity());
                    if (gps.canGetLocation()) {
                        CameraPosition cameraPosition = new CameraPosition.Builder().
                                target(new LatLng(gps.getLatitude(), gps.getLongitude())).
                                tilt(0).
                                zoom(15).
                                bearing(0).
                                build();

                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                    myLocationButton.setImageResource(R.drawable.mylocation_icon_selected);
                    myLocationButtonToggle = 1;
                }
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (myLocationButtonToggle < 2) {

                    myLocationButton.setImageResource(R.drawable.mylocation_icon);

                    myLocationButtonToggle = 2;
                }
            }
        });


        // GPSTracker gps = new GPSTracker(getActivity());
        // if(gps.canGetLocation())
        //{
        //    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
        //                    new LatLng(gps.getLatitude(), gps.getLongitude()), 15));
        // }


    }

    private void registerCustomReceiver() {
        //Broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String res = intent.getStringExtra(MyServiceUpdate.KEY_SENTRYINDIVIDUAL_RESPONSE);

                // Log.d("DEBUG_RES",res);

                //{"statusCodeId":1,"sentryIndividuals":[{"Lat":33.817827,"Lng":-118.298066,"Label":"bkindividual3"}]}
                try {
                    JSONObject jsonObject = new JSONObject(res);

                    JSONArray jsonArray = jsonObject.getJSONArray("sentryIndividuals");

                    sentryIndividuals.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject tempObject = null;
                        try {
                            tempObject = jsonArray.getJSONObject(i);

                            SentryIndividual sentryIndividual = gson.fromJson(tempObject.toString(), SentryIndividual.class);
                            sentryIndividuals.add(sentryIndividual);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    if (!sentryIndividuals.isEmpty()) {
                        placeMarkerForSentryIndividual();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(getActivity().getPackageName() + MyServiceUpdate.KEY_MARKERUPDATE);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(receiver, filter);
    }

    private void placeMarkerForSentryIndividual() {

        mMap.clear();

        for (int i = 0; i < sentryIndividuals.size(); i++) {


            if (flag_individual_label) {
                IconGenerator iconFactory = new IconGenerator(getActivity());
                iconFactory.setColor(Color.BLUE);
                MarkerOptions markerOptions = new MarkerOptions().
                        icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(sentryIndividuals.get(i).getLabel()))).
                        position(new LatLng(sentryIndividuals.get(i).getLat(), sentryIndividuals.get(i).getLng())).
                        anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

                mMap.addMarker(markerOptions);

            } else {
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(sentryIndividuals.get(i).getLat(), sentryIndividuals.get(i).getLng())).title(sentryIndividuals.get(i).getLabel());
               mMap.addMarker(markerOptions);
            }


        }


    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();

    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(receiver);
        super.onDestroy();
    }


}
