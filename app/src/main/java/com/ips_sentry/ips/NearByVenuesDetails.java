package com.ips_sentry.ips;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.ips_sentry.appdata.AppController;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.map.BaseMapActivity;
import com.ips_sentry.model.SentryIndividual;
import com.ips_sentry.utils.GPSTracker;
import com.ips_sentry.utils.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by comsol on 26-Mar-16.
 */
public class NearByVenuesDetails extends BaseMapActivity {
    private static int siteId = 0;

    private List<SentryIndividual> sentryIndividuals;

    private Gson gson;

    private static final int delay = 100; // delay for 5 sec.
    private static final int period = 5000; // repeat every 5 secs.

    private SaveManager saveManager;

    private GPSTracker gps;

    private double venue_first_lat = 0.0, venue_first_lng = 0.0;


    private Bitmap logo_bitmap;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras.containsKey(NearByVenues.KEY_SITEID)) {
            siteId = extras.getInt(NearByVenues.KEY_SITEID);
            venue_first_lat = extras.getDouble(NearByVenues.KEY_VENUE_POSITION_LAT);
            venue_first_lng = extras.getDouble(NearByVenues.KEY_VENUE_POSITION_LNG);
            String logo_url = extras.getString(NearByVenues.KEY_VENUE_LOGO);
            if (!logo_url.isEmpty())
                new DownloadFilesTask().execute(logo_url);

            //Log.d("DEBUG_inInternt", "Yes");
            // setUpMapIfNeeded();
        }
    }

    @Override
    protected void startDemo() {

        init();

        getMap().setMyLocationEnabled(true);

        getMap().setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if (gps.canGetLocation()) {
                    if (venue_first_lat != 0.0 && venue_first_lng != 0.0) {
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(new LatLng(venue_first_lat, venue_first_lng));
                        builder.include(new LatLng(gps.getLatitude(), gps.getLongitude()));
                        LatLngBounds bounds = builder.build();
                        int padding = 120; // offset from edges of the map in pixels
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                        //Log.d("DEBUG_1stLat",venue_first_lat + " " + venue_first_lng);
                        // placeMarkerForSentryIndividual(venue_first_lat, venue_first_lng, "");
                        getMap().animateCamera(cu);

                    } else {
                        LatLng myLocation = new LatLng(gps.getLatitude(),
                                gps.getLongitude());
                        getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                                15));

                    }
                } else {
                    if (venue_first_lat != 0.0 && venue_first_lng != 0.0) {
                        // placeMarkerForSentryIndividual(venue_first_lat, venue_first_lng, "");
                        LatLng myLocation = new LatLng(venue_first_lat,
                                venue_first_lng);
                        getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                                15));
                    }
                }
            }
        });


        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {

            public void run() {


                hitUrl(saveManager.getUrlEnv() + Constant.URL_SITE_INDIVIDUALS, siteId);

            }

        }, delay, period);


    }

    private void init() {
        gson = new Gson();
        sentryIndividuals = new ArrayList<>();
        saveManager = new SaveManager(this);
        gps = new GPSTracker(this);
    }


    private void hitUrl(String url, final int siteId) {
        // TODO Auto-generated method stub


        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //Log.d("DEBUG_venue_details_response",response);


                        try {
                            JSONObject jsonObject = new JSONObject(response);

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


                                getMap().clear();

                                for (int i = 0; i < sentryIndividuals.size(); i++) {


                                    placeMarkerForSentryIndividual(sentryIndividuals.get(i).getLat(), sentryIndividuals.get(i).getLng(), sentryIndividuals.get(i).getLabel());
                                }
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
                params.put("siteId", String.valueOf(siteId));
                //Log.d("DEBUG_selected",String.valueOf(selected));
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }

    private void placeMarkerForSentryIndividual(double lat, double lng, String title) {

        // Log.d("DEBUG_2nd",lat + " " +lng);
        if (logo_bitmap == null) {
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(lat, lng)).title(title);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_red_pin));
            getMap().addMarker(markerOptions);
        } else {
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(lat, lng)).title(title);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(logo_bitmap));
            getMap().addMarker(markerOptions);
        }


    }

    private class DownloadFilesTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... src) {
            try {
                java.net.URL url = new java.net.URL(src[0]);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                myBitmap = getResizedBitmap(myBitmap, 80, 80);


                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(Bitmap bitmap) {
            // showDialog("Downloaded " + result + " bytes");
            logo_bitmap = bitmap;

            if (venue_first_lat != 0.0 && venue_first_lng != 0.0) {
              placeMarkerForSentryIndividual(venue_first_lat,venue_first_lng,"");
            }

        }
    }


    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);

        return resizedBitmap;
    }
}
