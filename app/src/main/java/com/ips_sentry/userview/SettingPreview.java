package com.ips_sentry.userview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.ips.R;
import com.ips_sentry.utils.Constant;

/**
 * Created by comsol on 02-Feb-16.
 */
public class SettingPreview extends AppCompatActivity {
    TextView tv_server_env,tv_gps_interval,tv_stopped_threshold,dim_delay;
    SaveManager saveManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_preview);

        saveManager = new SaveManager(this);

        tv_server_env= (TextView)findViewById(R.id.tv_server_env);
        tv_gps_interval= (TextView)findViewById(R.id.tv_gps_interval);
        tv_stopped_threshold= (TextView)findViewById(R.id.tv_stopped_threshold);
        dim_delay= (TextView)findViewById(R.id.tv_dim_delay);

        String s = saveManager.getUrlEnv().substring(Constant.URL_PREFIX.length(),saveManager.getUrlEnv().length()-1);


        tv_server_env.setText(s);
        tv_gps_interval.setText(saveManager.getGpsInterval() + " sec");
        tv_stopped_threshold.setText(saveManager.getStoppedThreshold() + " mins");
        dim_delay.setText(saveManager.getDimDelay());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:

                onBackPressed();
                break;

        }

        return true;
    }
}
