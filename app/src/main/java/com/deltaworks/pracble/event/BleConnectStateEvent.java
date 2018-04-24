package com.deltaworks.pracble.event;

import android.bluetooth.BluetoothDevice;

/**
 * 블루투스 기기와 연결 됬을때 프래그먼트에 알려주는 이벤트
 */

public class BleConnectStateEvent implements Event {
    private boolean isConnected;
    private BluetoothDevice device;

    public BleConnectStateEvent(boolean isConnected, BluetoothDevice device) {
        this.isConnected = isConnected;
        this.device = device;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }
}
