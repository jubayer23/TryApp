package com.ips_sentry.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.ips_sentry.Adapter.RouteSelectedListAdapter;
import com.ips_sentry.appdata.AppController;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.ips.R;
import com.ips_sentry.model.Route;
import com.ips_sentry.utils.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminFragment extends Fragment {

    private Button btn_one_minute_eta, btn_three_minute_eta;


    private ListView listView;
    private RouteSelectedListAdapter routeSelectedListAdapter;


    private List<Route> routes;
    private Gson gson;
    private ProgressBar progressBar;

    private SaveManager saveManager;

    // Progress Dialog
    private ProgressDialog pDialog;

    private static final int ONE_MIN_ETA_SEC = 60;
    private static final int THREE_MIN_ETA_SEC = 180;

    private boolean SKIP_ONRESUME = false;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.show_admin, container,
                false);

    }

    public void onActivityCreated(Bundle SavedInstanceState) {
        super.onActivityCreated(SavedInstanceState);

        SKIP_ONRESUME = true;

        init();

        routeSelectedListAdapter = new RouteSelectedListAdapter(this.getActivity(), routes);

        listView.setAdapter(routeSelectedListAdapter);



        RouteSelectedListAdapter.selectedId = "";
        sendRequestToServerForFetchingRoutes();


        btn_one_minute_eta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!RouteSelectedListAdapter.selectedId.isEmpty()) {
                    pDialog.show();

                    hitUrlForEta(saveManager.getUrlEnv() +
                                    Constant.URL_ARRIVAL_NOTIFICATIONS,
                            saveManager.getSessionToken(),
                            RouteSelectedListAdapter.selectedId,
                            ONE_MIN_ETA_SEC);
                } else {
                    Toast.makeText(getActivity(), "Please Select A Route", Toast.LENGTH_LONG).show();
                }


            }


        });

        btn_three_minute_eta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pDialog.show();

                if (!RouteSelectedListAdapter.selectedId.isEmpty()) {
                    pDialog.show();

                    hitUrlForEta(saveManager.getUrlEnv() +
                                    Constant.URL_ARRIVAL_NOTIFICATIONS,
                            saveManager.getSessionToken(),
                            RouteSelectedListAdapter.selectedId,
                            THREE_MIN_ETA_SEC);
                } else {
                    Toast.makeText(getActivity(), "Please Select A Route", Toast.LENGTH_LONG).show();
                }
            }


        });

    }


    @Override
    public void onResume() {
        super.onResume();
       // Log.d("DEBUG",String.valueOf(SKIP_ONRESUME));
        if (SKIP_ONRESUME) {
            SKIP_ONRESUME = false;
        } else {

            RouteSelectedListAdapter.selectedId = "";

            sendRequestToServerForFetchingRoutes();
        }

    }

    private void init() {

        saveManager = new SaveManager(getActivity());

        gson = new Gson();
        routes = new ArrayList<Route>();

        progressBar = (ProgressBar) getActivity().findViewById(R.id.loadingProgterm_admin);
        progressBar.setVisibility(View.INVISIBLE);

        listView = (ListView) getActivity().findViewById(R.id.list_admin);

        btn_one_minute_eta = (Button) getActivity().findViewById(R.id.btn_one_minute_eta);
        btn_three_minute_eta = (Button) getActivity().findViewById(R.id.btn_three_minute_eta);

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Sending Your Response.... Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
    }


    private void sendRequestToServerForFetchingRoutes() {

        progressBar.setVisibility(View.VISIBLE);

        String url = saveManager.getUrlEnv() + Constant.URL_SHOW_ROUTES;

        //Log.d("DEBUG",url);

        StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //Log.d("HEATMAP","YES");
                        try {
                            parseJsonFeed(new JSONArray(response));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (routes.size() == 1) {
                            RouteSelectedListAdapter.alreadySelectedId = "";
                            RouteSelectedListAdapter.selectedId = routes.get(0).getRouteId();
                        }
                        routeSelectedListAdapter.notifyDataSetChanged();


                        if (progressBar.getVisibility() == View.VISIBLE)
                            progressBar.setVisibility(View.INVISIBLE);


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (progressBar.getVisibility() == View.VISIBLE)
                    progressBar.setVisibility(View.INVISIBLE);

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("sessionId", saveManager.getSessionToken());

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(req);

    }

    private void parseJsonFeed(JSONArray response) {

        routes.clear();
        for (int i = 0; i < response.length(); i++) {

            JSONObject tempObject = null;
            try {
                tempObject = response.getJSONObject(i);

                Route route = gson.fromJson(tempObject.toString(), Route.class);

                if (route.isSelected()) {
                    routes.add(route);
                }

                //route.setColor(Route.WHITE);


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }


    }


    private void hitUrlForEta(String url, final String sessionToken, final String routeId, final int sec) {


        // TODO Auto-generated method stub
        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (pDialog.isShowing()) pDialog.dismiss();

                        showDialog(1);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (pDialog.isShowing()) pDialog.dismiss();


                showDialog(0);

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                Map<String, String> params = new HashMap<String, String>();
                params.put("sessionId", sessionToken);
                params.put("routeId", routeId);
                params.put("seconds", String.valueOf(sec));
                // Log.d("DEBUG_selected",String.valueOf(finalBatteryPct));
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }

    private void showDialog(int code) {
        final Dialog dialog_start = new Dialog(getActivity(),
                android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog_start.setCancelable(true);
        switch (code) {
            case 1:
                dialog_start.setContentView(R.layout.dialog_success);
                dialog_start.show();
                break;
            case 0:
                dialog_start.setContentView(R.layout.dialog_error);
                dialog_start.show();
                break;
        }
        new CountDownTimer(2000, 1000) {

            public void onTick(long millisUntilFinished) {
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                dialog_start.dismiss();
            }

        }.start();
    }
}
