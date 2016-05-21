package com.ips_sentry.utils;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ips_sentry.ips.R;

/**
 * Created by comsol on 03-May-16.
 */
public class DummyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_success);
       // Log.d("DEBUG","YesItCreated");
        finish();
    }
}
