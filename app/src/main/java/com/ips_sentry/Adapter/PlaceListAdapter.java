package com.ips_sentry.Adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ips_sentry.appdata.AppController;
import com.ips_sentry.model.Venue;
import com.ips_sentry.ips.R;

import java.util.List;

/**
 * Created by comsol on 9/10/2015.
 */
public class PlaceListAdapter extends BaseAdapter {

    private List<Venue> displayedplaces;
    private List<Venue> originalplaces;
    private LayoutInflater inflater;
    @SuppressWarnings("unused")
    private Activity activity;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public PlaceListAdapter(Activity activity, List<Venue> venues) {
        this.activity = activity;
        this.displayedplaces = venues;
        this.originalplaces = venues;

        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return displayedplaces.size();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.venues_2, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.networkImageView = (NetworkImageView) convertView
                    .findViewById(R.id.product_thumb);
            viewHolder.productName = (TextView) convertView
                    .findViewById(R.id.product_name);
            viewHolder.productDetails = (TextView) convertView
                    .findViewById(R.id.product_details);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Venue venue = displayedplaces.get(position);

        float[] results = new float[1];


        if (venue.getLogo() == null || venue.getLogo().isEmpty()) {
            viewHolder.networkImageView.setVisibility(View.GONE);
        } else {
            viewHolder.networkImageView.setVisibility(View.VISIBLE);
            viewHolder.networkImageView.setImageUrl(venue.getLogo(), imageLoader);
        }

        viewHolder.productName.setText(venue.getVenueName());
        viewHolder.productDetails.setText(venue.getVenueAddress());


        return convertView;
    }

    public void addMore(List<Venue> venues) {
        this.displayedplaces.addAll(venues);
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        private NetworkImageView networkImageView;

        private TextView productName;
        private TextView productDetails;
        // private TextView destination;
    }


}
