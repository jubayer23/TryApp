package com.ips_sentry.appdata;

import android.content.Context;
import android.content.SharedPreferences;

import com.ips_sentry.utils.Constant;

public class SaveManager {

    // Shared Preferences
    private static SharedPreferences mSharedPreferences;

    private static final int PRIVATE_MODE = Context.MODE_PRIVATE;
    private static final String PREF_NAME = "com.ips_sentry";
    private static final String KEY_SESSION_TOKEN = "session_token";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PASSWORD = "user_password";
    private static final String KEY_SIGN_INOUT = "tracking_onoff";
    private static final String KEY_USER_LAT = "user_lat";
    private static final String KEY_USER_LANG= "user_lng";
    private static final String KEY_GPS_URL= "gps_url";
    private static final String KEY_GPS_URL_ENV= "url_env";
    private static final String KEY_GPS_INTERVAL= "gps_interval";
    private static final String KEY_USER_CURRENT_ACTIVITY= "user_activity";
    private static final String KEY_RECORD_TIME= "record_time";
    private static final String KEY_TRAFFIC_INFO= "showTrafficInfo";


    private SharedPreferences.Editor editor;
    private Context context;

    public SaveManager(Context context) {
        this.context = context;
        mSharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = mSharedPreferences.edit();
        editor.apply();
    }

    private SharedPreferences getSharedPreferences(final String prefName,
                                                   final int mode) {
        return this.context.getSharedPreferences(prefName, mode);
    }

    public void setSessionToken(String value) {
        editor.putString(KEY_SESSION_TOKEN, value);
        editor.apply();
    }

    public String getSessionToken() {
        return mSharedPreferences.getString(KEY_SESSION_TOKEN, "0");
    }
    public void setTrafficInfo(Boolean value) {
        editor.putBoolean(KEY_TRAFFIC_INFO, value);
        editor.apply();
    }

    public Boolean getTrafficInfo() {
        return mSharedPreferences.getBoolean(KEY_TRAFFIC_INFO, false);
    }
    public void setUserName(String value) {
        editor.putString(KEY_USER_NAME, value);
        editor.apply();
    }

    public String getUserName() {
        return mSharedPreferences.getString(KEY_USER_NAME, "0");
    }

    public void setUserPassword(String value) {
        editor.putString(KEY_USER_PASSWORD, value);
        editor.apply();
    }

    public String getUserPassword() {
        return mSharedPreferences.getString(KEY_USER_PASSWORD, "0");
    }


    public void setSignInOut(Boolean value) {
        editor.putBoolean(KEY_SIGN_INOUT, value);
        editor.apply();
    }

    public Boolean getSignInOut() {
        return mSharedPreferences.getBoolean(KEY_SIGN_INOUT, true);
    }

    public void setUserLat(String value) {
        editor.putString(KEY_USER_LAT, value);
        editor.apply();
    }

    public String getUserLat() {
        return mSharedPreferences.getString(KEY_USER_LAT, "0");
    }

    public void setUserLang(String value) {
        editor.putString(KEY_USER_LANG, value);
        editor.apply();
    }

    public String getUserLang() {
        return mSharedPreferences.getString(KEY_USER_LANG, "0");
    }


    public void setGpsUrl(String value) {
        editor.putString(KEY_GPS_URL, value);
        editor.apply();
    }

    public String getGpsUrl() {
        return mSharedPreferences.getString(KEY_GPS_URL,Constant.URL_ENV + Constant.URL_GPSUpdate);
    }


    public void setGpsUrlEnv(String value) {
        editor.putString(KEY_GPS_URL_ENV, value);
        editor.apply();
    }

    public String getGpsUrlEnv() {
        return mSharedPreferences.getString(KEY_GPS_URL_ENV, Constant.URL_ENV);
    }


    public void setGpsInterval(String value) {
        editor.putString(KEY_GPS_INTERVAL, value);
        editor.apply();
    }

    public String getGpsInterval() {
        return mSharedPreferences.getString(KEY_GPS_INTERVAL, Constant.gps_interval[0]);
    }

    public void setUserCurrentActivity(String value) {
        editor.putString(KEY_USER_CURRENT_ACTIVITY, value);
        editor.apply();
    }

    public String getUserCurrentActivity() {
        return mSharedPreferences.getString(KEY_USER_CURRENT_ACTIVITY, "null");
    }

    public void setRecordTime(long value) {
        editor.putLong(KEY_RECORD_TIME, value);
        editor.apply();
    }

    public long getRecordTime() {
        return mSharedPreferences.getLong(KEY_RECORD_TIME,0);
    }


}