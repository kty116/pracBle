package com.deltaworks.pracble.model;

import com.clj.fastble.data.BleDevice;

/**
 * Created by Administrator on 2018-04-25.
 */

public class DTGBasicData {
    private BleDevice bleDevice;
    private String dtgSerialNumber;

    public DTGBasicData(BleDevice bleDevice, String dtgSerialNumber) {
        this.bleDevice = bleDevice;
        this.dtgSerialNumber = dtgSerialNumber;
    }

    public BleDevice getBleDevice() {
        return bleDevice;
    }

    public void setBleDevice(BleDevice bleDevice) {
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
