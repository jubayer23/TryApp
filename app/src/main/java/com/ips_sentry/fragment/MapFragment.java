package com.ips_sentry.fragment;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.ips_sentry.ips.R;

import java.util.Timer;

public class MapFragment extends Fragment implements LocationListener {

	private MapView mMapView;
	private GoogleMap mMap;
	private Bundle mBundle;

	// Location location; // location
	private double latitude; // latitude
	private double longitude; // longitude

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000; // 1 minute

	private Timer timer;

	// Declaring a Location Manager
	protected LocationManager locationManager;

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




		mMap.setMyLocationEnabled(true);


		// BY THIS YOU CAN CHANGE MAP TYPE
		// mGoogleMap.setMapType(mGoogleMap.MAP_TYPE_SATELLITE);

		try {
			// Getting LocationManager object from System Service
			// LOCATION_SERVICE
			locationManager = (LocationManager) this.getActivity().getSystemService(this.getActivity().LOCATION_SERVICE);

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


		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	private void placeMarkerOnMap() {
		// check for null in case it is null


		mMap.moveCamera(
				CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
		mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub


		double latitude = location.getLatitude(); // latitude
		double longitude = location.getLongitude(); // longitude

		mMap.moveCamera(
				CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
		mMap.animateCamera(CameraUpdateFactory.zoomTo(15));


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
		super.onDestroy();
	}

}
