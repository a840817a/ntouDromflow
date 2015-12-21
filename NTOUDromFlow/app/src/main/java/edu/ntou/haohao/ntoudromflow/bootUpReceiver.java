package edu.ntou.haohao.ntoudromflow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Jeffrey on 2015/12/21.
 */
public class bootUpReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Intent serviceIntent = new Intent(context, notificationService.class);
            context.startService(serviceIntent);
        }
    }
}
