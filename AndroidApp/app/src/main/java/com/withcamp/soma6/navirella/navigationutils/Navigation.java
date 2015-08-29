package com.withcamp.soma6.navirella.navigationutils;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JaeyeonLee on 2015. 8. 29..
 */
public class Navigation {

    private static final String TAG = "NavigationUtil";
    public static final int ACTION_STRAIGHT = 11;
    public static final int ACTION_LEFT = 12;
    public static final int ACTION_RIGHT = 13;
    public static final int ACTION_ARRIVED = 201;
    public static final int ACTION_START = 200;

    private static Point startPoint;
    private static Point curTargetPoint;
    private static int currentType;
    private static Point endPoint;

    private static NavigationTask navigationTask;
    static PathInfo pathInfo;
    private String rawPathInfo;

    public Navigation() {
        startPoint = new Point();
        curTargetPoint = new Point();
        endPoint = new Point();
        navigationTask = new NavigationTask();
        pathInfo = new PathInfo();
        rawPathInfo = "";
    }

    //getters & setters
    public static Point getCurTargetPoint() {
        return curTargetPoint;
    }

    public static void setCurTargetPoint(Point curTargetPoint) {
        Navigation.curTargetPoint = curTargetPoint;
    }

    public static Point getStartPoint() {
        return startPoint;
    }

    public static void setStartPoint(Point startPoint) {
        Navigation.startPoint = startPoint;
    }

    public static Point getEndPoint() {
        return endPoint;
    }

    public static void setEndPoint(Point endPoint) {
        Navigation.endPoint = endPoint;
    }

    public String getRawPathInfo() {
        return rawPathInfo;
    }

    // 시작 포인트 설정
    public static void setStartPoint(double longitude, double latitude) {
        startPoint = new Point();
        startPoint.setLongitude(longitude);
        startPoint.setLatitude(latitude);
        startPoint.setFlag(true);
    }

    // 도착 포인트 설정
    public static void setEndPoint(double longitude, double latitude) {
        endPoint = new Point();
        endPoint.setLongitude(longitude);
        endPoint.setLatitude(latitude);
        endPoint.setFlag(true);
    }

    // 경로 정보 설정
    public void setPath() {
        if (startPoint.isFlag() && endPoint.isFlag()) {
            navigationTask = new NavigationTask();
            navigationTask.execute();
        }
    }

    // 거리 계산
    private static double calDistance (Point curPoint) {
        return Math.sqrt(Math.pow(Math.abs(curPoint.getLongitude()-curTargetPoint.getLongitude()), 2) + Math.pow(Math.abs(curPoint.getLatitude() - curTargetPoint.getLatitude()), 2));
    }

    /***
     * 현재 위치의 상태 확인
     * 출발 / 직진 경로 / 회전 포인트 근접
     * 상황에 따라 다음 목표지점(curTargetPoint) 갱신
     * gps가 갱신될 때마다 이 method를 실행!
     *
     * @param longitude
     * @param latitude
     */
    public static int checkCurrentLocation (double longitude, double latitude, double dd) {

        if (currentType == ACTION_START) {
            // 출발
            Log.i(TAG, "gogo: START");
            currentType = ACTION_STRAIGHT;
            pathInfo.addPointIndex();
            setCurTargetPoint(pathInfo.getCurTargetPoint());
            return sendCommandToUmbrella(currentType);

        }
        else if (currentType == ACTION_ARRIVED) {
            // 도착
            Log.i(TAG, "gogo: FIN");
            return sendCommandToUmbrella(currentType);
        }

        else {
            double distance = calDistance(new Point(longitude, latitude));
            distance = dd;

            if (distance < /*2.5E-4*/5) {
                if (currentType == ACTION_STRAIGHT) {
                    // 목표 지점에 도달한 시점.
                    currentType = curTargetPoint.getTurntype();
                }
                else {
                    // 회전을 아직 하지 않은 상태. 계속 회전하라고 명령
                }
            }
            else {
                if (currentType != ACTION_STRAIGHT) {
                    // 회전 포인트에서 회전하여 벗어남
                    // 새로운 목표 지점 설정
                    pathInfo.addPointIndex();
                    setCurTargetPoint(pathInfo.getCurTargetPoint());
                    currentType = ACTION_STRAIGHT;
                }
                else {
                    // 직진 도중. 계속 직진
                }
            }
            return sendCommandToUmbrella(currentType);
        }
    }

    public static int testAction(int index) {
        switch(index) {
            case 0:
                return checkCurrentLocation(0.0, 0.0, 10);  // 시작
            case 1:
                return checkCurrentLocation(0.0, 0.0, 6);  // 직진
            case 2:
                return checkCurrentLocation(0.0, 0.0, 4);  // 턴
            case 3:
                return checkCurrentLocation(0.0, 0.0, 6);  // 직진
            case 4:
                return checkCurrentLocation(0.0, 0.0, 3);  // 턴
            case 5:
                return checkCurrentLocation(0.0, 0.0, 6);   // 직진
            case 6:
                return checkCurrentLocation(0.0, 0.0, 3);   // 도착
            default:
                return 0;
        }
    }

    public static int sendCommandToUmbrella(int action) {
        switch (action) {
            case ACTION_STRAIGHT:
                Log.i(TAG, "gogo: go straight");
                break;
            case ACTION_LEFT:
                Log.i(TAG, "gogo: turn left");
                break;
            case ACTION_RIGHT:
                Log.i(TAG, "gogo: turn right");
                break;
            case ACTION_ARRIVED:
                Log.i(TAG, "gogo: arrived!");
                break;
            default:
                Log.e(TAG, "Wrong Command");
                break;
        }
        return action;
    }

    class NavigationTask extends AsyncTask<Double, Void, String> {

        private static final String TAG = "NavigationTask";

        @Override
        protected String doInBackground(Double... params) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("https://apis.skplanetx.com/tmap/routes/pedestrian?callback=&bizAppId=&version=1");
                httpPost.addHeader("x-skpop-userId", "jyegglee@gmail.com");
                httpPost.addHeader("Accept-Language", "ko_KR");
                httpPost.addHeader("appKey", "d772af81-2863-3c54-a8c4-c721c7a8d050");

                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(8);

                nameValuePair.add(new BasicNameValuePair("startX", Double.toString(Navigation.getStartPoint().getLongitude())));
                nameValuePair.add(new BasicNameValuePair("startY", Double.toString(Navigation.getStartPoint().getLatitude())));
                nameValuePair.add(new BasicNameValuePair("endX", Double.toString(Navigation.getEndPoint().getLongitude())));
                nameValuePair.add(new BasicNameValuePair("endY", Double.toString(Navigation.getEndPoint().getLatitude())));
                nameValuePair.add(new BasicNameValuePair("reqCoordType", "WGS84GEO"));
                nameValuePair.add(new BasicNameValuePair("resCoordType", "WGS84GEO"));
                nameValuePair.add(new BasicNameValuePair("startName", "startPoint"));
                nameValuePair.add(new BasicNameValuePair("endName", "endPoint"));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
//
//            Header[] h = httpPost.getAllHeaders();
//            for (Header hd : h) {
//                Log.d("resquenseHeader", hd.toString());
//            }

                HttpResponse httpResponse = httpClient.execute(httpPost);
                Log.d("responseStatusCode", httpResponse.getStatusLine().getStatusCode() + "");
//            h = httpResponse.getAllHeaders();
//            for (Header hd : h) {
//                Log.d("responseHeader", hd.toString());
//            }

                if (httpResponse.getStatusLine().getStatusCode() != 200)    {
                    return Integer.toString(httpResponse.getStatusLine().getStatusCode());
                }
                else {
                    InputStream is = httpResponse.getEntity().getContent();
                    StringBuilder stringBuilder = new StringBuilder();
                    byte[] b = new byte[4096];
                    for (int n; (n = is.read(b)) != -1;) {
                        stringBuilder.append(new String(b, 0, n));
                    }
                    return stringBuilder.toString();
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, e.getLocalizedMessage());
            } catch (ClientProtocolException e) {
                Log.e(TAG, e.getLocalizedMessage());
            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage());
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            rawPathInfo = s;
            Log.i(TAG, rawPathInfo);
            pathInfo = new PathInfo();
            try {
                JSONObject rawInfo = new JSONObject(s);
                JSONArray features = rawInfo.getJSONArray("features");
                int pointNum = 0;
                for (int i=0; i<features.length(); i++) {
                    JSONObject geo = features.getJSONObject(i).getJSONObject("geometry");
                    if (!geo.getString("type").equalsIgnoreCase("Point")) continue;
                    else {
                        JSONObject properties = features.getJSONObject(i).getJSONObject("properties");
                        JSONArray coordinate = geo.getJSONArray("coordinates");
                        int turntype;
                        if (properties.has("time")) turntype = 11;
                        else turntype = properties.getInt("turnType");
                        pathInfo.addTurnPoint(pointNum, coordinate.getDouble(0), coordinate.getDouble(1), turntype);
                        pointNum++;
                    }
                }
                pathInfo.setPointIndexNum(pointNum);
                setCurTargetPoint(pathInfo.getCurTargetPoint());
                currentType = 200;
                Log.i(TAG, "there are "+ pointNum+"points in path");

            } catch(JSONException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }
}
