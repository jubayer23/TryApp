package com.example.appdata;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SaveManager {

    // Shared Preferences
    private static SharedPreferences mSharedPreferences;

    private static final int PRIVATE_MODE = Context.MODE_PRIVATE;
    private static final String PREF_NAME = "com.ips_sentry";
    private static final String KEY_SESSION_TOKEN = "session_token";
    private static final String KEY_COOKIE = "cookie";


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

    public void saveCookie(String cookie) {
        if (cookie == null) {
            //the server did not return a cookie so we wont have anything to save

            Log.d("COKKIE", "nill");
            return;
        }
        // Save in the preferences
        Log.d("COKKIE", cookie);

        editor.putString(KEY_COOKIE, cookie);
        editor.commit();
    }
    public String getCookie() {
        return mSharedPreferences.getString(KEY_COOKIE, "0");
    }
}