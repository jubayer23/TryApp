package com.ips_sentry.ips;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.ips_sentry.appdata.GsonRequest;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.dialog.AlertDialogManager;
import com.ips_sentry.model.Route;
import com.ips_sentry.model.Venue;
import com.ips_sentry.model.Venues;
import com.ips_sentry.setting.GPSTracker;
import com.ips_sentry.utils.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by comsol on 9/10/2015.
 */
public class NearByVenues extends Activity implements View.OnClickListener {

    private LinearLayout progressbar;

    private SaveManager saveManager;

    private GPSTracker gps;


    private Button btnSignIn;

    private ListView listView;

    private TextView tvNoVenues;

    PlaceListAdapter placeListAdapter;

    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();


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
        btnSignIn.setVisibility(View.INVISIBLE);


        hitUrl(saveManager.getGpsUrlEnv() + Constant.URL_NEARBYPLACE,String.valueOf(gps.getLatitude()),String.valueOf(gps.getLongitude()));

        gps.stopUsingGPS();
    }

    private void init() {

        gson = new Gson();

        venues = new ArrayList<Venue>();

        progressbar = (LinearLayout) findViewById(R.id.loadingPanel);

        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(this);

        tvNoVenues = (TextView) findViewById(R.id.tvNoVenues);

        listView = (ListView) findViewById(R.id.place_list);

    }



    private void hitUrl(String url,final String lat,final String lng) {
        // TODO Auto-generated method stub


        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("DEBUG",response);

                        progressbar.setVisibility(View.INVISIBLE);
                        btnSignIn.setVisibility(View.VISIBLE);

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
                btnSignIn.setVisibility(View.VISIBLE);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                Map<String, String> params = new HashMap<String, String>();
                params.put("lat", lat);
                params.put("lng", lng);
                //Log.d("DEBUG_selected",String.valueOf(selected));
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }

    private void parseJsonFeed(JSONObject response) {

        try {
            String status = response.getString("status");

            if(status.equalsIgnoreCase("OK"))
            {

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


            }else
            {

                Log.d("NEARBYPLACE", "NOT FOUND");
                btnSignIn.setVisibility(View.VISIBLE);
                progressbar.setVisibility(View.GONE);

                listView.setVisibility(View.GONE);
                tvNoVenues.setVisibility(View.VISIBLE);

                alert.showAlertDialog(NearByVenues.this,"NO VENUES AVAILABLE","Sorry, we do not currently have any venues nearby.",false);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }




    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.btnSignIn) {
                finish();
        }

    }
}
