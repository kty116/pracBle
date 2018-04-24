package com.deltaworks.pracble.db;

import android.provider.BaseColumns;

/**
 * Created by Administrator on 2018-02-28.
 */

public class DTGContract {

    public static abstract class DTGDataEntry implements BaseColumns {
        public static final String DTG_TABLE_NAME = "DTG";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_CAR_TOTAL_DIST = "total_dist";
        public static final String COLUMN_NAME_CAR_DAILY_DIST = "daily_dist";
        public static final String COLUMN_NAME_CAR_SPEED = "car_speed";
        public static final String COLUMN_NAME_ENGINE_RPM = "engine_rpm";
        public static final String COLUMN_NAME_CAR_BREAK = "car_break";
        public static final String COLUMN_NAME_CAR_LAT = "car_lat";
        public static final String COLUMN_NAME_CAR_LON = "car_lon";
        public static final String COLUMN_NAME_CAR_SLEEP = "car_sleep";
        public static final String COLUMN_NAME_CAR_AZIMUTH = "car_azimuth";
        public static final String COLUMN_NAME_DTG_DEVICE_STATE = "dtg_device_state";
        public static final String COLUMN_NAME_CAR_BOOT = "car_boot";

        public static final String DTG_ENTRIES = "CREATE TABLE " + DTG_TABLE_NAME+
                "("+_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                COLUMN_NAME_DATE + " TEXT," +
                COLUMN_NAME_CAR_TOTAL_DIST + " TEXT," +
                COLUMN_NAME_CAR_DAILY_DIST + " TEXT," +
                COLUMN_NAME_CAR_SPEED+ " TEXT," +
                COLUMN_NAME_ENGINE_RPM+ " TEXT," +
                COLUMN_NAME_CAR_BREAK + " TEXT," +
                COLUMN_NAME_CAR_LAT+ " TEXT," +
                COLUMN_NAME_CAR_LON+ " TEXT," +
                COLUMN_NAME_CAR_SLEEP+ " TEXT," +
                COLUMN_NAME_CAR_AZIMUTH+ " TEXT," +
                COLUMN_NAME_DTG_DEVICE_STATE + " TEXT," +
                COLUMN_NAME_CAR_BOOT+ " TEXT);";

    }
}
