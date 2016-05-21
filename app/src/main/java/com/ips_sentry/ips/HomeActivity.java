package com.ips_sentry.ips;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
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
import com.ips_sentry.fragment.ValetFragment;
import com.ips_sentry.service.MyServiceUpdate;
import com.ips_sentry.userview.AboutPage;
import com.ips_sentry.userview.SettingPreview;
import com.ips_sentry.utils.Constant;
import com.ips_sentry.utils.DummyActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by comsol on 08-Feb-16.
 */
public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    RelativeLayout btn_hidemenu;

    LinearLayout btn_showmap, btn_showroutes, btn_showvalet;

    LinearLayout hide_menu;
    FragmentTransaction transaction;
    Fragment fragment_1, fragment_2, fragment_3;
    FragmentManager fragmentManager;

    RelativeLayout dummy_click_listener;

    TextView tv_help, tv_setting_preview, tv_logout;

    TextView tv_user_activity;

    SaveManager saveManager;

    private static boolean screenDimOnFlag = false;


    private BroadcastReceiver receiverForSetUserActivity, receiverScreenDimTurnOn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_home);


        init();

        registerCustomReceiver();

        Bundle bundle = new Bundle();
        bundle.putBoolean(MainActivity.KEY_TRAFFIC_INFO, saveManager.getTrafficInfo());
        bundle.putBoolean(MainActivity.KEY_SHOWINDIVIDUAL_LABEL, saveManager.getIndividualLabel());

        fragment_1 = new MapFragment();
        fragment_1.setArguments(bundle);
        fragment_2 = new ShowRoutesFragment();
        fragment_3 = new ValetFragment();

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
        btn_showvalet = (LinearLayout) findViewById(R.id.btn_valet);
        btn_showvalet.setOnClickListener(this);

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
        //Broadcast receiverForSetUserActivity
        receiverForSetUserActivity = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String user_activity = intent.getStringExtra(MyServiceUpdate.KEY_USER_STATUS);

                tv_user_activity.setText(user_activity);
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(getPackageName() + "ImActive");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiverForSetUserActivity, filter);

        receiverScreenDimTurnOn = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                boolean onORoff = intent.getBooleanExtra(MyServiceUpdate.KEY_SCREEN_DIM_ON_OFF, false);

                if (onORoff) {
                    screenDimOnFlag = true;
                    //Settings.System.putInt(HomeActivity.this.getContentResolver(),
                    //        Settings.System.SCREEN_BRIGHTNESS, 1);

                    // Log.d("DEBUG","it here 1");

                    WindowManager.LayoutParams lp = getWindow().getAttributes();
                    lp.screenBrightness = 0.0f;// 100 / 100.0f;
                    getWindow().setAttributes(lp);

                   // Intent globalService = new Intent(HomeActivity.this, GlobalTouchService.class);
                   // startService(globalService);

                } else {

                    float curBrightnessValue = 255;
                    try {
                        curBrightnessValue = android.provider.Settings.System.getInt(
                                getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
                        //Log.d("DEBUG",String.valueOf(getWindow().getAttributes().screenBrightness));
                    } catch (Settings.SettingNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    // Settings.System.putInt(HomeActivity.this.getContentResolver(),
                    //        Settings.System.SCREEN_BRIGHTNESS, 255);

                    WindowManager.LayoutParams lp = getWindow().getAttributes();
                    lp.screenBrightness = curBrightnessValue / (float) 255;
                    getWindow().setAttributes(lp);

                    //we start this activity only for refresh the screem
                    startActivity(new Intent(HomeActivity.this, DummyActivity.class));
                }


            }
        };

        IntentFilter filter_2 = new IntentFilter();
        filter_2.addAction(getPackageName() + MyServiceUpdate.KEY_BROADCAST_FOR_SCREEN_DIM);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiverScreenDimTurnOn, filter_2);
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
            if (fragment_3.isAdded()) {
                transaction.hide(fragment_3);
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
            if (fragment_3.isAdded()) {
                transaction.hide(fragment_3);
            }
            // Hide fragment C
            // Commit changes
            transaction.commit();


        }
        if (id == R.id.btn_valet) {
            btnToggleColor("show_valet");

            transaction = getSupportFragmentManager()
                    .beginTransaction();

            if (fragment_3.isAdded()) { // if the fragment is already in container
                transaction.show(fragment_3);
                fragment_3.onResume();
            } else { // fragment needs to be added to frame container
                transaction.add(R.id.container, fragment_3, "third");
            }
            // Hide fragment B
            if (fragment_1.isAdded()) {
                transaction.hide(fragment_1);
            }
            if (fragment_2.isAdded()) {
                transaction.hide(fragment_2);
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

            hitUrlForSignOut(saveManager.getUrlEnv() + Constant.URL_SIGNOUT);

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
            btn_showvalet.setBackgroundColor(getResources().getColor(R.color.white));
        } else if (btn_name.equalsIgnoreCase("show_routes")) {
            btn_showmap.setBackgroundColor(getResources().getColor(R.color.white));
            btn_showroutes.setBackgroundColor(getResources().getColor(R.color.blue_light));
            btn_showvalet.setBackgroundColor(getResources().getColor(R.color.white));
        } else {
            btn_showmap.setBackgroundColor(getResources().getColor(R.color.white));
            btn_showroutes.setBackgroundColor(getResources().getColor(R.color.white));
            btn_showvalet.setBackgroundColor(getResources().getColor(R.color.blue_light));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Intent globalService = new Intent(HomeActivity.this, GlobalTouchService.class);
       // stopService(globalService);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverForSetUserActivity);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverScreenDimTurnOn);
    }


    public boolean dispatchTouchEvent(MotionEvent event) {

        if (screenDimOnFlag) {
            screenDimOnFlag = false;

            float curBrightnessValue = 255;
            try {
                curBrightnessValue = android.provider.Settings.System.getInt(
                        getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
                //Log.d("DEBUG",String.valueOf(getWindow().getAttributes().screenBrightness));
            } catch (Settings.SettingNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // Settings.System.putInt(HomeActivity.this.getContentResolver(),
            //        Settings.System.SCREEN_BRIGHTNESS, 255);

            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = curBrightnessValue / (float) 255;
            getWindow().setAttributes(lp);

            //we start this activity only for refresh the screem
            startActivity(new Intent(HomeActivity.this, DummyActivity.class));
        }

        MyServiceUpdate.lastUpdateForScreenOff = 0;

        return super.dispatchTouchEvent(event);


    }


}
