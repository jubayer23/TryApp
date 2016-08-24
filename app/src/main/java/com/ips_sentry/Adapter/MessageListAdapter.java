package com.ips_sentry.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
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

            viewHolder.content = (LinearLayout) convertView.findViewById(R.id.content);
            viewHolder.tv_message = (TextView) convertView
                    .findViewById(R.id.tv_message);

            viewHolder.tv_reply_alert = (TextView) convertView
                    .findViewById(R.id.tv_reply_alert);

            viewHolder.ll_contentWithBackground = (LinearLayout) convertView.findViewById(R.id.contentWithBackground);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Message message = Displayedplaces.get(position);

        viewHolder.tv_reply_alert.setVisibility(View.GONE);


        if (message.getType().equalsIgnoreCase(Constant.outGoingMessage)) {
            //viewHolder.ll_contentWithBackground.setBackgroundResource(R.drawable.in_message_bg);

            // RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewHolder.ll_contentWithBackground.getLayoutParams();
            // layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            // viewHolder.ll_contentWithBackground.setLayoutParams(layoutParams);


            viewHolder.ll_contentWithBackground.setBackgroundResource(R.drawable.in_message_bg);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) viewHolder.ll_contentWithBackground.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            viewHolder.ll_contentWithBackground.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) viewHolder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            viewHolder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) viewHolder.tv_message.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            viewHolder.tv_message.setLayoutParams(layoutParams);


        } else {
            if (message.isSeen()) {
                viewHolder.ll_contentWithBackground.setBackgroundResource(R.drawable.out_message_bg_2);
            } else {
                viewHolder.ll_contentWithBackground.setBackgroundResource(R.drawable.out_message_bg);
            }


            if(message.isReplied() ){
                viewHolder.tv_reply_alert.setVisibility(View.VISIBLE);
            }
            // RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewHolder.ll_contentWithBackground.getLayoutParams();
            // layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            // viewHolder.ll_contentWithBackground.setLayoutParams(layoutParams);


            //viewHolder.ll_contentWithBackground.setBackgroundResource(R.drawable.out_message_bg_2);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) viewHolder.ll_contentWithBackground.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            viewHolder.ll_contentWithBackground.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) viewHolder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            viewHolder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) viewHolder.tv_message.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            viewHolder.tv_message.setLayoutParams(layoutParams);

            //layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            // layoutParams.gravity = Gravity.LEFT;
            // holder.txtInfo.setLayoutParams(layoutParams);


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
        public LinearLayout content;
        public TextView tv_reply_alert;
    }


}