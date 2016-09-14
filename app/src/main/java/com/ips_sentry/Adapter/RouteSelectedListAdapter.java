package com.ips_sentry.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ips_sentry.appdata.AppController;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.fragment.ValetFragment;
import com.ips_sentry.ips.R;
import com.ips_sentry.model.Route;
import com.ips_sentry.utils.Constant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressLint("DefaultLocale")
public class RouteSelectedListAdapter extends BaseAdapter {

    private List<Route> routes;
    private LayoutInflater inflater;
    @SuppressWarnings("unused")
    private Activity activity;
    private SaveManager saveManager;

    public static String selectedId = "";
    public static String alreadySelectedId = "";


    public RouteSelectedListAdapter(Activity activity, List<Route> routes) {
        this.activity = activity;
        this.routes = routes;
        if (routes.size() == 1) {
            selectedId = routes.get(0).getRouteId();
        }
        saveManager = new SaveManager(activity);


        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return routes.size();
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

            convertView = inflater.inflate(R.layout.list_item_selectedroutes, parent, false);

            viewHolder = new ViewHolder();


            viewHolder.rl_layout = (RelativeLayout) convertView
                    .findViewById(R.id.main_container);

            viewHolder.route_name = (TextView) convertView
                    .findViewById(R.id.route_name);

            viewHolder.radioButton = (RadioButton) convertView.findViewById(R.id.radiobutton_routes);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Route route = routes.get(position);


        viewHolder.route_name.setText(route.getRouteName());

        if (selectedId.equalsIgnoreCase(route.getRouteId())) {

            viewHolder.radioButton.setChecked(true);

            if (!selectedId.equalsIgnoreCase(alreadySelectedId)) {
                alreadySelectedId = selectedId;
                Intent i = new Intent(activity.getPackageName() + ValetFragment.KEY_BROADCAST);
                LocalBroadcastManager.getInstance(activity).sendBroadcast(i);

            }


        } else {

            viewHolder.radioButton.setChecked(false);
        }


        viewHolder.radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedId = route.getRouteId();
                alreadySelectedId = "";
                notifyDataSetChanged();
            }
        });

        return convertView;
    }


    public void addMore() {
        //this.routes.addAll(places);
        notifyDataSetChanged();
    }


    private static class ViewHolder {
        private TextView route_name;
        private RadioButton radioButton;
        RelativeLayout rl_layout;
    }


}