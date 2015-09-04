package com.example.appdata;

import android.content.Context;
import android.content.SharedPreferences;

public class SaveManager {

    // Shared Preferences
    private static SharedPreferences mSharedPreferences;

    private static final int PRIVATE_MODE = Context.MODE_PRIVATE;
    private static final String PREF_NAME = "com.ips_sentry";
    private static final String KEY_USER_ID = "user_id";


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

    public void setUserId(String value) {
        editor.putString(KEY_USER_ID, value);
        editor.apply();
    }

    public String getUserId() {
        return mSharedPreferences.getString(KEY_USER_ID, "0");
    }


}