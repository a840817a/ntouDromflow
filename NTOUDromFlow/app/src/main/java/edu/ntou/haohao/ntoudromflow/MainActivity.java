package edu.ntou.haohao.ntoudromflow;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.HttpStatusException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    static ArrayList<HashMap<String, Object>> mylist;
    private ProgressDialog pd;
    public static final int MAXmb = 5000 ;
    String userID;
    int userNO1, userNO2, warnNO;
    ListView list;
    public static final String KEY = "uDATA";
    SharedPreferences udata;
    SharedPreferences.Editor editor;
    Thread getData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = (ListView) findViewById(R.id.listView);

        udata = getApplication().getSharedPreferences(KEY, Context.MODE_PRIVATE);
        editor = udata.edit();

        userNO1 = udata.getInt("userNO1", 0);
        userNO2 = udata.getInt("userNO2", 0);
        warnNO = udata.getInt("warnNO", 0);

        Button setb = (Button)findViewById(R.id.SETbutton);
        setb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(MainActivity.this, set.class);
                Bundle bundle = new Bundle();
                bundle.putInt("userNO1",userNO1);
                bundle.putInt("userNO2",userNO2);
                bundle.putInt("warnNO",warnNO);
                intent.putExtras(bundle);

                startActivityForResult(intent, 0);
            }
        });

        Button refresh = (Button)findViewById(R.id.REFRESHbutton);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                pd = ProgressDialog.show(MainActivity.this, getString(R.string.Loading), getString(R.string.Wait));
                getData = new  Thread(set);
                getData.start();
            }
        });

        if (mylist == null) {
            pd = ProgressDialog.show(MainActivity.this, getString(R.string.Loading), getString(R.string.Wait));
            getData = new  Thread(set);
            getData.start();
        }
        else {
            findIP();
        }

        Intent intent = new Intent(this,notificationService.class);
        startService(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            userNO1 = bundle.getInt("userNO1");
            userNO2 = bundle.getInt("userNO2");
            warnNO = bundle.getInt("warnNO");
            editor.putInt("userNO1", userNO1);
            editor.putInt("userNO2", userNO2);
            editor.putInt("warnNO", warnNO);
            editor.commit();
            if (mylist != null)
                findIP();

            AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
            ComponentName widgetComponent = new ComponentName(this, DromflowAppWidget.class);
            int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);
            Intent update = new Intent(this, DromflowAppWidget.class);
            update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
            update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            sendBroadcast(update);

            Intent intent = new Intent(this,notificationService.class);
            Bundle sBundle = new Bundle();
            sBundle.putBoolean("new", true);
            intent.putExtras(sBundle);
            startService(intent);
        }
    }

    Runnable set = new Runnable() {
        boolean retry;
        public void run()
        {
            retry = true;
            while (retry) {
                try {
                    dromflow ntou = new dromflow();
                    mylist = ntou.getData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findIP();
                        }
                    });
                    retry = false;
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                    showError(R.string.Error_TimeOut);
                } catch (HttpStatusException e) {
                        e.printStackTrace();
                        showError(R.string.Error_HTTP);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    showError(R.string.Error_HTTP);
                } catch (IOException e) {
                    e.printStackTrace();
                    showError(R.string.Error_Other);
                }
                finally {
                    if(!retry)
                        pd.dismiss();
                }
            }
        }

        private void showError (final int messageID){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.Error)
                            .setMessage(messageID)
                            .setNegativeButton(R.string.Ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    retry = false;
                                    synchronized (getData) {
                                        getData.notify();
                                    }
                                }
                            })
                            .setPositiveButton(R.string.Retry, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    retry = true;
                                    synchronized (getData) {
                                        getData.notify();
                                    }
                                }
                            })
                            .show();
                }
            });
            try {
                synchronized (getData) {
                    getData.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    protected void findIP(){
        userID = "140.121." + userNO1 + "." + userNO2;
        list.setAdapter(new MyAdapter());
        for(int i=0; i<mylist.size(); i++) {
            if(mylist.get(i).get("IP").equals(userID)) {
                list.setSelectionFromTop(i, 150);
                break;
            }
        }
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mylist.size();
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
            View v = convertView;
            Holder holder;
            if(v == null){
                v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.list, null);
                holder = new Holder();
                holder.ip = (TextView) v.findViewById(R.id.IPtextView);
                holder.sum = (TextView) v.findViewById(R.id.SUMtextView);
                holder.in = (TextView) v.findViewById(R.id.INtextView);
                holder.out = (TextView) v.findViewById(R.id.OUTtextView);
                v.setTag(holder);
            } else
                holder = (Holder) v.getTag();

            holder.ip.setText((String) mylist.get(position).get("IP"));
            holder.sum.setText((String) mylist.get(position).get("SUM"));
            holder.in.setText((String) mylist.get(position).get("IN"));
            holder.out.setText((String) mylist.get(position).get("OUT"));

            if ((Double.parseDouble((String)mylist.get(position).get("SUM")) >= MAXmb))
                holder.sum.setBackgroundColor(getResources().getColor(R.color.background_alarm));
            else if ((Double.parseDouble((String)mylist.get(position).get("SUM")) >= MAXmb * 0.8))
                holder.sum.setBackgroundColor(getResources().getColor(R.color.background_warning));
            else
                holder.sum.setBackgroundColor(Color.argb(0, 0, 255, 0));

            if (mylist.get(position).get("IP").equals(userID))
                holder.ip.setBackgroundColor(getResources().getColor(R.color.background_select));
            else
                holder.ip.setBackgroundColor(Color.argb(0, 0, 255, 0));

            return v;
        }

        class Holder{
            TextView ip;
            TextView sum;
            TextView in;
            TextView out;
        }
    }
}