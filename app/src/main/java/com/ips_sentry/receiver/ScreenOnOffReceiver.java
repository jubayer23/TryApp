package com.ips_sentry.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ips_sentry.service.MyServiceUpdate;

/**
 * Created by comsol on 09-Mar-16.
 */
public class ScreenOnOffReceiver extends BroadcastReceiver {

    // THANKS JASON
    public static boolean wasScreenOn = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // DO WHATEVER YOU NEED TO DO HERE
            wasScreenOn = false;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // AND DO WHATEVER YOU NEED TO DO HERE
            wasScreenOn = true;
        }

       // Log.d("DEBUG_SCREEN_0",String.valueOf(wasScreenOn));
        //Fire the intent with activity name & confidence
        Intent i = new Intent(context.getPackageName() + MyServiceUpdate.KEY_ACTION_SCREENONOFF);
        i.putExtra(MyServiceUpdate.KEY_SCREEN_ONOFF, wasScreenOn);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);

    }

}