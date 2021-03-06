package edu.ntou.haohao.ntoudromflow;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

public class notificationService extends Service {
    ArrayList<HashMap<String, Object>> mylist;
    SharedPreferences udata;
    SharedPreferences.Editor editor;
    int userNO1, userNO2, warnNO;
    Thread getData;
    String userID, myDate;
    NotificationCompat.Builder builder;
    NotificationCompat.BigTextStyle bigtext;
    NotificationManager manager;
    SimpleDateFormat df;
    boolean warn1, warn2;
    final static double warnCode[] = {0, 0.3, 0.35, 0.4, 0.8, 0.85, 0.9, 0.95};

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this);
        int defaults = 0;
            defaults |= Notification.DEFAULT_VIBRATE;
            defaults |= Notification.DEFAULT_SOUND;
            defaults |= Notification.DEFAULT_LIGHTS;
        bigtext = new NotificationCompat.BigTextStyle();
        builder.setSmallIcon(R.drawable.ic_swap_vertical_circle_black_36dp)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getString(R.string.app_name))
                .setDefaults(defaults)
                .setStyle(bigtext);

        udata = getApplication().getSharedPreferences(MainActivity.KEY, Context.MODE_PRIVATE);
        editor = udata.edit();


        getData = new Thread(set);

        df = new SimpleDateFormat("yyyy/MM/dd");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        warnNO = udata.getInt("warnNO", 0);
        if(warnNO == 0)
            stopSelf();
        userNO1 = udata.getInt("userNO1", 0);
        userNO2 = udata.getInt("userNO2", 0);
        userID = "140.121." + userNO1 + "." + userNO2;
        warn1 = udata.getBoolean("warn1", false);
        warn2 = udata.getBoolean("warn2", false);
        myDate = udata.getString("myDate", df.format(Calendar.getInstance().getTime()));


        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.getBoolean("new", false)){
            warn1 = false;
            warn2 = false;
        }

        if(!myDate.equals(df.format(Calendar.getInstance().getTime())))
        {
            myDate = df.format(Calendar.getInstance().getTime());
            warn1 = false;
            warn2 = false;
        }

        if(warnNO != 0 && !getData.isAlive())
            getData.start();

        return super.onStartCommand(intent, flags, startId);
    }

    Runnable set = new Runnable() {
        public void run()
        {
            Calendar next;
            int nexts = 30;
            try {
                dromflow ntou = new dromflow();
                mylist = ntou.getData();
                for (int i = 0; i < mylist.size(); i++) {
                    if (mylist.get(i).get("IP").equals(userID)) {
                        if(!warn2 && Double.parseDouble((String)mylist.get(i).get("SUM"))
                                >= MainActivity.MAXmb) {
                            builder.setContentText(getString(R.string.Warning_max));
                            manager.notify(1, builder.build());
                            warn2 = true;
                        }else if(!warn1 && Double.parseDouble((String)mylist.get(i).get("SUM"))
                                >= MainActivity.MAXmb * warnCode[warnNO]) {
                            builder.setContentText(getString(R.string.Warning,
                                    Double.parseDouble((String)mylist.get(i).get("SUM")),
                                    5000 - Double.parseDouble((String)mylist.get(i).get("SUM"))));
                            bigtext.bigText(getString(R.string.Warning,
                                    Double.parseDouble((String)mylist.get(i).get("SUM")),
                                    5000 - Double.parseDouble((String)mylist.get(i).get("SUM"))));
                            manager.notify(1, builder.build());
                            warn1 = true;
                        }
                        break;
                    }
                }
                if(warn2) {
                    next = Calendar.getInstance();
                    next.add(Calendar.DATE, 1);
                    next.set(Calendar.HOUR_OF_DAY, 0);
                    next.set(Calendar.MINUTE, 12);
                    next.set(Calendar.SECOND, 0);
                    alarmNextTime(next);
                }
                else {
                    nexts = 600;
                    alarmNextTime(nexts);
                }
            } catch (Exception e) {
                e.printStackTrace();
                nexts = 30;
                alarmNextTime(nexts);
            }

            stopSelf();
        }

        void alarmNextTime(Calendar cal){
            Intent intent = new Intent(getApplicationContext(), alarmReceiver.class);
            intent.putExtra("msg", "check_dromflow");

            PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(),
                    1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        }

        void alarmNextTime(int time){
            Intent intent = new Intent(getApplicationContext(), alarmReceiver.class);
            intent.putExtra("msg", "check_dromflow");

            PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(),
                    1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + time*1000, pi);
        }
    };

    @Override
    public void onDestroy (){
        if(getData.isAlive()) {
            try {
                getData.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        editor.putBoolean("warn1", warn1);
        editor.putBoolean("warn2", warn2);
        editor.putString("myDate", myDate);
        editor.commit();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
