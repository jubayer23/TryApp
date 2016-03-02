package com.ips_sentry.userview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.ips.R;
import com.ips_sentry.utils.Constant;

import java.util.Calendar;

/**
 * Created by comsol on 02-Feb-16.
 */
public class SettingPreview extends AppCompatActivity {
    TextView tv_server_env,tv_gps_interval;
    SaveManager saveManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_preview);

        saveManager = new SaveManager(this);

        tv_server_env= (TextView)findViewById(R.id.tv_server_env);
        tv_gps_interval= (TextView)findViewById(R.id.tv_gps_interval);

        String s = saveManager.getGpsUrlEnv().substring(Constant.URL_PREFIX.length(),saveManager.getGpsUrlEnv().length()-1);


        tv_server_env.setText(s);
        tv_gps_interval.setText(saveManager.getGpsInterval() + " sec");
    }
}
