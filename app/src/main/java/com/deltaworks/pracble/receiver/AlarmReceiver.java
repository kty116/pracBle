package com.deltaworks.pracble.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.deltaworks.pracble.event.AlarmReceiverEvent;
import com.deltaworks.pracble.service.MainService;

import org.greenrobot.eventbus.EventBus;

/**
 * 알람 등록 리시버
 */

public class AlarmReceiver extends BroadcastReceiver {//service로 가는 리시버
    public static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();

            switch (action) {
                case MainService.ACTION_ALARM_DTG_DATA:
                    Log.d(TAG, "ACTION_ALARM_DTG_DATA onReceive: ");
                    EventBus.getDefault().post(new AlarmReceiverEvent(MainService.ACTION_ALARM_DTG_DATA));
                    break;

                case MainService.ACTION_ALARM_DTG_LOCATION:
                    Log.d(TAG, "ACTION_ALARM_DTG_LOCATION onReceive: ");
                    EventBus.getDefault().post(new AlarmReceiverEvent(MainService.ACTION_ALARM_DTG_LOCATION));
                    break;

//            case MainService.ACTION_ALARM_CHECK_FOR_CHANGED_DATA:
//                Log.d(TAG, "ACTION_ALARM_CHECK_FOR_CHANGED_DATA onReceive: ");
//                EventBus.getDefault().post(new AlarmReceiverEvent(MainService.ACTION_ALARM_CHECK_FOR_CHANGED_DATA));
//                break;
            }

        }
    }
}
