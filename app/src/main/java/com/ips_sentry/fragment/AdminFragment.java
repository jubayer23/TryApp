package com.ips_sentry.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.ips_sentry.Adapter.RouteSelectedListAdapter;
import com.ips_sentry.appdata.AppController;
import com.ips_sentry.appdata.SaveManager;
import com.ips_sentry.ips.R;
import com.ips_sentry.model.Route;
import com.ips_sentry.utils.Constant;
import com.ips_sentry.utils.LastLocationOnly;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminFragment extends Fragment {

    private static final int NOTIFY_SUPERVISORS = 1;
    private static final int INCIDENT_SUBMIT = 2;
    private Button btn_now_arriving, btn_notify_supervisor, btn_new_incident, btn_rider_count,btn_rename_bus;


    private ListView listView;
    private RouteSelectedListAdapter routeSelectedListAdapter;


    private List<Route> routes;
    private Gson gson;
    private ProgressBar progressBar;

    private SaveManager saveManager;

    // Progress Dialog
    private ProgressDialog pDialog;

    private static final int ONE_MIN_ETA_SEC = 60;
    private static final int THREE_MIN_ETA_SEC = 180;

    private boolean SKIP_ONRESUME = false;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.show_admin, container,
                false);

    }

    public void onActivityCreated(Bundle SavedInstanceState) {
        super.onActivityCreated(SavedInstanceState);

        SKIP_ONRESUME = true;

        init();

        routeSelectedListAdapter = new RouteSelectedListAdapter(this.getActivity(), routes);

        listView.setAdapter(routeSelectedListAdapter);


        RouteSelectedListAdapter.selectedId = "";
        sendRequestToServerForFetchingRoutes();


        btn_now_arriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!RouteSelectedListAdapter.selectedId.isEmpty()) {
                    pDialog.show();

                    hitUrlForEta(saveManager.getUrlEnv() +
                                    Constant.URL_ARRIVAL_NOTIFICATIONS,
                            saveManager.getSessionToken(),
                            RouteSelectedListAdapter.selectedId,
                            ONE_MIN_ETA_SEC);
                } else {
                    Toast.makeText(getActivity(), "Please Select A Route", Toast.LENGTH_LONG).show();
                }


            }


        });

        btn_notify_supervisor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogForIncidentOrNotifySupervisors(NOTIFY_SUPERVISORS);
            }
        });

        btn_new_incident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogForIncidentOrNotifySupervisors(INCIDENT_SUBMIT);
            }
        });

        btn_rider_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showDialogForIncidentOrNotifySupervisors(INCIDENT_SUBMIT);
                showDialogCustomKeyboard();
            }
        });

        btn_rename_bus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showDialogForIncidentOrNotifySupervisors(INCIDENT_SUBMIT);
                showDialogForRenameBus();
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();
        // Log.d("DEBUG",String.valueOf(SKIP_ONRESUME));
        if (SKIP_ONRESUME) {
            SKIP_ONRESUME = false;
        } else {

            RouteSelectedListAdapter.selectedId = "";

            sendRequestToServerForFetchingRoutes();
        }

    }

    private void init() {

        saveManager = new SaveManager(getActivity());

        gson = new Gson();
        routes = new ArrayList<Route>();

        progressBar = (ProgressBar) getActivity().findViewById(R.id.loadingProgterm_admin);
        progressBar.setVisibility(View.INVISIBLE);

        listView = (ListView) getActivity().findViewById(R.id.list_admin);

        btn_now_arriving = (Button) getActivity().findViewById(R.id.btn_now_arriving);
        btn_notify_supervisor = (Button) getActivity().findViewById(R.id.btn_notify_supervisor);
        btn_new_incident = (Button) getActivity().findViewById(R.id.btn_new_incident);
        btn_rider_count = (Button) getActivity().findViewById(R.id.btn_rider_count);
        btn_rename_bus = (Button) getActivity().findViewById(R.id.btn_rename_bus);

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Sending Your Response.... Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
    }


    private void sendRequestToServerForFetchingRoutes() {

        progressBar.setVisibility(View.VISIBLE);

        String url = saveManager.getUrlEnv() + Constant.URL_SHOW_ROUTES;

        //Log.d("DEBUG",url);

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

                        if (routes.size() == 1) {
                            RouteSelectedListAdapter.alreadySelectedId = "";
                            RouteSelectedListAdapter.selectedId = routes.get(0).getRouteId();
                        }
                        routeSelectedListAdapter.notifyDataSetChanged();


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
                params.put("lat", saveManager.getUserLat());
                params.put("lng", saveManager.getUserLang());

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(req);

    }

    private void parseJsonFeed(JSONArray response) {

        routes.clear();
        for (int i = 0; i < response.length(); i++) {

            JSONObject tempObject = null;
            try {
                tempObject = response.getJSONObject(i);

                Route route = gson.fromJson(tempObject.toString(), Route.class);

                if (route.isSelected()) {
                    routes.add(route);
                }

                //route.setColor(Route.WHITE);


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }


    }


    private void hitUrlForEta(String url, final String sessionToken, final String routeId, final int sec) {


        // TODO Auto-generated method stub
        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (pDialog.isShowing()) pDialog.dismiss();

                        showDialog(1);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (pDialog.isShowing()) pDialog.dismiss();


                showDialog(0);

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                Map<String, String> params = new HashMap<String, String>();
                params.put("sessionId", sessionToken);
                params.put("routeId", routeId);
                params.put("seconds", String.valueOf(sec));
                // Log.d("DEBUG_selected",String.valueOf(finalBatteryPct));
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }

    private void showDialog(int code) {
        final Dialog dialog_start = new Dialog(getActivity(),
                android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog_start.setCancelable(true);
        switch (code) {
            case 1:
                dialog_start.setContentView(R.layout.dialog_success);
                dialog_start.show();
                break;
            case 0:
                dialog_start.setContentView(R.layout.dialog_error);
                dialog_start.show();
                break;
        }
        new CountDownTimer(2000, 1000) {

            public void onTick(long millisUntilFinished) {
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                dialog_start.dismiss();
            }

        }.start();
    }


    public void showDialogForIncidentOrNotifySupervisors(final int forWhat) {
        final Dialog dialog = new Dialog(getActivity(),
                android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_admin_reply);


        final EditText et_dialog_password = (EditText) dialog.findViewById(R.id.dialog_password);

        final Button btn_submit = (Button) dialog.findViewById(R.id.dialog_btn_submit);
        btn_submit.setEnabled(false);

        Button btn_cancel = (Button) dialog.findViewById(R.id.dialog_bnt_cancel);


        et_dialog_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() != 0) {
                    btn_submit.setEnabled(true);
                    // btn_submit.setAlpha(0f);
                } else {
                    btn_submit.setEnabled(false);
                    // btn_submit.setAlpha(0.5f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_dialog_password.getText().toString().isEmpty()) {
                    dialog.dismiss();
                } else {

                    new AlertDialog.Builder(getActivity())
                            .setTitle("Alert")
                            .setMessage("Are you sure you want cancel?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog2, int which) {
                                    // continue with delete
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                }
            }
        });


        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(getActivity())
                        .setTitle("Alert")
                        .setMessage("Are you sure you want submit?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog2, int which) {
                                // continue with delete


                                if (forWhat == NOTIFY_SUPERVISORS) {
                                    hitUrlForNotifySupervisors(saveManager.getUrlEnv() +
                                                    Constant.URL_NOTIFY_SUPERVISORS, saveManager.getSessionToken(),
                                            et_dialog_password.getText().toString());
                                } else if (forWhat == INCIDENT_SUBMIT) {
                                    LastLocationOnly gps = new LastLocationOnly(getActivity());

                                    if (gps.canGetLocation()) {

                                        hitUrlForNewIncident(saveManager.getUrlEnv() +
                                                        Constant.URL_NEW_INCIDENT, saveManager.getSessionToken(),
                                                et_dialog_password.getText().toString(),
                                                String.valueOf(gps.getLatitude()),
                                                String.valueOf(gps.getLongitude()));
                                    }


                                }


                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });


        dialog.show();


    }

    private void showDialogForRenameBus(){

        final Dialog dialog = new Dialog(getActivity(),
                android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_admin_reply);


        final EditText et_dialog_password = (EditText) dialog.findViewById(R.id.dialog_password);

        final Button btn_submit = (Button) dialog.findViewById(R.id.dialog_btn_submit);
        btn_submit.setEnabled(false);

        Button btn_cancel = (Button) dialog.findViewById(R.id.dialog_bnt_cancel);


        et_dialog_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() != 0) {
                    btn_submit.setEnabled(true);
                    // btn_submit.setAlpha(0f);
                } else {
                    btn_submit.setEnabled(false);
                    // btn_submit.setAlpha(0.5f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rename_bus = et_dialog_password.getText().toString();

                hitUrlForRenameBus(saveManager.getUrlEnv() + Constant.URL_RENAME_BUS,saveManager.getSessionToken(),rename_bus);

                dialog.dismiss();
            }
        });

        dialog.show();

    }


    private void hitUrlForNotifySupervisors(String url, final String sessionToken, final String message) {

        pDialog.show();
        // TODO Auto-generated method stub
        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (pDialog.isShowing()) pDialog.dismiss();

                        showDialog(1);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (pDialog.isShowing()) pDialog.dismiss();


                showDialog(0);

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                // Sentry/NotifySupervisors(string sessionId, string message)
                Map<String, String> params = new HashMap<String, String>();
                params.put("sessionId", sessionToken);
                params.put("message", message);
                params.put("siteId", RouteSelectedListAdapter.selectedId);
                // Log.d("DEBUG_selected",String.valueOf(finalBatteryPct));
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }

    private void hitUrlForRenameBus(String url, final String sessionToken, final String message) {

        pDialog.show();
        // TODO Auto-generated method stub
        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (pDialog.isShowing()) pDialog.dismiss();

                        showDialog(1);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (pDialog.isShowing()) pDialog.dismiss();


                showDialog(0);

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                // Sentry/NotifySupervisors(string sessionId, string message)
                Map<String, String> params = new HashMap<String, String>();
                params.put("sessionId", sessionToken);
                params.put("newName", message);
                // Log.d("DEBUG_selected",String.valueOf(finalBatteryPct));
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }


    private void hitUrlForNewIncident(String url, final String sessionToken, final String incident, final String lat, final String lng) {


        pDialog.show();
        // TODO Auto-generated method stub
        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (pDialog.isShowing()) pDialog.dismiss();

                        showDialog(1);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (pDialog.isShowing()) pDialog.dismiss();


                showDialog(0);

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                // Sentry/NewIncidentSubmit(string sessionId, string incident, Decimal lat, Decimal lng)
                Map<String, String> params = new HashMap<String, String>();
                params.put("sessionId", sessionToken);
                params.put("incident", incident);
                params.put("lat", lat);
                params.put("lng", lng);
                // Log.d("DEBUG_selected",String.valueOf(finalBatteryPct));
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }


    public void showDialogCustomKeyboard() {
        final Dialog dialog = new Dialog(getActivity(),
                android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_custom_keyboard);


        final int on_count = 1;
        final int off_count = 0;
        final int[] selected_field = {off_count};

        LinearLayout ll_container = (LinearLayout) dialog.findViewById(R.id.ll_container);

        ll_container.setBackgroundColor(getActivity().getResources().getColor(R.color.monsoon));


        final EditText ed_numerical_field_on = (EditText) dialog.findViewById(R.id.ed_numerical_field_on);
        ed_numerical_field_on.setInputType(EditorInfo.TYPE_NULL);

        final EditText ed_numerical_field_off = (EditText) dialog.findViewById(R.id.ed_numerical_field_off);
        ed_numerical_field_off.setInputType(EditorInfo.TYPE_NULL);


        ed_numerical_field_off.setBackgroundResource(R.drawable.rounded_edittext_green);


        final Button btn_submit = (Button) dialog.findViewById(R.id.btn_submit);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ed_numerical_field_off.getText().toString().isEmpty() && ed_numerical_field_on.getText().toString().isEmpty()) {

                    Toast.makeText(getActivity(),"All Filed Need To Fill Up",Toast.LENGTH_LONG).show();
                } else if (btn_submit.getText().toString().equalsIgnoreCase("Submit")) {
                    String oncount = ed_numerical_field_on.getText().toString();
                    String offcount = ed_numerical_field_off.getText().toString();

                    if(oncount.isEmpty())oncount = "0";
                    if(offcount.isEmpty())offcount = "0";

                    LastLocationOnly gps = new LastLocationOnly(getActivity());

                    if (gps.canGetLocation()) {
                        hitUrlForRiderCount(saveManager.getUrlEnv() +
                                        Constant.URL_RIDER_COUNT, saveManager.getSessionToken(), oncount, offcount,
                                String.valueOf(gps.getLatitude()),
                                String.valueOf(gps.getLongitude()));

                       // dialog.dismiss();
                        ed_numerical_field_off.setText("");
                        ed_numerical_field_on.setText("");

                        selected_field[0] = off_count;
                        ed_numerical_field_off.setBackgroundResource(R.drawable.rounded_edittext_green);
                        ed_numerical_field_on.setBackgroundResource(R.drawable.rounded_edittext);
                    }


                }

            }
        });
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

            }
        });


        Button btn0 = (Button) dialog.findViewById(R.id.btn_zerp);
        btn0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Button btn = (Button) v;
                String charText = btn.getText().toString();
                if (selected_field[0] == on_count) {
                    ed_numerical_field_on.setText(ed_numerical_field_on.getText().toString() + charText);
                } else {
                    ed_numerical_field_off.setText(ed_numerical_field_off.getText().toString() + charText);
                }


            }
        });
        Button btn1 = (Button) dialog.findViewById(R.id.btn_one);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button) v;
                String charText = btn.getText().toString();
                if (selected_field[0] == on_count) {
                    ed_numerical_field_on.setText(ed_numerical_field_on.getText().toString() + charText);
                } else {
                    ed_numerical_field_off.setText(ed_numerical_field_off.getText().toString() + charText);
                }


            }
        });
        Button btn2 = (Button) dialog.findViewById(R.id.btn_two);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Button btn = (Button) v;
                String charText = btn.getText().toString();
                if (selected_field[0] == on_count) {
                    ed_numerical_field_on.setText(ed_numerical_field_on.getText().toString() + charText);
                } else {
                    ed_numerical_field_off.setText(ed_numerical_field_off.getText().toString() + charText);
                }


            }
        });
        Button btn3 = (Button) dialog.findViewById(R.id.btn_three);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Button btn = (Button) v;
                String charText = btn.getText().toString();
                if (selected_field[0] == on_count) {
                    ed_numerical_field_on.setText(ed_numerical_field_on.getText().toString() + charText);
                } else {
                    ed_numerical_field_off.setText(ed_numerical_field_off.getText().toString() + charText);
                }


            }
        });
        Button btn4 = (Button) dialog.findViewById(R.id.btn_four);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Button btn = (Button) v;
                String charText = btn.getText().toString();
                if (selected_field[0] == on_count) {
                    ed_numerical_field_on.setText(ed_numerical_field_on.getText().toString() + charText);
                } else {
                    ed_numerical_field_off.setText(ed_numerical_field_off.getText().toString() + charText);
                }


            }
        });
        Button btn5 = (Button) dialog.findViewById(R.id.btn_five);
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Button btn = (Button) v;
                String charText = btn.getText().toString();
                if (selected_field[0] == on_count) {
                    ed_numerical_field_on.setText(ed_numerical_field_on.getText().toString() + charText);
                } else {
                    ed_numerical_field_off.setText(ed_numerical_field_off.getText().toString() + charText);
                }


            }
        });
        Button btn6 = (Button) dialog.findViewById(R.id.btn_six);
        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Button btn = (Button) v;
                String charText = btn.getText().toString();
                if (selected_field[0] == on_count) {
                    ed_numerical_field_on.setText(ed_numerical_field_on.getText().toString() + charText);
                } else {
                    ed_numerical_field_off.setText(ed_numerical_field_off.getText().toString() + charText);
                }


            }
        });
        Button btn7 = (Button) dialog.findViewById(R.id.btn_seven);
        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Button btn = (Button) v;
                String charText = btn.getText().toString();
                if (selected_field[0] == on_count) {
                    ed_numerical_field_on.setText(ed_numerical_field_on.getText().toString() + charText);
                } else {
                    ed_numerical_field_off.setText(ed_numerical_field_off.getText().toString() + charText);
                }


            }
        });
        Button btn8 = (Button) dialog.findViewById(R.id.btn_eight);
        btn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button) v;
                String charText = btn.getText().toString();
                if (selected_field[0] == on_count) {
                    ed_numerical_field_on.setText(ed_numerical_field_on.getText().toString() + charText);
                } else {
                    ed_numerical_field_off.setText(ed_numerical_field_off.getText().toString() + charText);
                }

            }
        });
        Button btn9 = (Button) dialog.findViewById(R.id.btn_nine);
        btn9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button) v;
                String charText = btn.getText().toString();
                if (selected_field[0] == on_count) {
                    ed_numerical_field_on.setText(ed_numerical_field_on.getText().toString() + charText);
                } else {
                    ed_numerical_field_off.setText(ed_numerical_field_off.getText().toString() + charText);
                }


            }
        });
        Button btn_erase = (Button) dialog.findViewById(R.id.btn_erase);
        btn_erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selected_field[0] == on_count) {
                    String str = ed_numerical_field_on.getText().toString();
                    if (str != null && str.length() > 0) {
                        str = str.substring(0, str.length() - 1);
                        ed_numerical_field_on.setText(str);
                    }
                } else {
                    String str = ed_numerical_field_off.getText().toString();
                    if (str != null && str.length() > 0) {
                        str = str.substring(0, str.length() - 1);
                        ed_numerical_field_off.setText(str);
                    }
                }


            }
        });




        btn_erase.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (selected_field[0] == on_count) {
                    ed_numerical_field_on.setText("");
                } else {
                    ed_numerical_field_off.setText("");
                }

                return false;
            }
        });


        ed_numerical_field_off.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                selected_field[0] = off_count;
                ed_numerical_field_off.setBackgroundResource(R.drawable.rounded_edittext_green);
                ed_numerical_field_on.setBackgroundResource(R.drawable.rounded_edittext);
                return false;
            }
        });

        ed_numerical_field_on.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                selected_field[0] = on_count;
                ed_numerical_field_off.setBackgroundResource(R.drawable.rounded_edittext);
                ed_numerical_field_on.setBackgroundResource(R.drawable.rounded_edittext_green);
                return false;
            }
        });

        dialog.show();
    }


    private void hitUrlForRiderCount(String url, final String sessionToken, final String onCount, final String offCount, final String lat, final String lng) {


        pDialog.show();
        // TODO Auto-generated method stub
        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (pDialog.isShowing()) pDialog.dismiss();

                        showDialog(1);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (pDialog.isShowing()) pDialog.dismiss();


                showDialog(0);

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                // Sentry/NewIncidentSubmit(string sessionId, string incident, Decimal lat, Decimal lng)
                Map<String, String> params = new HashMap<String, String>();
                params.put("sessionId", sessionToken);
                params.put("onCount", onCount);
                params.put("offCount", offCount);
                params.put("lat", lat);
                params.put("lng", lng);
                // Log.d("DEBUG_selected",String.valueOf(finalBatteryPct));
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }


}
