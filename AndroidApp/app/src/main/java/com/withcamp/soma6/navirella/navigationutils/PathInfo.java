package com.withcamp.soma6.navirella.navigationutils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JaeyeonLee on 2015. 8. 30..
 */
public class PathInfo {

    private int pointIndexNum;
    private int pointIndex = 0;
    private List<Point> pointArray;

    public int getPointIndexNum() {
        return pointIndexNum;
    }

    public void setPointIndexNum(int pointIndexNum) {
        this.pointIndexNum = pointIndexNum;
    }

    public int getPointIndex() {
        return pointIndex;
    }

    public void addPointIndex() {
        pointIndex++;
    }

    public void setPointIndex(int pointIndex) {
        this.pointIndex = pointIndex;
    }

    public void addTurnPoint(int index, double longitude, double latitude, int turntype) {
        Point newPoint = new Point(longitude, latitude, turntype);
        if (index == 0)
            pointArray = new ArrayList<Point>();
        pointArray.add(index, newPoint);
    }

    public Point getCurTargetPoint() {
        return pointArray.get(pointIndex);
    }
}
