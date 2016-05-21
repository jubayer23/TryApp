package com.ips_sentry.AlarmManager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.ips_sentry.receiver.BootCompletedIntentReceiver;
import com.ips_sentry.service.MyService;

import java.util.Calendar;

/**
 * Created by comsol on 9/10/2015.
 */
public class AlarmManager {

    private static final int REQUEST_CODE = 0;
    private static final int REQUEST_CODE_CHECKING = 2002;

    Calendar cur_cal = Calendar.getInstance();

    private Context context;

    public AlarmManager(Context context) {
        this.context = context;
    }


    public void startOneTimeAlrm() {

        Intent intent = new Intent(context, MyService.class);

        PendingIntent pendingIntent = PendingIntent.getService(context, REQUEST_CODE, intent,PendingIntent.FLAG_CANCEL_CURRENT);
        //am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
        //		1000 * 5, pi);
        android.app.AlarmManager alarm = (android.app.AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        alarm.set(android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        6 * 1000, pendingIntent);
    }

    public void startRepeatingAlrmForChecking(int after_sec) {

        Intent intent = new Intent(context, BootCompletedIntentReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_CHECKING, intent, 0);
        //am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
        //		1000 * 5, pi);
        android.app.AlarmManager alarm = (android.app.AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        cur_cal.setTimeInMillis(System.currentTimeMillis());
        alarm.setRepeating(android.app.AlarmManager.RTC_WAKEUP, cur_cal.getTimeInMillis(),
                after_sec * 1000, pendingIntent);
    }

    public void stopAlarm() {
        Intent intent = new Intent(context, MyService.class);
        PendingIntent pintent = PendingIntent.getService(
                context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        android.app.AlarmManager alarm = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pintent);
    }

    public void stopAlarmForChecking() {
        Intent intent = new Intent(context, BootCompletedIntentReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_CHECKING, intent, 0);

        android.app.AlarmManager alarm = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
    }

    public void startRepeatingAlarm(int after_sec) {
        Intent intent = new Intent(context, MyService.class);
        PendingIntent pintent = PendingIntent.getService(
                context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        android.app.AlarmManager alarm = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        cur_cal.setTimeInMillis(System.currentTimeMillis());
        alarm.setRepeating(android.app.AlarmManager.RTC_WAKEUP, cur_cal.getTimeInMillis(),
                after_sec * 1000, pintent);

    }

}
