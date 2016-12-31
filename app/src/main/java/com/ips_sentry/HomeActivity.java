package com.ips_sentry;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.ips_sentry.appdata.AppController;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.dialog.AlertDialogManager;
import com.ips_sentry.fragment.AdminFragment;
import com.ips_sentry.fragment.MapFragment;
import com.ips_sentry.fragment.MessagesFragment;
import com.ips_sentry.fragment.ShowRoutesFragment;
import com.ips_sentry.fragment.ValetFragment;
import com.ips_sentry.ips.R;
import com.ips_sentry.model.Message;
import com.ips_sentry.service.MyServiceUpdate;
import com.ips_sentry.userview.AboutPage;
import com.ips_sentry.userview.SettingPreview;
import com.ips_sentry.utils.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by comsol on 08-Feb-16.
 */
public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String KEY_BROADCAST_FOR_MESSAGE_SEEN = "message_seen_by_click";
    private RelativeLayout btn_hidemenu;

    private LinearLayout btn_showmap, btn_showroutes, btn_showvalet, btn_admin;
    private RelativeLayout btn_message;

    private LinearLayout hide_menu;
    private FragmentTransaction transaction;
    private Fragment fragment_1, fragment_2, fragment_3, fragment_4, fragment_5;
    private FragmentManager fragmentManager;

    private RelativeLayout dummy_click_listener;

    private TextView tv_help, tv_setting_preview, tv_logout;

    private TextView tv_user_activity;

    private SaveManager saveManager;

    private ImageView charge_icon;

    private BroadcastReceiver receiverForSetUserActivity;
    private BroadcastReceiver receiverForMessage, receiverBatteryStateChanged;


    private LinearLayout ll_chat_noti;
    private TextView tv_num_of_unseen;


    private static final int TAB_COLOR_UNSELECTED = R.color.gray_dark;
    private static final int TAB_COLOR_SELECTED = R.color.gray_light;

    private TextView tv_batterylevel;


    private ProgressDialog pDialogHome;
    // private static boolean isBatteryPlugedIn = false;

    private Gson gson;

    private View v;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        v = getLayoutInflater().inflate(R.layout.activity_home, null);// or any View (incase generated programmatically )

        setContentView(v);

        init();

        registerCustomReceiver();

        hitUrlForAllMessage(saveManager.getUrlEnv() + Constant.URL_UserAllMessage, saveManager.getSessionToken());

        Bundle bundle = new Bundle();
        bundle.putBoolean(MainActivity.KEY_TRAFFIC_INFO, saveManager.getTrafficInfo());
        bundle.putBoolean(MainActivity.KEY_SHOWINDIVIDUAL_LABEL, saveManager.getIndividualLabel());

        fragment_1 = new MapFragment();
        fragment_1.setArguments(bundle);
        fragment_2 = new ShowRoutesFragment();
        fragment_3 = new ValetFragment();
        fragment_4 = new MessagesFragment();
        fragment_5 = new AdminFragment();

        btnToggleColor("show_routes");
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager
                .beginTransaction();
        transaction.add(R.id.container, fragment_2, "first");
        transaction.commit();


    }

    private void init() {

        saveManager = new SaveManager(this);

        gson = new Gson();

        btn_showmap = (LinearLayout) findViewById(R.id.btn_showmap);
        btn_showmap.setOnClickListener(this);
        btn_showroutes = (LinearLayout) findViewById(R.id.btn_showroutes);
        btn_showroutes.setOnClickListener(this);
        btn_showvalet = (LinearLayout) findViewById(R.id.btn_valet);
        btn_showvalet.setOnClickListener(this);
        btn_message = (RelativeLayout) findViewById(R.id.btn_message);
        btn_message.setOnClickListener(this);
        btn_admin = (LinearLayout) findViewById(R.id.btn_admin);
        btn_admin.setOnClickListener(this);

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

        charge_icon = (ImageView) findViewById(R.id.img_charge_icon);
        ll_chat_noti = (LinearLayout) findViewById(R.id.ll_chat_noti);
        ll_chat_noti.setVisibility(View.GONE);
        tv_num_of_unseen = (TextView) findViewById(R.id.tv_num_of_unseen);
        tv_batterylevel = (TextView) findViewById(R.id.tv_batterylevel);


        // if (DeviceInfoUtils.isPlugged(this)) {
        //    charge_icon.setImageResource(R.drawable.charge_icon);
        //} else {
        //   charge_icon.setImageResource(0);
        // }

        //changeBatteryIcon(DeviceInfoUtils.getBatteryLevel(this));


        pDialogHome = new ProgressDialog(HomeActivity.this);
        pDialogHome.setMessage("Loading.... Please wait...");
        pDialogHome.setIndeterminate(false);
        pDialogHome.setCancelable(false);
        //pDialogHome.show();

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


        receiverForMessage = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (saveManager.getNumOfUnseenMessage() > 0) {

                    ll_chat_noti.setVisibility(View.VISIBLE);
                    tv_num_of_unseen.setText(String.valueOf(saveManager.getNumOfUnseenMessage()));

                } else {
                    ll_chat_noti.setVisibility(View.GONE);
                    //tv_num_of_unseen.setText(String.valueOf(saveManager.getNumOfUnseenMessage()));
                }
            }
        };

        IntentFilter filter_4 = new IntentFilter();
        filter_4.addAction(getPackageName() + MyServiceUpdate.KEY_BROADCAST_FOR_MESSAGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiverForMessage, filter_4);


        receiverBatteryStateChanged = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {


                    //Toast.makeText(HomeActivity.this,"called",Toast.LENGTH_SHORT).show();

                    int plugged =
                            intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

                    if (plugged == BatteryManager.BATTERY_PLUGGED_AC
                            || plugged == BatteryManager.BATTERY_PLUGGED_USB) {
                        charge_icon.setImageResource(R.drawable.charge_icon);

                        //lp.flags |= lp.FLAG_KEEP_SCREEN_ON;

                        if(saveManager.getUserCurrentActivity().equalsIgnoreCase(Constant.USER_ACTIVITY_MOVING) ||
                                saveManager.getUserCurrentActivity().equalsIgnoreCase(Constant.USER_ACTIVITY_IDLE)){
                            v.setKeepScreenOn(true);
                        }



                    } else {
                        charge_icon.setImageResource(0);

                        //  lp.flags |= lp.FLAGS_CHANGED;
                        v.setKeepScreenOn(false);

                    }
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    int batteryPct = 100;
                    if (level != -1 && scale != -1) {
                        batteryPct = (int) ((level / (float) scale) * 100f);
                        changeBatteryIcon(batteryPct);
                    }
                }

            }
        };

        IntentFilter filter_5 = new IntentFilter();
        filter_5.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiverBatteryStateChanged, filter_5);
    }

    private void changeBatteryIcon(int batteryPct) {

        tv_batterylevel.setText(batteryPct + "%");


        if (batteryPct < 9) {
            charge_icon.setBackgroundResource(R.drawable.icon_battery_uncharge_red);
        } else if (batteryPct >= 10 && batteryPct <= 50) {
            charge_icon.setBackgroundResource(R.drawable.icon_battery_uncharge_yellow);
        } else {
            charge_icon.setBackgroundResource(R.drawable.icon_battery_uncharge_green);
        }


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
            if (fragment_4.isAdded()) {
                transaction.hide(fragment_4);
                fragment_4.onPause();
            }
            if (fragment_5.isAdded()) {
                transaction.hide(fragment_5);
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
            if (fragment_4.isAdded()) {
                transaction.hide(fragment_4);
                fragment_4.onPause();
            }
            if (fragment_5.isAdded()) {
                transaction.hide(fragment_5);
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
            if (fragment_4.isAdded()) {
                transaction.hide(fragment_4);
                fragment_4.onPause();
            }
            if (fragment_5.isAdded()) {
                transaction.hide(fragment_5);
            }
            // Hide fragment C
            // Commit changes
            transaction.commit();


        }
        if (id == R.id.btn_message) {
            btnToggleColor("show_message");

            transaction = getSupportFragmentManager()
                    .beginTransaction();

            if (fragment_4.isAdded()) { // if the fragment is already in container
                transaction.show(fragment_4);
                fragment_4.onResume();
                ll_chat_noti.setVisibility(View.GONE);
            } else { // fragment needs to be added to frame container
                transaction.add(R.id.container, fragment_4, "fourth");
                ll_chat_noti.setVisibility(View.GONE);
            }
            // Hide fragment B
            if (fragment_1.isAdded()) {
                transaction.hide(fragment_1);
            }
            if (fragment_2.isAdded()) {
                transaction.hide(fragment_2);
            }
            if (fragment_3.isAdded()) {
                transaction.hide(fragment_3);
            }
            if (fragment_5.isAdded()) {
                transaction.hide(fragment_5);
            }
            // Hide fragment C
            // Commit changes
            transaction.commit();


        }
        if (id == R.id.btn_admin) {
            btnToggleColor("show_admin");

            transaction = getSupportFragmentManager()
                    .beginTransaction();

            if (fragment_5.isAdded()) { // if the fragment is already in container
                transaction.show(fragment_5);
                fragment_5.onResume();
            } else { // fragment needs to be added to frame container
                transaction.add(R.id.container, fragment_5, "fifth");
            }
            // Hide fragment B
            if (fragment_1.isAdded()) {
                transaction.hide(fragment_1);
            }
            if (fragment_2.isAdded()) {
                transaction.hide(fragment_2);
            }
            if (fragment_3.isAdded()) {
                transaction.hide(fragment_3);
            }
            if (fragment_4.isAdded()) {
                transaction.hide(fragment_4);
                fragment_4.onPause();
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


            if (saveManager.getLockCandetials()) {

                  showDialogForCheckPassword();
            } else {
                startLogoutProcess();
            }


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
            btn_showmap.setBackgroundColor(getResources().getColor(TAB_COLOR_SELECTED));
            btn_showroutes.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
            btn_showvalet.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
            btn_message.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
            btn_admin.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
        } else if (btn_name.equalsIgnoreCase("show_routes")) {
            btn_showmap.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
            btn_showroutes.setBackgroundColor(getResources().getColor(TAB_COLOR_SELECTED));
            btn_showvalet.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
            btn_message.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
            btn_admin.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
        } else if (btn_name.equalsIgnoreCase("show_valet")) {
            btn_showmap.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
            btn_showroutes.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
            btn_showvalet.setBackgroundColor(getResources().getColor(TAB_COLOR_SELECTED));
            btn_message.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
            btn_admin.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
        } else if (btn_name.equalsIgnoreCase("show_message")) {
            btn_showmap.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
            btn_showroutes.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
            btn_showvalet.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
            btn_message.setBackgroundColor(getResources().getColor(TAB_COLOR_SELECTED));
            btn_admin.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
        } else {
            btn_showmap.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
            btn_showroutes.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
            btn_showvalet.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
            btn_message.setBackgroundColor(getResources().getColor(TAB_COLOR_UNSELECTED));
            btn_admin.setBackgroundColor(getResources().getColor(TAB_COLOR_SELECTED));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Intent globalService = new Intent(HomeActivity.this, GlobalTouchService.class);
        // stopService(globalService);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverForSetUserActivity);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverForMessage);
        unregisterReceiver(receiverBatteryStateChanged);
    }


    public boolean dispatchTouchEvent(MotionEvent event) {


        if (Constant.isMessageLayoutResume && Constant.isIncomingMessageDuringOnResume) {
            saveManager.setNumOfUnseenMessage(0);
            ll_chat_noti.setVisibility(View.GONE);
            Constant.makeMessageSeen();
            Intent i = new Intent(getPackageName() + KEY_BROADCAST_FOR_MESSAGE_SEEN);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        }

        return super.dispatchTouchEvent(event);


    }


    private void hitUrlForAllMessage(String url, final String session_id) {

        pDialogHome.show();

        // TODO Auto-generated method stub
        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //  Log.d("DEbug",response);

                        Constant.messageList.clear();

                        try {
                            JSONArray jsonArray = new JSONArray(response);


                            //AppConstant.histories.clear();
                            // AppConstant.NUM_OF_UNSEEN_HISTORY = 0;
                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject tempObject = jsonArray.getJSONObject(i);

                                Message message = gson.fromJson(tempObject.toString(), Message.class);

                                if (tempObject.getString("read") != null) message.setSeen(true);
                                else message.setSeen(false);

                                Constant.messageList.add(message);

                            }


                            //Collections.reverse(AppConstant.histories);

                            pDialogHome.dismiss();


                        } catch (JSONException e) {
                            e.printStackTrace();

                            pDialogHome.dismiss();

                            // Log.d("DEbug","tryCatch");
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                pDialogHome.dismiss();

                Log.d("DEbug", "error");

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                Map<String, String> params = new HashMap<String, String>();
                params.put("sessionId", session_id);
                // Log.d("DEBUG_selected",String.valueOf(finalBatteryPct));
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }



    public void showDialogForCheckPassword() {

        final Dialog dialog = new Dialog(this,
                android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_settingpassword);


        final EditText et_dialog_password = (EditText) dialog.findViewById(R.id.dialog_password);
        et_dialog_password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        Button btn_submit = (Button) dialog.findViewById(R.id.dialog_submit);
        Button btn_cancel = (Button) dialog.findViewById(R.id.dialog_cancel);
        btn_cancel.setVisibility(View.VISIBLE);


        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = et_dialog_password.getText().toString().trim();


                if (password.isEmpty()) {
                    et_dialog_password.setError("Enter Password!");
                    return;
                }

                if(password.equals(Constant.ADMIN_PASSWORD)){
                    startLogoutProcess();
                }else{
                    Log.d("DEBUG",saveManager.getUserPassword());
                    new AlertDialogManager().showAlertDialog(HomeActivity.this, "Error", "Password does not match", false);
                }



                dialog.dismiss();
                //TODO
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();


    }

    private  void startLogoutProcess(){
        saveManager.setSignInOut(false);

        hitUrlForSignOut(saveManager.getUrlEnv() + Constant.URL_SIGNOUT);

        deleteSession();

        stopService(new Intent(HomeActivity.this, MyServiceUpdate.class));

        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);

        finish();
    }


}
