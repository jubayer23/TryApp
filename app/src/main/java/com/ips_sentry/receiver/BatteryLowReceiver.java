package com.ips_sentry.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ips_sentry.appdata.AppController;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.service.MyServiceUpdate;
import com.ips_sentry.setting.ConnectionDetector;
import com.ips_sentry.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by comsol on 8/28/2015.
 */
public class BatteryLowReceiver extends BroadcastReceiver {


    SaveManager saveManager;
    ConnectionDetector cd;

    @Override
    public void onReceive(Context context, Intent intent) {

        saveManager = new SaveManager(context);
        cd = new ConnectionDetector(context);

        if(intent.getAction().equals("android.intent.action.BATTERY_LOW") && !saveManager.getSessionToken().equals("0") && cd.isConnectingToInternet())
        {
                // Do Something

            int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int level = -1;
            if (rawlevel >= 0 && scale > 0) {
                level = (rawlevel * 100) / scale;
            }

            hitUrl(saveManager.getGpsUrlEnv() + Constant.URL_BATTERY_DAMAGE , level + "%");

        }
    }
    private void hitUrl(String url, final String level) {
        // TODO Auto-generated method stub


        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


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
                params.put("sessionId", saveManager.getSessionToken());
                params.put("status", level);
                //Log.d("DEBUG_selected",String.valueOf(selected));
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }

}