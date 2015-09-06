package com.example.service;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.appdata.AppController;
import com.example.appdata.SaveManager;
import com.example.setting.ConnectionDetector;
import com.example.setting.GPSTracker;
import com.example.utils.Constant;

public class MyService extends Service {
	Calendar cur_cal = Calendar.getInstance();



	// GPS Location
	GPSTracker gps;

	// Connection detector class
	ConnectionDetector cd;

	private SaveManager saveManager;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Intent intent = new Intent(this, MyService.class);
		PendingIntent pintent = PendingIntent.getService(
				getApplicationContext(), 0, intent, 0);
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		cur_cal.setTimeInMillis(System.currentTimeMillis());
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, cur_cal.getTimeInMillis(),
				50 * 100, pintent);



		cd = new ConnectionDetector(getApplicationContext());

		// creating GPS Class object
		gps = new GPSTracker(this);


		saveManager = new SaveManager(this);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		// your code for background process
		String session_id = saveManager.getSessionToken();

		//Log.d("DEBUG", "onStart");

		if (!session_id.equals("0")) {
			//Log.d("DEBUG", "user enable");
			if (cd.isConnectingToInternet()) {
				//Log.d("DEBUG", "user connextion ok");
				if (gps.canGetLocation()) {
					
					//Log.d("DEBUG", "user gps ok");
					
					String user_lat = String.valueOf(gps.getLatitude());
					String user_lang = String.valueOf(gps.getLongitude());

					String URL = Constant.URL_GPSUpdate
							+ "sessionId="
							+ session_id
							+ "&lat="
							+ user_lat
							+ "&lng="
							+ user_lang;

					Log.d("DEBUG", URL);

					hitUrl(URL);
				}
			}
		}

	}

	private void hitUrl(String uRL) {
		// TODO Auto-generated method stub
		final JsonObjectRequest jsObjRequest = new JsonObjectRequest(
				Request.Method.GET, uRL, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {

						//String textResult = response.toString();

						//Log.d("DEBUG_SUCESS", textResult);
						try {
							boolean status = response.getBoolean("status");
							int id;
							if (status) {
								// id = response.getInt("status");
								Log.d("DEBUG_service", String.valueOf(status));

							}

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {



					}
				});
		// TODO Auto-generated method stub
		AppController.getInstance().addToRequestQueue(jsObjRequest);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		//Log.d("DEBUG", "onBind");
		return null;
	}
}
