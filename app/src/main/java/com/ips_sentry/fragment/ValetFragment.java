package com.ips_sentry.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.drive.DriveId;
import com.google.gson.Gson;
import com.ips_sentry.Adapter.RouteListAdapter;
import com.ips_sentry.Adapter.RouteSelectedListAdapter;
import com.ips_sentry.appdata.AppController;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.ips.R;
import com.ips_sentry.model.Route;
import com.ips_sentry.service.MyServiceUpdate;
import com.ips_sentry.utils.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValetFragment extends Fragment implements View.OnClickListener {

    public static final String KEY_BROADCAST = "broadcastForEditText";
    private ListView listView;
    private RouteSelectedListAdapter routeSelectedListAdapter;


    private List<Route> routes;
    private Gson gson;
    private ProgressBar progressBar;

    private SaveManager saveManager;

    private EditText ed_numerical_field;

    private BroadcastReceiver receiver;

    private Button btn_submit, btn_cancel,btn1,btn2,btn3,btn4,btn5,btn6,btn7,btn8,btn9,btn0,btn_erase;


    private boolean SKIP_ONRESUME = false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.show_valet_2, container,
                false);

    }

    public void onActivityCreated(Bundle SavedInstanceState) {
        super.onActivityCreated(SavedInstanceState);

        if (SavedInstanceState == null) {
            SKIP_ONRESUME = true;

            init();

            registerCustomReceiver();

            routeSelectedListAdapter = new RouteSelectedListAdapter(this.getActivity(), routes);

            listView.setAdapter(routeSelectedListAdapter);

            RouteSelectedListAdapter.selectedId = "";

            sendRequestToServer();

        } else {

        }


    }

    @Override
    public void onResume() {
        super.onResume();
        if (SKIP_ONRESUME) {
            SKIP_ONRESUME = false;
        } else {

            RouteSelectedListAdapter.selectedId = "";

            sendRequestToServer();
        }

    }


    private void init() {

        gson = new Gson();
        routes = new ArrayList<Route>();
        saveManager = new SaveManager(this.getActivity());

        progressBar = (ProgressBar) getActivity().findViewById(R.id.loadingProgterm_2);
        progressBar.setVisibility(View.INVISIBLE);

        listView = (ListView) getActivity().findViewById(R.id.list_2);

        ed_numerical_field = (EditText) getActivity().findViewById(R.id.ed_numerical_field);
        ed_numerical_field.setInputType(EditorInfo.TYPE_NULL);
        //ed_numerical_field.setRawInputType(Configuration.KEYBOARD_12KEY);

        btn_submit = (Button) getActivity().findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);
        btn_cancel = (Button) getActivity().findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(this);


        btn0 = (Button) getActivity().findViewById(R.id.btn_zerp);
        btn0.setOnClickListener(this);
        btn1 = (Button) getActivity().findViewById(R.id.btn_one);
        btn1.setOnClickListener(this);
        btn2 = (Button) getActivity().findViewById(R.id.btn_two);
        btn2.setOnClickListener(this);
        btn3 = (Button) getActivity().findViewById(R.id.btn_three);
        btn3.setOnClickListener(this);
        btn4 = (Button) getActivity().findViewById(R.id.btn_four);
        btn4.setOnClickListener(this);
        btn5 = (Button) getActivity().findViewById(R.id.btn_five);
        btn5.setOnClickListener(this);
        btn6 = (Button) getActivity().findViewById(R.id.btn_six);
        btn6.setOnClickListener(this);
        btn7 = (Button) getActivity().findViewById(R.id.btn_seven);
        btn7.setOnClickListener(this);
        btn8 = (Button) getActivity().findViewById(R.id.btn_eight);
        btn8.setOnClickListener(this);
        btn9 = (Button) getActivity().findViewById(R.id.btn_nine);
        btn9.setOnClickListener(this);
        btn_erase = (Button) getActivity().findViewById(R.id.btn_erase);
        btn_erase.setOnClickListener(this);

        btn_erase.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ed_numerical_field.setText("");
                return false;
            }
        });


    }

    private void sendRequestToServer() {

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

                        if(routes.size() == 1){
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

    private void registerCustomReceiver() {
        //Broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

               // ed_numerical_field.requestFocus();
              //  InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
              //  imm.showSoftInput(ed_numerical_field, InputMethodManager.SHOW_IMPLICIT);
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(getActivity().getPackageName() + KEY_BROADCAST);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.btn_submit) {
            submitDataToServer();
            return;
        }
        if (id == R.id.btn_cancel) {

            if(routes.size() != 1){
                RouteSelectedListAdapter.selectedId = "";
                routeSelectedListAdapter.notifyDataSetChanged();
            }
            ed_numerical_field.setText("");
            return;

        }
        if(id == R.id.btn_erase){
            String str = ed_numerical_field.getText().toString();
            if (str != null && str.length() > 0) {
                str = str.substring(0, str.length()-1);
                ed_numerical_field.setText(str);
            }

            return;
        }

        if(id == R.id.btn_one || id == R.id.btn_two || id == R.id.btn_three || id == R.id.btn_four ||
                id == R.id.btn_five || id == R.id.btn_six || id == R.id.btn_seven || id == R.id.btn_eight
                || id == R.id.btn_nine || id == R.id.btn_zerp)
        {
            Button btn = (Button)v;
            String charText = btn.getText().toString();
            ed_numerical_field.setText(ed_numerical_field.getText().toString() + charText);
        }

    }

    private void submitDataToServer() {

        if (!ed_numerical_field.getText().toString().isEmpty() && !RouteSelectedListAdapter.selectedId.isEmpty()) {
            hitUrl(saveManager.getUrlEnv() + Constant.URL_VALET, RouteSelectedListAdapter.selectedId, ed_numerical_field.getText().toString());
        } else {
            if (RouteSelectedListAdapter.selectedId.isEmpty()) {
                Toast.makeText(getActivity(), "Please Select A Route", Toast.LENGTH_LONG).show();
            } else if (ed_numerical_field.getText().toString().isEmpty()) {
                ed_numerical_field.setError("Enter Value");
                ed_numerical_field.requestFocus();
                Toast.makeText(getActivity(), "Please Enter Numerial Value", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void hitUrl(String url, final String siteId, final String ticketId) {
        // TODO Auto-generated method stub

        progressBar.setVisibility(View.VISIBLE);
       // Log.d("DEBUG", url);

        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                       // Log.d("DEBUG", response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int response_code = jsonObject.getInt("statusCodeId");
                            showDialog(response_code);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        progressBar.setVisibility(View.INVISIBLE);


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.d("DEBUG", error.getMessage().toString());

                progressBar.setVisibility(View.INVISIBLE);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX//(33.817831, -118.298076)
                Map<String, String> params = new HashMap<String, String>();
                params.put("sessionId", saveManager.getSessionToken());
                params.put("siteId", siteId);
                params.put("ticketId", ticketId);


                //Log.d("DEBUG_selected",String.valueOf(selected));
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
        new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                dialog_start.dismiss();
            }

        }.start();
    }






}
