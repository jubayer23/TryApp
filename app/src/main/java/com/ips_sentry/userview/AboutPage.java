package com.ips_sentry.userview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ips_sentry.ips.R;
import com.ips_sentry.ips.WebViewActivity;

import java.util.Calendar;

/**
 * Created by comsol on 02-Feb-16.
 */
public class AboutPage extends AppCompatActivity {
    TextView tv_copywrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutpage);
        tv_copywrite= (TextView)findViewById(R.id.copywrite);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);

        tv_copywrite.setText("Copyright © "+year +", Information Processing Service, LLC. All Rights Reserved");
    }
    public void openEmail(View view)
    {
        Log.d("DEBUG","YES");
        //Load Default App to Open email
        Intent send = new Intent(Intent.ACTION_SENDTO);
        String uriText = "mailto:" + Uri.encode("contact@IPS-Systems.com") +
                "?subject=" + Uri.encode("the subject") +
                "&body=" + Uri.encode("Help = "+ "abc");
        Uri uri = Uri.parse(uriText);
        send.setData(uri);
        startActivity(Intent.createChooser(send, "Send mail..."));
    }

    public void openDialPad(View view)
    {
        Uri number = Uri.parse("tel:(562) 888-2477");
        Intent dial = new Intent(Intent.ACTION_DIAL, number);
        startActivity(dial);
    }
    public void openWebsite(View view)
    {
        Intent intent = new Intent(AboutPage.this, WebViewActivity.class);
        startActivity(intent);
    }
}
