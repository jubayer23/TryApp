package com.ips_sentry;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.model.people.Person;
import com.google.gson.Gson;
import com.ips_sentry.Adapter.PlaceListAdapter;
import com.ips_sentry.appdata.AppController;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.dialog.AlertDialogManager;
import com.ips_sentry.ips.R;
import com.ips_sentry.map.BaseMapActivity;
import com.ips_sentry.model.Venue;
import com.ips_sentry.utils.Constant;
import com.ips_sentry.utils.LastLocationOnly;

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

/**
 * Created by comsol on 25-Aug-16.
 */
public class NearByVenues2 extends BaseMapActivity {


    private static final int delay = 100; // delay for 5 sec.
    private static final int period = 5000; // repeat every 5 secs.

    private Gson gson;

    private ArrayList<Venue> venues;

    private LastLocationOnly gps;

    private ProgressDialog pDialog;

    private SaveManager saveManager;


    AlertDialogManager alert = new AlertDialogManager();

    private List<Bitmap> logos;

   private List<Marker> markers;

    private double venue_first_lat = 0.0, venue_first_lng = 0.0;

    @Override
    protected void startDemo() {

        init();

        getMap().setMyLocationEnabled(true);

        getMap().getUiSettings().setZoomControlsEnabled(true);

        // LatLng myLocation = new LatLng(gps.getLatitude(),
        //        gps.getLongitude());
        // getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
        //         15));

        hitUrl(saveManager.getUrlEnv() + Constant.URL_NEARBYPLACE, String.valueOf(gps.getLatitude()), String.valueOf(gps.getLongitude()));


    }

    private void init() {

        gson = new Gson();

        venues = new ArrayList<>();

        gps = new LastLocationOnly(this);

        saveManager = new SaveManager(this);

        logos = new ArrayList<>();

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading.... Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        //pDialog.show();
    }


    private void hitUrl(String url, final String lat, final String lng) {
        // TODO Auto-generated method stub


        pDialog.show();

        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        //Log.d("DEBUG", response);

                        try {

                            JSONObject jsonObject = new JSONObject(response);

                            String status = jsonObject.getString("status");

                            if (status.equalsIgnoreCase("OK")) {

                                JSONArray jsonArray = jsonObject.getJSONArray("venues");

                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject tempObject = null;
                                    try {
                                        tempObject = jsonArray.getJSONObject(i);


                                        Venue venue = gson.fromJson(tempObject.toString(), Venue.class);
                                        venues.add(venue);

                                        if (i == 0) {
                                            venue_first_lat = venue.getLatitude();
                                            venue_first_lng = venue.getLongitude();
                                        }

                                        //route.setColor(Route.WHITE);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }


                                }


                                new DownloadFilesTask().execute();


                            } else {

                                //Log.d("NEARBYPLACE", "NOT FOUND");
                                pDialog.dismiss();


                                alert.showAlertDialog(NearByVenues2.this, "NO VENUES AVAILABLE", "Sorry, we do not currently have any venues nearby.", false);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            pDialog.dismiss();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX//(33.817831, -118.298076)
                Map<String, String> params = new HashMap<String, String>();
                params.put("lat", lat);
                params.put("lng", lng);

                // params.put("lat", "33.817831");
                // params.put("lng", "-118.298076");


                //Log.d("DEBUG_selected",String.valueOf(selected));
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }


    private class DownloadFilesTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... src) {

            Bitmap myBitmap;
            try {

                for (int i = 0; i < venues.size(); i++) {
                    Venue venue = venues.get(i);

                    if (!venue.getLogo().isEmpty()) {
                        java.net.URL url = new java.net.URL(venue.getLogo());
                        HttpURLConnection connection = (HttpURLConnection) url
                                .openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        myBitmap = BitmapFactory.decodeStream(input);

                    } else {
                        myBitmap = BitmapFactory.decodeResource(NearByVenues2.this.getResources(),
                                R.drawable.marker_red_pin);
                    }
                    if (myBitmap != null) {
                        logos.add(myBitmap);
                    } else {
                        myBitmap = BitmapFactory.decodeResource(NearByVenues2.this.getResources(),
                                R.drawable.marker_red_pin);
                        logos.add(myBitmap);
                    }

                    // myBitmap = getResizedBitmap(myBitmap, 80, 80);

                }


                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(Void result) {
            // showDialog("Downloaded " + result + " bytes");

            markers = new ArrayList<>();

            for (int i = 0; i < venues.size(); i++) {
                Venue venue = venues.get(i);
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(venue.getLatitude(),
                        venue.getLongitude())).title(venue.getVenueName());

                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getResizedBitmap(logos.get(i), 80, 80)));
                markerOptions.snippet(venue.getVenueAddress());

                Marker marker = getMap().addMarker(markerOptions);
                markers.add(marker);
            }


            getMap().setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                public View getInfoWindow(Marker marker) {
                    return null;
                }

                public View getInfoContents(Marker marker) {


                    View v = getLayoutInflater().inflate(R.layout.custom_infowindow, null);
                    TextView tv_title = (TextView) v.findViewById(R.id.tv_title);
                    TextView tv_info = (TextView) v.findViewById(R.id.tv_info);
                    ImageView img = (ImageView) v.findViewById(R.id.icon);
                    tv_title.setText(marker.getTitle());
                    tv_info.setText(marker.getSnippet());
                    img.setImageBitmap(logos.get(markers.indexOf(marker)));
                    return v;

                }
            });

            pDialog.dismiss();


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

                            //This mean that there is no venues nearby user so we need to show a alert
                            alert.showAlertDialog(NearByVenues2.this, "NO VENUES AVAILABLE", "Sorry, we do not currently have any venues nearby.", false);


                        }
                    } else {
                        if (venue_first_lat != 0.0 && venue_first_lng != 0.0) {
                            // placeMarkerForSentryIndividual(venue_first_lat, venue_first_lng, "");
                            LatLng myLocation = new LatLng(venue_first_lat,
                                    venue_first_lng);
                            getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                                    15));
                        } else {

                            //Log.d("NEARBYPLACE", "NOT FOUND");
                            pDialog.dismiss();


                            alert.showAlertDialog(NearByVenues2.this, "NO VENUES AVAILABLE", "Sorry, we do not currently have any venues nearby.", false);

                        }
                    }
                }
            });

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
