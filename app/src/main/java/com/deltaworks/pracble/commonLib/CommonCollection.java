package com.deltaworks.pracble.commonLib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by Administrator on 2018-03-14.
 */

public class CommonCollection {

    public final String TAG = CommonCollection.class.getSimpleName();
    public Context context;

    public CommonCollection(Context context) {
        this.context = context;
    }

    /**
     * 인터넷 연결 확인 메소드
     * 연결돼있으면 true, 연결 안돼 있으면 false 리턴
     * 네트워크 확인 종류는 mobile. wifi, wimax <<  태블릿
     *
     * @return
     */
    public int checkNetwork() {
        int networkType = 0;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  // 인터넷 연결 상태 매니저
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            int networkInfoType = networkInfo.getType();
            switch (networkInfoType) {
                case ConnectivityManager.TYPE_MOBILE:
                    if (networkInfo.isConnected()) {
                        Log.d(TAG, "checkNetwork: TYPE_MOBILE");
                        networkType = 2;
                    }
                    break;
                case ConnectivityManager.TYPE_WIFI:
                    if (networkInfo.isConnected()) {
                        Log.d(TAG, "checkNetwork: TYPE_WIFI");
                        networkType = 1;
                    }
                    break;

                case ConnectivityManager.TYPE_WIMAX:
                    if (networkInfo.isConnected()) {
                        Log.d(TAG, "checkNetwork: TYPE_WIMAX");
                        networkType = 1;
                    }
                    break;
            }
        }
        return networkType;
    }


    /**
     * 거리 측정
     */
    public double MeasureDistance(String carSpeed) {

        String zeroDeleteCarSpeed;
        int lastZeroIndex = carSpeed.indexOf("0");  //앞의 3자리 중에 마지막 0이 있는 index
        String firstZeroDeleteCarSpeed = carSpeed.substring(lastZeroIndex + 1);  //앞의 3자리 중에 마지막 0이 있는 자리 삭제된 text값

        if (firstZeroDeleteCarSpeed.substring(0,0).equals("0")){  //두번째자리도 0이면
            String secondZeroDeleteCarSpeed = firstZeroDeleteCarSpeed.substring(1);
            zeroDeleteCarSpeed = secondZeroDeleteCarSpeed;
        }else {
            zeroDeleteCarSpeed = firstZeroDeleteCarSpeed;
        }

//        Log.d(TAG, "MeasureDistance: " + zeroDeleteCarSpeed);
        //거리 = 속도 * 시간
        double distance = 0;
        if (zeroDeleteCarSpeed != null) {
            double dDistance = Integer.parseInt(zeroDeleteCarSpeed) / 3.6;
            distance = Double.parseDouble(String.format("%.2f", dDistance));
        }

        return distance;

    }

//        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null) {
//            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
//                //데이터 보내기
//                networkType = 1;
//            } else {
//                Log.d(TAG, "checkNetwork: wifi null");
//            }
//        } else if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null) {
//            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()) {
//                networkType = 2;
//            } else {
//                Log.d(TAG, "checkNetwork: mobile null");
//            }
//        }
//        return networkType;  //연결 안돼있으면 0


//    public void killStartScanThread() {
//        if (mStartScanThread != null && mStartScanRunnable != null) {
//            try {
//                mStartScanThread.interrupt();
//                mStartScanThread.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }


}
