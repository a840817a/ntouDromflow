package edu.ntou.haohao.ntoudromflow;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * Implementation of App Widget functionality.
 */
public class DromflowAppWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            new updateThread(context, appWidgetManager, appWidgetId).start();
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

class updateThread extends Thread{
    ArrayList<HashMap<String, Object>> mylist;
    Context context;
    AppWidgetManager appWidgetManager;
    SharedPreferences udata;
    SharedPreferences.Editor editor;
    int next = 30;
    String userID;
    int appWidgetId;

    public updateThread(Context aaa, AppWidgetManager bbb, int ccc){
        context = aaa;
        appWidgetManager = bbb;
        appWidgetId = ccc;
        udata = context.getSharedPreferences(MainActivity.KEY, Context.MODE_PRIVATE);
        editor = udata.edit();
        int userNO1, userNO2;
        userNO1 = udata.getInt("userNO1", 0);
        userNO2 = udata.getInt("userNO2", 0);
        userID = "140.121." + userNO1 + "." + userNO2;
    }

    @Override
    public void run(){
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.dromflow_app_widget);
        try {
            dromflow ntou = new dromflow();
            mylist = ntou.getData();
            for (int i = 0; i < mylist.size(); i++) {
                if (mylist.get(i).get("IP").equals(userID)) {
                    if(Double.parseDouble((String)mylist.get(i).get("SUM"))
                            >= MainActivity.MAXmb)
                        views.setInt(R.id.appwidget, "setBackgroundResource", R.color.background_alarm);
                    else if(Double.parseDouble((String)mylist.get(i).get("SUM"))
                            >= MainActivity.MAXmb * 0.8)
                        views.setInt(R.id.appwidget, "setBackgroundResource", R.color.background_warning);
                    else
                        views.setInt(R.id.appwidget, "setBackgroundResource", R.color.background_select);
                    views.setTextViewText(R.id.appwidget_text, mylist.get(i).get("SUM") + " MB");
                    break;
                }
            }
            if(!userID.equals("140.121.0.0")) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                df.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
                editor.putString("last_update", df.format(Calendar.getInstance().getTime()));
                editor.commit();
            }
            views.setTextViewText(R.id.appwidget_error_textView, "");
            next = 600;
        } catch (Exception e) {
            e.printStackTrace();
            if(!userID.equals("140.121.0.0"))
                views.setTextViewText(R.id.appwidget_error_textView,
                        context.getString(R.string.Error_Widget) +
                                udata.getString("last_update", context.getString(R.string.No_Data)));
            next = 30;

        }
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, DromflowAppWidget.class));
        Intent intent = new Intent(context, DromflowAppWidget.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        PendingIntent pi = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + next*1000, pi);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}