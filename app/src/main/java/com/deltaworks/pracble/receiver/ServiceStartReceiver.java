package com.deltaworks.pracble.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.deltaworks.pracble.service.MainService;

/**
 * Created by Administrator on 2018-05-02.
 */

public class ServiceStartReceiver extends BroadcastReceiver {

    public static final String TAG = ServiceStartReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();

            switch (action) {
                case MainService.ACTION_ALARM_BOOT_HANDPHONE:
                    Intent intent1 = new Intent(context, MainService.class);
                    intent1.setAction(MainService.ACTION_START_CONNECT);
                    context.startService(intent1);
                    break;
            }
        }
    }
}
