package com.ips_sentry;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.ips_sentry.Adapter.PlaceListAdapter;
import com.ips_sentry.appdata.AppController;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.dialog.AlertDialogManager;
import com.ips_sentry.ips.R;
import com.ips_sentry.model.Venue;
import com.ips_sentry.utils.GPSTracker;
import com.ips_sentry.utils.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by comsol on 9/10/2015.
 */
public class NearByVenues extends AppCompatActivity {

    private LinearLayout progressbar;

    private SaveManager saveManager;

    private GPSTracker gps;


    private ListView listView;

    private TextView tvNoVenues;

    PlaceListAdapter placeListAdapter;

    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

    public static final String KEY_SITEID = "siteId";
    public static final String KEY_VENUE_POSITION_LAT = "venue_position_lat";
    public static final String KEY_VENUE_POSITION_LNG = "venue_position_lng";
    public static final String KEY_VENUE_LOGO = "venue_logo";

    Gson gson;

    ArrayList<Venue> venues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearby_venues);


        saveManager = new SaveManager(this);

        gps = new GPSTracker(this);

        init();

        progressbar.setVisibility(View.VISIBLE);


        hitUrl(saveManager.getUrlEnv() + Constant.URL_NEARBYPLACE, String.valueOf(gps.getLatitude()), String.valueOf(gps.getLongitude()));

        gps.stopUsingGPS();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(NearByVenues.this, NearByVenuesDetails.class);
                if (venues.get(position).getLatitude() != null) {
                    intent.putExtra(KEY_VENUE_POSITION_LAT, venues.get(position).getLatitude());
                    intent.putExtra(KEY_VENUE_POSITION_LNG, venues.get(position).getLongitude());
                } else {
                    intent.putExtra(KEY_VENUE_POSITION_LAT, 0.0);
                    intent.putExtra(KEY_VENUE_POSITION_LNG, 0.0);
                }
                intent.putExtra(KEY_VENUE_LOGO, venues.get(position).getLogo());
                intent.putExtra(KEY_SITEID, venues.get(position).getSiteId());
                startActivity(intent);

            }
        });
    }

    private void init() {

        gson = new Gson();

        venues = new ArrayList<Venue>();

        progressbar = (LinearLayout) findViewById(R.id.loadingPanel);


        tvNoVenues = (TextView) findViewById(R.id.tvNoVenues);

        listView = (ListView) findViewById(R.id.place_list);

    }


    private void hitUrl(String url, final String lat, final String lng) {
        // TODO Auto-generated method stub


        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("DEBUG", response);

                        progressbar.setVisibility(View.INVISIBLE);

                        try {
                            parseJsonFeed(new JSONObject(response));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressbar.setVisibility(View.INVISIBLE);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX//(33.817831, -118.298076)
                Map<String, String> params = new HashMap<String, String>();
                //params.put("lat", lat);
               // params.put("lng", lng);

                params.put("lat", "33.817831");
                params.put("lng", "-118.298076");


                //Log.d("DEBUG_selected",String.valueOf(selected));
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }

    private void parseJsonFeed(JSONObject response) {

        try {
            String status = response.getString("status");

            if (status.equalsIgnoreCase("OK")) {

                JSONArray jsonArray = response.getJSONArray("venues");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject tempObject = null;
                    try {
                        tempObject = jsonArray.getJSONObject(i);


                        Venue venue = gson.fromJson(tempObject.toString(), Venue.class);
                        venues.add(venue);

                        //route.setColor(Route.WHITE);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }

                if (placeListAdapter == null) {
                    placeListAdapter = new PlaceListAdapter(
                            NearByVenues.this, venues);
                    listView.setAdapter(placeListAdapter);
                } else {
                    placeListAdapter.addMore(venues);
                }


            } else {

                //Log.d("NEARBYPLACE", "NOT FOUND");
                progressbar.setVisibility(View.GONE);

                listView.setVisibility(View.GONE);
                tvNoVenues.setVisibility(View.VISIBLE);

                alert.showAlertDialog(NearByVenues.this, "NO VENUES AVAILABLE", "Sorry, we do not currently have any venues nearby.", false);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

}
