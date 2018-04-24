package com.deltaworks.pracble.model;

/**
 * Created by Administrator on 2018-03-08.
 */

public class SettingInfo {
    private long alarmTime;


    public SettingInfo(long alarmTime) {
        this.alarmTime = alarmTime;
    }

    public long getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(long alarmTime) {
        this.alarmTime = alarmTime;
    }
}
