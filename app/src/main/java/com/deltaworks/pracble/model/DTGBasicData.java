package com.deltaworks.pracble.model;

import android.bluetooth.BluetoothDevice;

import com.clj.fastble.data.BleDevice;

/**
 * Created by Administrator on 2018-04-25.
 */

public class DTGBasicData {
    private BluetoothDevice bleDevice;
    private String dtgSerialNumber;

    public DTGBasicData(BluetoothDevice bleDevice, String dtgSerialNumber) {
        this.bleDevice = bleDevice;
        this.dtgSerialNumber = dtgSerialNumber;
    }


    public BluetoothDevice getBleDevice() {
        return bleDevice;
    }

    public void setBleDevice(BluetoothDevice bleDevice) {
        this.bleDevice = bleDevice;
    }

    public String getDtgSerialNumber() {
        return dtgSerialNumber;
    }

    public void setDtgSerialNumber(String dtgSerialNumber) {
        this.dtgSerialNumber = dtgSerialNumber;
    }

    @Override
    public String toString() {
        return "DTGBasicData{" +
                "bleDevice=" + bleDevice +
                ", dtgSerialNumber='" + dtgSerialNumber + '\'' +
                '}';
    }
}
