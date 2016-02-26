package com.ips_sentry.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ips_sentry.appdata.AppController;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.ips.R;
import com.ips_sentry.model.Route;
import com.ips_sentry.utils.Constant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressLint("DefaultLocale")
public class RouteListAdapter extends BaseAdapter {

    private List<Route> Displayedplaces;
    private List<Route> Originalplaces;
    private LayoutInflater inflater;
    @SuppressWarnings("unused")
    private Activity activity;
    private SaveManager saveManager;


    public RouteListAdapter(Activity activity, List<Route> routes) {
        this.activity = activity;
        this.Displayedplaces = routes;
        this.Originalplaces = routes;
        saveManager = new SaveManager(activity);


        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return Displayedplaces.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {

            convertView = inflater.inflate(R.layout.list_item_showroutes, parent, false);

            viewHolder = new ViewHolder();


            viewHolder.rl_layout = (RelativeLayout) convertView
                    .findViewById(R.id.main_container);

            viewHolder.route_name = (TextView) convertView
                    .findViewById(R.id.route_name);

            viewHolder.checkBox = (CheckBox) convertView
                    .findViewById(R.id.checkbox);

            viewHolder.progressBar = (ProgressBar) convertView
                    .findViewById(R.id.loadingProgterm_in_item);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Route route = Displayedplaces.get(position);


        viewHolder.route_name.setText(route.getRouteName());


        if (route.isSelected()) {
            viewHolder.checkBox.setChecked(true);
        } else {
            viewHolder.checkBox.setChecked(false);
        }

        viewHolder.rl_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.checkBox.setVisibility(View.INVISIBLE);
                viewHolder.progressBar.setVisibility(View.VISIBLE);

                sendRequestToServerForSelectionConfirmation(viewHolder.checkBox, viewHolder.progressBar, route);
            }
        });

        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.checkBox.setVisibility(View.INVISIBLE);
                viewHolder.progressBar.setVisibility(View.VISIBLE);

                sendRequestToServerForSelectionConfirmation(viewHolder.checkBox, viewHolder.progressBar, route);
            }
        });



        return convertView;
    }

    private void sendRequestToServerForSelectionConfirmation(final CheckBox checkbox, final ProgressBar progressbar, final Route route) {

        //progressBar.setVisibility(View.VISIBLE);

        String url = saveManager.getGpsUrlEnv() + Constant.URL_SELECT_DESELECT_ROUTES;

        StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (route.isSelected()) {
                            route.setSelected(false);
                            checkbox.setChecked(false);
                        } else {
                            checkbox.setChecked(true);
                            route.setSelected(true);
                        }

                        checkbox.setVisibility(View.VISIBLE);
                        progressbar.setVisibility(View.INVISIBLE);


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                checkbox.setChecked(false);
                checkbox.setVisibility(View.VISIBLE);
                progressbar.setVisibility(View.INVISIBLE);

            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                //userId=XXX&routeId=XXX&selected=XXX

                Map<String, String> params = new HashMap<String, String>();
                params.put("sessionId", saveManager.getSessionToken());
                params.put("routeId", route.getRouteId());
                params.put("selected", String.valueOf(!route.isSelected()));
                //Log.d("DEBUG_selected",String.valueOf(selected));

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(req);

    }

    public void addMore() {
        //this.Displayedplaces.addAll(places);
        notifyDataSetChanged();
    }


    private static class ViewHolder {
        private TextView route_name;
        RelativeLayout rl_layout;
        ProgressBar progressBar;
        CheckBox checkBox;
    }


}