package edu.ntou.haohao.ntoudromflow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Jeffrey on 2015/12/22.
 */
public class alarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getExtras().get("msg").equals("check_dromflow")){
            Intent serviceIntent = new Intent(context, notificationService.class);
            context.startService(serviceIntent);
        }
    }
}