package com.ips_sentry.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.ips_sentry.Adapter.RouteListAdapter;
import com.ips_sentry.appdata.AppController;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.ips.R;
import com.ips_sentry.model.Route;
import com.ips_sentry.utils.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowRoutesFragment extends Fragment {

	ListView listView;
	RouteListAdapter routeListAdapter;


	List<Route> routes;
	Gson gson;
	ProgressBar progressBar;

	SaveManager saveManager;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.show_routes, container,
				false);

	}

	public void onActivityCreated(Bundle SavedInstanceState) {
		super.onActivityCreated(SavedInstanceState);

		if(SavedInstanceState == null)
		{
			init();

			routeListAdapter = new RouteListAdapter(this.getActivity(), routes);

			listView.setAdapter(routeListAdapter);

			sendRequestToServer();
		}else
		{

		}





	}

	private void init() {

		gson = new Gson();
		routes = new ArrayList<Route>();
		saveManager = new SaveManager(this.getActivity());

		progressBar = (ProgressBar) getActivity().findViewById(R.id.loadingProgterm);
		progressBar.setVisibility(View.INVISIBLE);

		listView = (ListView) getActivity().findViewById(R.id.list);
	}

	private void sendRequestToServer() {

		progressBar.setVisibility(View.VISIBLE);

		String url = saveManager.getGpsUrlEnv() + Constant.URL_SHOW_ROUTES;

		StringRequest req = new StringRequest(Request.Method.POST, url,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {

						//Log.d("HEATMAP","YES");
						try {
							parseJsonFeed(new JSONArray(response));
						} catch (JSONException e) {
							e.printStackTrace();
						}

						routeListAdapter.notifyDataSetChanged();


						if (progressBar.getVisibility() == View.VISIBLE)
							progressBar.setVisibility(View.INVISIBLE);


					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (progressBar.getVisibility() == View.VISIBLE)
					progressBar.setVisibility(View.INVISIBLE);

			}
		}) {
			@Override
			protected Map<String, String> getParams() {
				Map<String, String> params = new HashMap<String, String>();
				params.put("sessionId", saveManager.getSessionToken());

				return params;
			}
		};

		AppController.getInstance().addToRequestQueue(req);

	}

	private void parseJsonFeed(JSONArray response) {


		for (int i = 0; i < response.length(); i++) {

			JSONObject tempObject = null;
			try {
				tempObject = response.getJSONObject(i);

				Route route = gson.fromJson(tempObject.toString(), Route.class);

				//route.setColor(Route.WHITE);

				routes.add(route);

			} catch (JSONException e) {
				e.printStackTrace();
			}


		}


	}
}
