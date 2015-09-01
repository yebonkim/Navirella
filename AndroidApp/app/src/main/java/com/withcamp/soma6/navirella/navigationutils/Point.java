package com.withcamp.soma6.navirella.navigationutils;

/**
 * Created by JaeyeonLee on 2015. 8. 29..
 */
public class Point {
    private double longitude;
    private double latitude;
    private int turntype;
    private boolean flag = false;

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public Point(double longitude, double latitude) {
        this.setLongitude(longitude);
        this.setLatitude(latitude);
        this.setTurntype(0);
        this.flag = true;
    }

    public Point(double longitude, double latitude, int turntype) {
        this.setLongitude(longitude);
        this.setLatitude(latitude);
        this.setTurntype(turntype);
        this.flag = true;
    }

    public Point() {
        this.setLongitude(0);
        this.setLatitude(0);
        this.setTurntype(0);
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getTurntype() {
        return turntype;
    }

    public void setTurntype(int turntype) {
        this.turntype = turntype;
    }
}
