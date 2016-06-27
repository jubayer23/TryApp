package com.ips_sentry.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ips_sentry.Adapter.MessageListAdapter;
import com.ips_sentry.HomeActivity;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.ips.R;
import com.ips_sentry.service.MyServiceUpdate;
import com.ips_sentry.utils.Constant;

public class MessagesFragment extends Fragment {


    private ListView listViewMessage;
    private MessageListAdapter messageListAdapter;

    private SaveManager saveManager;

    private BroadcastReceiver receiverForMessage,receiverForMakeSeen;


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

        listViewMessage = (ListView)getActivity().findViewById(R.id.messagesContainer);

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
}
