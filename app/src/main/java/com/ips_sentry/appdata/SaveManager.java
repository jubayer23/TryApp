package com.ips_sentry.appdata;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.ips_sentry.model.Message;
import com.ips_sentry.utils.Constant;

import java.util.ArrayList;

public class SaveManager {

    // Shared Preferences
    private static SharedPreferences mSharedPreferences;

    private static final int PRIVATE_MODE = Context.MODE_PRIVATE;
    private static final String PREF_NAME = "com.creative.ips_sentry";
    private static final String KEY_SESSION_TOKEN = "session_token";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PASSWORD = "user_password";
    private static final String KEY_SIGN_INOUT = "tracking_onoff";
    private static final String KEY_USER_LAT = "user_lat";
    private static final String KEY_USER_LANG = "user_lng";
    private static final String KEY_GPS_URL = "gps_url";
    private static final String KEY_URL_ENV = "url_env";
    private static final String KEY_GPS_INTERVAL = "gps_interval";
    private static final String KEY_DUMMY_GPS_INTERVAL = "gps_dummy_interval";
    private static final String KEY_STOPPED_THRESHOLD = "stopped_interval";
    private static final String KEY_USER_CURRENT_ACTIVITY = "user_activity";
    private static final String KEY_RECORD_TIME = "record_time";
    private static final String KEY_TRAFFIC_INFO = "showTrafficInfo";
    private static final String KEY_SHOWINDIVIDUAL = "showIndividualLabels";
    private static final String KEY_UNSEEN_MESSAGE = "unseen_message";

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

    public void setIndividualLabel(Boolean value) {
        editor.putBoolean(KEY_SHOWINDIVIDUAL, value);
        editor.apply();
    }

    public Boolean getIndividualLabel() {
        return mSharedPreferences.getBoolean(KEY_SHOWINDIVIDUAL, false);
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
        return mSharedPreferences.getString(KEY_GPS_URL, Constant.URL_ENV + Constant.URL_GPSUpdate);
    }


    public void setUrlEnv(String value) {
        editor.putString(KEY_URL_ENV, value);
        editor.apply();
    }

    public String getUrlEnv() {
        return mSharedPreferences.getString(KEY_URL_ENV, Constant.URL_ENV);
    }

    public void setStoppedThreshold(String value) {
        editor.putString(KEY_STOPPED_THRESHOLD, value);
        editor.apply();
    }

    public String getStoppedThreshold() {
        return mSharedPreferences.getString(KEY_STOPPED_THRESHOLD, Constant.stopped_threshold[1]);
    }


    public void setGpsInterval(int value) {
        editor.putInt(KEY_GPS_INTERVAL, value);
        editor.apply();
    }

    public int getGpsInterval() {
        return mSharedPreferences.getInt(KEY_GPS_INTERVAL, Integer.parseInt(Constant.gps_interval[0]));
    }

    public void setPermanentGpsInterval(int value) {
        editor.putInt(KEY_DUMMY_GPS_INTERVAL, value);
        editor.apply();
    }

    public int getPermanentGpsInterval() {
        return mSharedPreferences.getInt(KEY_DUMMY_GPS_INTERVAL, Integer.parseInt(Constant.gps_interval[0]));
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
        return mSharedPreferences.getLong(KEY_RECORD_TIME, 0);
    }

    public void setNumOfUnseenMessage(int value) {
        editor.putInt(KEY_UNSEEN_MESSAGE, value);
        editor.apply();
    }

    public int getNumOfUnseenMessage() {
        return mSharedPreferences.getInt(KEY_UNSEEN_MESSAGE, 0);
    }


    // public void setMessageList(ArrayList<String> list) {
    //editor = pref.edit();


    //  if(list.size() > 100)
    //  {
    //      editor.putInt("Count", 100);

    //      int size = list.size();

    //       for(int i = 99;i>=0;i--){
    //           editor.putString("messagesValue_" + i, list.get(--size));
    //       }

    //   }else{
    //       editor.putInt("Count", list.size());
    //       int count = 0;
    //       for (String message : list) {
    //           editor.putString("messagesValue_" + count++, message);
    //      }
    //    }


    //editor.commit();
    // }
    //  public ArrayList<String> getMessageList() {
    //    ArrayList<String> temp = new ArrayList<String>();

    //   int count = mSharedPreferences.getInt("Count", 0);
    //   temp.clear();
    //   for (int i = 0; i < count; i++) {
    //         temp.add(mSharedPreferences.getString("messagesValue_" + i, ""));
    //    }
    //    return temp;
    // }


    public void setMessageObjList(ArrayList<Message> list) {
        // editor = pref.edit();
        if (list.size() > 100) {
            editor.putInt("Count", 100);

            int size = list.size();

            for (int i = 99; i >= 0; i--) {
                Gson gson = new Gson();
                String json = gson.toJson(list.get(--size));

                editor.putString("favouriteplace_" + i, json);
            }

        } else {
            editor.putInt("Count", list.size());
            int count = 0;
            for (Message i : list) {

                Gson gson = new Gson();
                String json = gson.toJson(i); // myObject - instance of MyObject

                editor.putString("favouriteplace_" + count++, json);
            }
        }

        editor.commit();
    }

    public ArrayList<Message> getMessageObjList() {
        ArrayList<Message> temp = new ArrayList<Message>();

        int count = mSharedPreferences.getInt("Count", 0);
        temp.clear();
        for (int i = 0; i < count; i++) {

            Gson gson = new Gson();
            String json = mSharedPreferences.getString("favouriteplace_" + i, "");
            Message obj = gson.fromJson(json, Message.class);
            temp.add(obj);


            //temp.add(mSharedPreferences.getString("favouriteplace_" + i, ""));
        }
        return temp;
    }
}