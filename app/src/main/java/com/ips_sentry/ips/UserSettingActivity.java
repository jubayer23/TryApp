package com.ips_sentry.ips;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.utils.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by comsol on 13-Jan-16.
 */
public class UserSettingActivity extends AppCompatActivity implements View.OnClickListener {

    Spinner spinner_gps_interval, spinner_url_env;
    TextView tv_gps_url;

    private SaveManager saveManager;

    List<String> list_gps_interval, list_url_env;

    Button btn_save;


    public static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_usersetting);

        init();


        // getActionBar().setDisplayHomeAsUpEnabled(true);
        spinner_gps_interval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();

                item = item.trim();

                saveManager.setGpsInterval(item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // getActionBar().setDisplayHomeAsUpEnabled(true);
        spinner_url_env.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();

                item = item.trim();

                if(item.equalsIgnoreCase(Constant.url_env[3]))
                {
                    saveManager.setGpsUrlEnv(Constant.URL_PREFIX );
                }else
                {
                    saveManager.setGpsUrlEnv(Constant.URL_PREFIX + item + ".");
                }



                updateGpsUrlTextView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void init() {

        saveManager = new SaveManager(this);

        list_gps_interval = new ArrayList<String>();

        list_url_env = new ArrayList<String>();

        tv_gps_url = (TextView) findViewById(R.id.setting_tv_gps_url);

        spinner_gps_interval = (Spinner) findViewById(R.id.setting_spinner_gps_interval);

        spinner_url_env = (Spinner) findViewById(R.id.setting_spinner_url_env);

        btn_save = (Button) findViewById(R.id.setting_save);

        btn_save.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();


        updateGpsUrlTextView();


        list_gps_interval.add(saveManager.getGpsInterval());

        for (int i = 0; i < Constant.gps_interval.length; i++) {

            if (list_gps_interval.contains(Constant.gps_interval[i])) continue;

            list_gps_interval.add(Constant.gps_interval[i]);

        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (this, R.layout.spinner_item, list_gps_interval);

        spinner_gps_interval.setAdapter(dataAdapter);

        /***********************************************/
        if(saveManager.getGpsUrlEnv().length() > Constant.URL_PREFIX.length())
        {
            list_url_env.add(saveManager.getGpsUrlEnv().substring(Constant.URL_PREFIX.length(),saveManager.getGpsUrlEnv().length()-1));

        }else
        {
            list_url_env.add(Constant.url_env[3]);
        }

        for (int i = 0; i < Constant.url_env.length; i++) {

            if (list_url_env.contains(Constant.url_env[i])) continue;

            list_url_env.add(Constant.url_env[i]);

        }
        ArrayAdapter<String> dataAdapter_url_env = new ArrayAdapter<String>
                (this, R.layout.spinner_item, list_url_env);

        spinner_url_env.setAdapter(dataAdapter_url_env);


    }

    private void updateGpsUrlTextView() {

        if(saveManager.getGpsUrlEnv().length() > Constant.URL_PREFIX.length())
        {
            String next = saveManager.getGpsUrlEnv().substring(0, Constant.URL_PREFIX.length()) +
                    "<font color='#59A5E5'>" + saveManager.getGpsUrlEnv().substring(Constant.URL_PREFIX.length(),saveManager.getGpsUrlEnv().length()-1) + "</font>" + ".";
            tv_gps_url.setText(Html.fromHtml(next + Constant.URL_GPSUpdate));
        }else
        {
            //String next = saveManager.getGpsUrlEnv().substring(0, Constant.URL_PREFIX.length()) +
             //       "<font color='#59A5E5'>" + saveManager.getGpsUrlEnv().substring(Constant.URL_PREFIX.length(),saveManager.getGpsUrlEnv().length()-1) + "</font>";
            tv_gps_url.setText(Html.fromHtml(saveManager.getGpsUrlEnv() + Constant.URL_GPSUpdate));
        }




    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.setting_save) {


            saveManager.setGpsUrl(saveManager.getGpsUrlEnv() + Constant.URL_GPSUpdate);

            Toast.makeText(UserSettingActivity.this, "Saved Successfull", Toast.LENGTH_LONG).show();


            finish();
        }
    }
}
