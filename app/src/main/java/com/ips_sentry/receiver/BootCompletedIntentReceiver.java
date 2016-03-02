package com.ips_sentry.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.service.MyServiceUpdate;

/**
 * Created by comsol on 8/28/2015.
 */
public class BootCompletedIntentReceiver extends BroadcastReceiver {


    private com.ips_sentry.AlarmManager.AlarmManager myAlarm;
    private SaveManager saveManager;

    @Override
    public void onReceive(Context context, Intent intent) {

        saveManager = new SaveManager(context);
       // Log.d("DEBUG_BOOT", "its hit");
        //if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) && saveManager.getSignInOut())
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) && saveManager.getSignInOut()) {


            Intent intent2 = new Intent(context,
                    MyServiceUpdate.class);
            context.startService(intent2);

        } else if (saveManager.getSignInOut()) {


            Intent intent2 = new Intent(context,
                    MyServiceUpdate.class);
            context.startService(intent2);
        }
    }


}