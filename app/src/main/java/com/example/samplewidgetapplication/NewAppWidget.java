package com.example.samplewidgetapplication;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {

    private static final String mSharedPrefFile =
            "com.example.android.appwidgetsample";
    private static final String COUNT_KEY = "count";


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        SharedPreferences prefs = context.getSharedPreferences(
                mSharedPrefFile, 0);
        int count = prefs.getInt(COUNT_KEY + appWidgetId, 0);
        count++;

        String dateString = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date());

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        //final TextView textViewId = (TextView) findViewById(R.id.text);

        views.setTextViewText(R.id.appwidget_id, String.valueOf(appWidgetId));
        views.setTextViewText(R.id.appwidget_update,
               context.getResources().getString(
                        R.string.date_count_format, dateString));

        //Call the function that fetch the CBBI API
        getCBBIIndex(views, R.id.appwidget_id, context, appWidgetId);

        // Setup update button to send an update request as a pending intent.
        Intent intentUpdate = new Intent(context, NewAppWidget.class);

        // The intent action must be an app widget update.
        intentUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        // Include the widget ID to be updated as an intent extra.
        int[] idArray = new int[]{appWidgetId};
        intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);

        // Wrap it all in a pending intent to send a broadcast.
        // Use the app widget ID as the request code (third argument) so that
        // each intent is unique.
        PendingIntent pendingUpdate = PendingIntent.getBroadcast(context,
                appWidgetId, intentUpdate, PendingIntent.FLAG_UPDATE_CURRENT);

        // Assign the pending intent to the button onClick handler
        views.setOnClickPendingIntent(R.id.button_update, pendingUpdate);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void getCBBIIndex(RemoteViews views, int textViewid, Context context, int appWidgetId) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        //String url ="https://www.google.com";
        String url = "https://colintalkscrypto.com/cbbi/data/latest.json";

        /*
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        AppWidgetManager appMng = AppWidgetManager.getInstance(context);
                        int[] idArray = new int[]{appWidgetId};
                        views.setTextViewText(textViewid,
                                String.valueOf(200));
                        appMng.partiallyUpdateAppWidget(idArray, views);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                views.setTextViewText(textViewid,
                        String.valueOf(500));
            }
        });*/

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST,url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response);
                        Calendar currentTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                        currentTime.set(Calendar.HOUR_OF_DAY, 0);
                        currentTime.set(Calendar.MINUTE, 0);
                        currentTime.set(Calendar.SECOND, 0);
                        currentTime.set(Calendar.MILLISECOND, 0);
                        long currentTime_long = currentTime.getTimeInMillis() / 1000;
                        // Convert String to json object
                        try {
                            JSONObject json = response.getJSONObject("Confidence");
                            double confidence_value_of_today = json.getDouble(String.valueOf(currentTime_long));
                            System.out.println(confidence_value_of_today);
                            AppWidgetManager appMng = AppWidgetManager.getInstance(context);
                            int[] idArray = new int[]{appWidgetId};
                            views.setTextViewText(textViewid,
                                    String.valueOf(confidence_value_of_today));
                            appMng.partiallyUpdateAppWidget(idArray, views);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error);
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(jsObjRequest);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}