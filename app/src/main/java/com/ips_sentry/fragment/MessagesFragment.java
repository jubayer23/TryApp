package com.ips_sentry.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.ips_sentry.Adapter.MessageListAdapter;
import com.ips_sentry.HomeActivity;
import com.ips_sentry.UserSettingActivity;
import com.ips_sentry.appdata.AppController;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.ips.R;
import com.ips_sentry.model.Message;
import com.ips_sentry.service.MyServiceUpdate;
import com.ips_sentry.utils.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MessagesFragment extends Fragment {


    private ListView listViewMessage;
    private MessageListAdapter messageListAdapter;

    private SaveManager saveManager;


    private BroadcastReceiver receiverForMessage, receiverForMakeSeen;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.show_messages, container,
                false);

    }

    public void onActivityCreated(Bundle SavedInstanceState) {
        super.onActivityCreated(SavedInstanceState);

        init();

        messageListAdapter = new MessageListAdapter(getActivity(), Constant.messageList);

        listViewMessage.setAdapter(messageListAdapter);

        listViewMessage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Message message = Constant.messageList.get(position);


                if (message.getType().equalsIgnoreCase(Constant.incomingMessage)) {
                    message.setReplied(true);
                    showDialogForReply(message.getId());
                }


            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d("DEBUG",Constant.messageList.get(0));

        saveManager.setNumOfUnseenMessage(0);

        Constant.makeMessageSeen();

        messageListAdapter.notifyDataSetChanged();

        Constant.isMessageLayoutResume = true;

        scroll();

        registerCustomReceiver();
    }

    private void registerCustomReceiver() {
        receiverForMessage = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                messageListAdapter = new MessageListAdapter(getActivity(), Constant.messageList);

                listViewMessage.setAdapter(messageListAdapter);

                scroll();

                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getActivity(), notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(getActivity().getPackageName() + MyServiceUpdate.KEY_BROADCAST_FOR_MESSAGE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiverForMessage, filter);


        receiverForMakeSeen = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Constant.isIncomingMessageDuringOnResume = false;

                //messageListAdapter = new MessageListAdapter(getActivity(), Constant.messageList);

                // listViewMessage.setAdapter(messageListAdapter);

                //scroll();
                messageListAdapter.notifyDataSetChanged();
                // Log.d("DEBUG","receiver called");
            }
        };

        IntentFilter filter_2 = new IntentFilter();
        filter_2.addAction(getActivity().getPackageName() + HomeActivity.KEY_BROADCAST_FOR_MESSAGE_SEEN);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiverForMakeSeen, filter_2);
    }

    private void init() {

        listViewMessage = (ListView) getActivity().findViewById(R.id.messagesContainer);

        saveManager = new SaveManager(getActivity());


    }


    @Override
    public void onPause() {
        super.onPause();

        //Log.d("DEBUG","onPause");

        Constant.isMessageLayoutResume = false;

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiverForMessage);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiverForMakeSeen);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiverForMessage);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiverForMakeSeen);
    }

    private void scroll() {
        listViewMessage.setSelection(listViewMessage.getCount() - 1);


    }

    public void showDialogForReply(final int id) {

        final Dialog dialog = new Dialog(getActivity(),
                android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_reply);


        final EditText et_dialog_password = (EditText) dialog.findViewById(R.id.dialog_password);
        et_dialog_password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        Button btn_submit = (Button) dialog.findViewById(R.id.dialog_submit);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = et_dialog_password.getText().toString().trim();


                if (password.isEmpty()) {
                    et_dialog_password.setError("Enter Reply");
                    return;
                }

                hitUrlForAllMessage(saveManager.getUrlEnv() + Constant.URL_UserSendMessage,
                        saveManager.getSessionToken(),
                        String.valueOf(id),
                        et_dialog_password.getText().toString());



                Message message = new Message(et_dialog_password.getText().toString(),true,Constant.outGoingMessage);


                Constant.messageList.add(message);

                messageListAdapter.notifyDataSetChanged();


                scroll();

                dialog.dismiss();
                //TODO
            }
        });


        dialog.show();


    }


    private void hitUrlForAllMessage(String url, final String session_id, final String messageId, final String message) {

        // pDialogHome.show();

        // TODO Auto-generated method stub
        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // pDialogHome.dismiss();
                       // Log.d("debug","success");

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //pDialogHome.dismiss();

                // Log.d("DEbug","error");

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                // public JsonResult SendMessage(string sessionId, int messageId, string replyMessage)
                Map<String, String> params = new HashMap<String, String>();
                params.put("sessionId", session_id);
                params.put("messageId", messageId);
                params.put("replyMessage", message);
                // Log.d("DEBUG_selected",String.valueOf(finalBatteryPct));
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }


}
