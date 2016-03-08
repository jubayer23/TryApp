package com.ips_sentry.ips;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ips_sentry.appdata.AppController;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.fragment.MapFragment;
import com.ips_sentry.fragment.ShowRoutesFragment;
import com.ips_sentry.service.MyServiceUpdate;
import com.ips_sentry.userview.AboutPage;
import com.ips_sentry.userview.SettingPreview;
import com.ips_sentry.utils.Constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by comsol on 08-Feb-16.
 */
public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    RelativeLayout  btn_hidemenu;

   LinearLayout btn_showmap, btn_showroutes;

    LinearLayout hide_menu;
    FragmentTransaction transaction;
    Fragment fragment_1, fragment_2;
    FragmentManager fragmentManager;

    RelativeLayout dummy_click_listener;

    TextView tv_help, tv_setting_preview, tv_logout;

    TextView tv_user_activity;

    SaveManager saveManager;


    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_home);


        init();

        registerCustomReceiver();

        Bundle bundle=new Bundle();
        bundle.putBoolean(MainActivity.KEY_TRAFFIC_INFO, saveManager.getTrafficInfo());
        bundle.putBoolean(MainActivity.KEY_SHOWINDIVIDUAL_LABEL, saveManager.getIndividualLabel());

        fragment_1 = new MapFragment();
        fragment_1.setArguments(bundle);
        fragment_2 = new ShowRoutesFragment();

        btnToggleColor("show_routes");
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager
                .beginTransaction();
        transaction.add(R.id.container, fragment_2, "first");
        transaction.commit();


    }

    private void init() {

        saveManager = new SaveManager(this);

        btn_showmap = (LinearLayout) findViewById(R.id.btn_showmap);
        btn_showmap.setOnClickListener(this);
        btn_showroutes = (LinearLayout) findViewById(R.id.btn_showroutes);
        btn_showroutes.setOnClickListener(this);
        btn_hidemenu = (RelativeLayout) findViewById(R.id.btn_showhidemenu);
        btn_hidemenu.setOnClickListener(this);

        btn_hidemenu = (RelativeLayout) findViewById(R.id.btn_showhidemenu);
        btn_hidemenu.setOnClickListener(this);

        hide_menu = (LinearLayout) findViewById(R.id.hide_menu);
        hide_menu.setVisibility(View.INVISIBLE);

        dummy_click_listener = (RelativeLayout) findViewById(R.id.dummy_click_checker);
        dummy_click_listener.setOnClickListener(this);
        dummy_click_listener.setVisibility(View.GONE);


        tv_help = (TextView) findViewById(R.id.tv_help);
        tv_help.setOnClickListener(this);
        tv_setting_preview = (TextView) findViewById(R.id.tv_setting_preview);
        tv_setting_preview.setOnClickListener(this);
        tv_logout = (TextView) findViewById(R.id.tv_logout);
        tv_logout.setOnClickListener(this);

        tv_user_activity = (TextView) findViewById(R.id.user_activity);

    }

    private void registerCustomReceiver() {
        //Broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String user_activity = intent.getStringExtra(MyServiceUpdate.KEY_USER_STATUS);

                tv_user_activity.setText(user_activity);
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(getPackageName() + "ImActive");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_showmap) {
            btnToggleColor("map");


            transaction = getSupportFragmentManager()
                    .beginTransaction();

            if (fragment_1.isAdded()) { // if the fragment is already in container
                transaction.show(fragment_1);
            } else { // fragment needs to be added to frame container
                transaction.add(R.id.container, fragment_1, "second");
            }
            // Hide fragment B
            if (fragment_2.isAdded()) {
                transaction.hide(fragment_2);
            }
            // Hide fragment C
            // Commit changes
            transaction.commit();


        }
        if (id == R.id.btn_showroutes) {
            btnToggleColor("show_routes");

            transaction = getSupportFragmentManager()
                    .beginTransaction();

            if (fragment_2.isAdded()) { // if the fragment is already in container
                transaction.show(fragment_2);
            } else { // fragment needs to be added to frame container
                transaction.add(R.id.container, fragment_2, "first");
            }
            // Hide fragment B
            if (fragment_1.isAdded()) {
                transaction.hide(fragment_1);
            }
            // Hide fragment C
            // Commit changes
            transaction.commit();


        }

        if (id == R.id.btn_showhidemenu) {
            if (hide_menu.getVisibility() == View.INVISIBLE) {
                hide_menu.setVisibility(View.VISIBLE);
                dummy_click_listener.setVisibility(View.VISIBLE);
            } else {
                hide_menu.setVisibility(View.INVISIBLE);
                dummy_click_listener.setVisibility(View.GONE);
            }
            return;
        }

        if (id == R.id.tv_help) {
            Intent intent = new Intent(HomeActivity.this, AboutPage.class);
            startActivity(intent);
            return;
        }
        if (id == R.id.tv_setting_preview) {
            Intent intent = new Intent(HomeActivity.this, SettingPreview.class);
            startActivity(intent);
            return;

        }
        if (id == R.id.tv_logout) {


            saveManager.setSignInOut(false);

            hitUrlForSignOut(saveManager.getGpsUrlEnv() + Constant.URL_SIGNOUT);

            deleteSession();

            stopService(new Intent(HomeActivity.this, MyServiceUpdate.class));

            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);

            finish();

            return;

        }

        if (id == R.id.dummy_click_checker) {
            if (hide_menu.getVisibility() == View.VISIBLE) {
                hide_menu.setVisibility(View.INVISIBLE);
                dummy_click_listener.setVisibility(View.GONE);
            }

        }
    }


    private void hitUrlForSignOut(String url) {
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
                //Log.d("DEBUG_selected",String.valueOf(selected));
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }

    private void deleteSession()

    {
        saveManager.setSessionToken("0");
    }

    public void btnToggleColor(String btn_name) {
        if (btn_name.equalsIgnoreCase("map")) {
            btn_showmap.setBackgroundColor(getResources().getColor(R.color.blue_light));
            btn_showroutes.setBackgroundColor(getResources().getColor(R.color.white));
        } else {
            btn_showmap.setBackgroundColor(getResources().getColor(R.color.white));
            btn_showroutes.setBackgroundColor(getResources().getColor(R.color.blue_light));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
}
