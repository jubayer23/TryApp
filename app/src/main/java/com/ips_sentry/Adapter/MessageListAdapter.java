package com.ips_sentry.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
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
import com.ips_sentry.model.Message;
import com.ips_sentry.model.Route;
import com.ips_sentry.utils.Constant;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressLint("DefaultLocale")
public class MessageListAdapter extends BaseAdapter {

    private List<Message> Displayedplaces;
    private List<Message> Originalplaces;
    private LayoutInflater inflater;
    @SuppressWarnings("unused")
    private Activity activity;
    private SaveManager saveManager;


    public MessageListAdapter(Activity activity, List<Message> messages) {
        this.activity = activity;
        this.Displayedplaces = messages;
        this.Originalplaces = messages;
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

            convertView = inflater.inflate(R.layout.list_item_message, parent, false);

            viewHolder = new ViewHolder();


            viewHolder.tv_message = (TextView) convertView
                    .findViewById(R.id.tv_message);
            viewHolder.ll_contentWithBackground = (LinearLayout)convertView.findViewById(R.id.contentWithBackground);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Message message = Displayedplaces.get(position);

        if(message.isSeen()){
            viewHolder.ll_contentWithBackground.setBackgroundResource(R.drawable.out_message_bg_2);
        }else{
            viewHolder.ll_contentWithBackground.setBackgroundResource(R.drawable.out_message_bg);
        }

        final String body = message.getBody();


        viewHolder.tv_message.setText(body);



        return convertView;
    }


    public void addMore() {
        //this.Displayedplaces.addAll(places);
        notifyDataSetChanged();
    }



    private static class ViewHolder {
        private TextView tv_message;
        private LinearLayout ll_contentWithBackground;
    }


}