package com.withcamp.soma6.navirella;

/**
 * Created by jiyoungpark on 15. 8. 30..
 */
public class PointFactory {
    private static double start_longitude;
    private static double start_latitude;
    private static double end_longitude;
    private static double end_latitude;

    public static double getStart_longitude() {
        return start_longitude;
    }

    public static void setStart_longitude(double start_longitude) {
        PointFactory.start_longitude = start_longitude;
    }

    public static double getStart_latitude() {
        return start_latitude;
    }

    public static void setStart_latitude(double start_latitude) {
        PointFactory.start_latitude = start_latitude;
    }

    public static double getEnd_longitude() {
        return end_longitude;
    }

    public static void setEnd_longitude(double end_longitude) {
        PointFactory.end_longitude = end_longitude;
    }

    public static double getEnd_latitude() {
        return end_latitude;
    }

    public static void setEnd_latitude(double end_latitude) {
        PointFactory.end_latitude = end_latitude;
    }
}
