package com.deltaworks.pracble.model;

/**
 * Created by kyoungae on 2018-03-11.
 */

public class DTGInfo {
    private String date;
    private String carTotalDist;
    private String carDailyDist;
    private String carSpeed;
    private String engineRpm;
    private String carBreak;
    private String carLat;
    private String carLon;
    private String carAzimuth;
    private String carSleep;
    private String dtgDeviceState;
    private String carBoot;


    public DTGInfo(String date, String carTotalDist, String carDailyDist, String carSpeed, String engineRpm, String carBreak, String carLat, String carLon, String carAzimuth, String carSleep, String dtgDeviceState, String carBoot) {
        this.date = date;
        this.carTotalDist = carTotalDist;
        this.carDailyDist = carDailyDist;
        this.carSpeed = carSpeed;
        this.engineRpm = engineRpm;
        this.carBreak = carBreak;
        this.carLat = carLat;
        this.carLon = carLon;
        this.carAzimuth = carAzimuth;
        this.carSleep = carSleep;
        this.dtgDeviceState = dtgDeviceState;
        this.carBoot = carBoot;
    }

    @Override
    public String toString() {
        return "DTGInfo{" +
                "date='" + date + '\'' +
                ", carTotalDist='" + carTotalDist + '\'' +
                ", carDailyDist='" + carDailyDist + '\'' +
                ", carSpeed='" + carSpeed + '\'' +
                ", engineRpm='" + engineRpm + '\'' +
                ", carBreak='" + carBreak + '\'' +
                ", carLat='" + carLat + '\'' +
                ", carLon='" + carLon + '\'' +
                ", carAzimuth='" + carAzimuth + '\'' +
                ", carSleep='" + carSleep + '\'' +
                ", dtgDeviceState='" + dtgDeviceState + '\'' +
                ", carBoot='" + carBoot + '\'' +
                '}';
    }

    public String toScreenString() {
        String sleepText;
        switch (carSleep) {
            case "0":
                sleepText = "정상";
                break;
            case "1":
                sleepText = "졸음";
                break;
            default:
                sleepText = carSleep;
                break;
        }
        return "정보 발생 일시='" + date + '\'' + "\n" +
                "누적 주행 거리='" + carTotalDist + '\'' + "\n" +
                "일일 주행 거리='" + carDailyDist + '\'' + "\n" +
                "차량 속도='" + carSpeed + '\'' + "\n" +
                "RPM='" + engineRpm + '\'' + "\n" +
                "브레이크='" + carBreak + '\'' + "\n" +
                "경도='" + carLon + '\'' + "\n" +
                "위도='" + carLat + '\'' + "\n" +
                "방위각='" + carAzimuth + '\'' + "\n" +
                "졸음='" + sleepText + '\'' + "\n" +
                "dtg 기기 상태='" + dtgDeviceState + '\'' + "\n" +
                "carBoot='" + carBoot + '\'';
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCarTotalDist() {
        return carTotalDist;
    }

    public void setCarTotalDist(String carTotalDist) {
        this.carTotalDist = carTotalDist;
    }

    public String getCarDailyDist() {
        return carDailyDist;
    }

    public void setCarDailyDist(String carDailyDist) {
        this.carDailyDist = carDailyDist;
    }

    public String getCarSpeed() {
        return carSpeed;
    }

    public void setCarSpeed(String carSpeed) {
        this.carSpeed = carSpeed;
    }

    public String getEngineRpm() {
        return engineRpm;
    }

    public void setEngineRpm(String engineRpm) {
        this.engineRpm = engineRpm;
    }

    public String getCarBreak() {
        return carBreak;
    }

    public void setCarBreak(String carBreak) {
        this.carBreak = carBreak;
    }

    public String getCarLat() {
        return carLat;
    }

    public void setCarLat(String carLat) {
        this.carLat = carLat;
    }

    public String getCarLon() {
        return carLon;
    }

    public void setCarLon(String carLon) {
        this.carLon = carLon;
    }

    public String getCarAzimuth() {
        return carAzimuth;
    }

    public void setCarAzimuth(String carAzimuth) {
        this.carAzimuth = carAzimuth;
    }

    public String getCarSleep() {
        return carSleep;
    }

    public void setCarSleep(String carSleep) {
        this.carSleep = carSleep;
    }

    public String getDtgDeviceState() {
        return dtgDeviceState;
    }

    public void setDtgDeviceState(String dtgDeviceState) {
        this.dtgDeviceState = dtgDeviceState;
    }

    public String getCarBoot() {
        return carBoot;
    }

    public void setCarBoot(String carBoot) {
        this.carBoot = carBoot;
    }
}
