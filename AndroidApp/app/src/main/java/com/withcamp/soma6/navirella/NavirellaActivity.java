package com.withcamp.soma6.navirella;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.withcamp.soma6.navirella.navigationutils.Navigation;
import com.withcamp.soma6.navirella.navigationutils.Point;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NavirellaActivity extends AppCompatActivity implements MapView.MapViewEventListener, MapView.POIItemEventListener {

    private static final String TAG = "NavirellaActivity";
    private static final String LOG_TAG = "SearchDemoActivity";

    private EditText editText_startpoint, editText_endpoint;
    private Button btn_findstart, btn_findend, btn_findPath;

    private Navigation navigation;
    private com.withcamp.soma6.navirella.BluetoothService btService;
    private Handler mHandler;
    private TimerTask timerTask;
    private HashMap<Integer, Item> mTagItemMap = new HashMap<Integer, Item>();

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private MapView mapView;

    public static int index = 0;
    private int tag = 0;

    private MapPOIItem startMarker, endMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navirella);

        editText_startpoint = (EditText) findViewById(R.id.editText_startpoint);
        editText_endpoint = (EditText) findViewById(R.id.editText_endpoint);

        btn_findstart = (Button) findViewById(R.id.btn_findstart);
        btn_findend = (Button) findViewById(R.id.btn_findend);

        btn_findPath = (Button) findViewById(R.id.btn_findpath);

        mapView = (MapView) findViewById(R.id.map_view);

        mapView.setDaumMapApiKey(MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY);
        mapView.setMapViewEventListener(this);
        mapView.setPOIItemEventListener(this);
        mapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());

        final GpsInfo gps = new GpsInfo(NavirellaActivity.this);

        mHandler = new Handler() {

            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        timerTask = new TimerTask() {
            @Override
            public void run() {

                int command = navigation.commandToNavi(gps.getLocation().getLongitude(), gps.getLocation().getLatitude());
                Log.d(TAG, String.valueOf(gps.getLocation().getLongitude()) + " / " + String.valueOf(gps.getLocation().getLatitude()));
                sendCommandAction(command);
            }
        };

        btService = new BluetoothService(NavirellaActivity.this, mHandler);


        startConnection();

        navigation = new Navigation();

        // 출발지 설정
        btn_findstart.setOnClickListener(new View.OnClickListener() { // 검색버튼 클릭 이벤트 리스너
            @Override
            public void onClick(View v) {
                String query = editText_startpoint.getText().toString();
                if (query == null || query.length() == 0) {
                    showToast("검색어를 입력하세요.");
                    return;
                }
                hideSoftKeyboard(); // 키보드 숨김
                tag = 1;
                MapPoint.GeoCoordinate geoCoordinate = mapView.getMapCenterPoint().getMapPointGeoCoord();
                double latitude = geoCoordinate.latitude; // 위도
                double longitude = geoCoordinate.longitude; // 경도
                int radius = 10000; // 중심 좌표부터의 반경거리. 특정 지역을 중심으로 검색하려고 할 경우 사용. meter 단위 (0 ~ 10000)
                int page = 1; // 페이지 번호 (1 ~ 3). 한페이지에 15개
                String apikey = MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY;

                Searcher searcher = new Searcher(); // net.daum.android.map.openapi.search.Searcher
                searcher.searchKeyword(getApplicationContext(), query, latitude, longitude, radius, page, apikey, new OnFinishSearchListener() {
                    @Override
                    public void onSuccess(List<Item> itemList) {
                        mapView.removeAllPOIItems(); // 기존 검색 결과 삭제
                        showResult(itemList); // 검색 결과 보여줌
                    }
                    @Override
                    public void onFail() {
                        showToast("API_KEY의 제한 트래픽이 초과되었습니다.");
                    }
                });
            }
        });

        // 도착지 설정
        btn_findend.setOnClickListener(new View.OnClickListener() { // 검색버튼 클릭 이벤트 리스너
            @Override
            public void onClick(View v) {
                String query = editText_endpoint.getText().toString();
                if (query == null || query.length() == 0) {
                    showToast("검색어를 입력하세요.");
                    return;
                }
                hideSoftKeyboard(); // 키보드 숨김
                tag = 2;
                MapPoint.GeoCoordinate geoCoordinate = mapView.getMapCenterPoint().getMapPointGeoCoord();
                double latitude = geoCoordinate.latitude; // 위도
                double longitude = geoCoordinate.longitude; // 경도
                int radius = 10000; // 중심 좌표부터의 반경거리. 특정 지역을 중심으로 검색하려고 할 경우 사용. meter 단위 (0 ~ 10000)
                int page = 1; // 페이지 번호 (1 ~ 3). 한페이지에 15개
                String apikey = MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY;

                Searcher searcher = new Searcher(); // net.daum.android.map.openapi.search.Searcher
                searcher.searchKeyword(getApplicationContext(), query, latitude, longitude, radius, page, apikey, new OnFinishSearchListener() {
                    @Override
                    public void onSuccess(List<Item> itemList) {
                        mapView.removeAllPOIItems(); // 기존 검색 결과 삭제
                        showResult(itemList); // 검색 결과 보여줌
                    }

                    @Override
                    public void onFail() {
                        showToast("API_KEY의 제한 트래픽이 초과되었습니다.");
                    }
                });
            }
        });


        // 경로 받아오기
        btn_findPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mapView.removeAllPOIItems(); // 기존 검색 결과 삭제
                createMarker();
                addPolyline();
                Timer timer = new Timer();
                timer.schedule(timerTask, 0, 3000);
            }
        });



    }

    private void createMarker() {

        startMarker = new MapPOIItem();
        endMarker = new MapPOIItem();

        startMarker.setItemName("시작점");
        endMarker.setItemName("도착점");
        startMarker.setTag(0);
        endMarker.setTag(1);
        startMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(PointFactory.getStart_longitude(), PointFactory.getStart_latitude()));
        endMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(PointFactory.getEnd_longitude(), PointFactory.getEnd_latitude()));
        startMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        startMarker.setCustomImageResourceId(R.mipmap.map_pin_blue);
        startMarker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
        startMarker.setCustomSelectedImageResourceId(R.mipmap.map_pin_red);
        startMarker.setCustomImageAutoscale(false);
        startMarker.setCustomImageAnchor(0.5f, 1.0f);
        endMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        endMarker.setCustomImageResourceId(R.mipmap.map_pin_blue);
        endMarker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
        endMarker.setCustomSelectedImageResourceId(R.mipmap.map_pin_red);
        endMarker.setCustomImageAutoscale(false);
        endMarker.setCustomImageAnchor(0.5f, 1.0f);


        mapView.addPOIItem(startMarker);
        mapView.addPOIItem(endMarker);

    }

    private void sendCommandAction(int command) {
        if (btService.getState() == 3) {

            switch (command) {
                case Navigation.ACTION_STRAIGHT:
                    btService.sendDirection("S".getBytes());
                    Log.d(TAG, "command: "+command);
                    break;
                case Navigation.ACTION_LEFT:
                    btService.sendDirection("L".getBytes());
                    Log.d(TAG, "command: " + command);
                    break;
                case Navigation.ACTION_RIGHT:
                    btService.sendDirection("R".getBytes());
                    Log.d(TAG, "command: " + command);
                    break;
                case Navigation.ACTION_ARRIVED:
                    btService.sendDirection("A".getBytes());
                    Log.d(TAG, "command: " + command);
                    break;
                default:
                    Log.e(TAG, "wrong command delivered");
            }
        }

    }

    void startConnection() {

        if(btService.getDeviceState()) {
            btService.enableBluetooth();
        } else {
            finish();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode) {
            case REQUEST_CONNECT_DEVICE :
                if(resultCode == Activity.RESULT_OK) {
                    btService.getDeviceInfo(data);
                }
                //send!!!!
//			for(int i=0;i<100000000; i++) {
//				if(btService.getState()==3) {
//					btService.sendDirection("L".getBytes());
//				}
//			}
                break;
            case REQUEST_ENABLE_BT :
                if(resultCode == Activity.RESULT_OK) {
                    btService.scanDevice();
                }else {
                    Log.d(TAG, "Bluetooth is not enavled");
                }
        }
    }

    private void addPolyline() {
        MapPolyline existingPolyline = mapView.findPolylineByTag(1000);
        if(existingPolyline != null) {
            mapView.removePolyline(existingPolyline);
        }

        MapPolyline polyline1 = new MapPolyline();
        polyline1.setTag(1000);
        polyline1.setLineColor(Color.argb(128, 255, 51, 0));
        List<Point> pointList = navigation.getPointList();
        for (Point p : pointList) {
            polyline1.addPoint(MapPoint.mapPointWithGeoCoord(p.getLatitude(), p.getLongitude()));
        }

        mapView.addPolyline(polyline1);

        MapPointBounds mapPointBounds = new MapPointBounds(polyline1.getMapPoints());
        int padding = 100;
        mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));
    }
    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {

        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            if (poiItem == null) return null;
            Item item = mTagItemMap.get(poiItem.getTag());
            if (item == null) return null;
            ImageView imageViewBadge = (ImageView) mCalloutBalloon.findViewById(R.id.badge);
            TextView textViewTitle = (TextView) mCalloutBalloon.findViewById(R.id.title);
            textViewTitle.setText(item.title);
            TextView textViewDesc = (TextView) mCalloutBalloon.findViewById(R.id.desc);
            textViewDesc.setText(item.address);
            imageViewBadge.setImageDrawable(createDrawableFromUrl(item.imageUrl));
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }

    }


    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText_startpoint.getWindowToken(), 0);
    }

    public void onMapViewInitialized(MapView mapView) {
        Log.i(LOG_TAG, "MapView had loaded. Now, MapView APIs could be called safely");

        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(37.537229,127.005515), 2, true);

        Searcher searcher = new Searcher();
        String query = "";
        double latitude = 37.537229;
        double longitude = 127.005515;
        int radius = 10000; // 중심 좌표부터의 반경거리. 특정 지역을 중심으로 검색하려고 할 경우 사용. meter 단위 (0 ~ 10000)
        int page = 1;
        String apikey = MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY;

        searcher.searchKeyword(getApplicationContext(), query, latitude, longitude, radius, page, apikey, new OnFinishSearchListener() {
            @Override
            public void onSuccess(final List<Item> itemList) {
                showResult(itemList);
            }

            @Override
            public void onFail() {

            }
        });
    }

    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NavirellaActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showResult(List<Item> itemList) {
        MapPointBounds mapPointBounds = new MapPointBounds();

        for (int i = 0; i < itemList.size(); i++) {
            Item item = itemList.get(i);

            MapPOIItem poiItem = new MapPOIItem();
            poiItem.setItemName(item.title);
            poiItem.setTag(i);
            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(item.latitude, item.longitude);
            poiItem.setMapPoint(mapPoint);
            mapPointBounds.add(mapPoint);
            poiItem.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            poiItem.setCustomImageResourceId(R.mipmap.map_pin_blue);
            poiItem.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
            poiItem.setCustomSelectedImageResourceId(R.mipmap.map_pin_red);
            poiItem.setCustomImageAutoscale(false);
            poiItem.setCustomImageAnchor(0.5f, 1.0f);

            mapView.addPOIItem(poiItem);
            mTagItemMap.put(poiItem.getTag(), item);
        }

        mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds));

        MapPOIItem[] poiItems = mapView.getPOIItems();
        if (poiItems.length > 0) {
            mapView.selectPOIItem(poiItems[0], false);
        }
    }

    private Drawable createDrawableFromUrl(String url) {
        try {
            InputStream is = (InputStream) this.fetch(url);
            Drawable d = Drawable.createFromStream(is, "src");
            return d;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Object fetch(String address) throws MalformedURLException,IOException {
        URL url = new URL(address);
        Object content = url.getContent();
        return content;
    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
        Item item = mTagItemMap.get(mapPOIItem.getTag());

        Log.e("item1 : ", String.valueOf(item.latitude));
        Log.e("item2 : ", String.valueOf(item.longitude));

        switch (tag){
            case 1:
                PointFactory.setStart_latitude(item.latitude);
                PointFactory.setStart_longitude(item.longitude);
                navigation.setStartPoint(item.longitude, item.latitude);
                editText_startpoint.setText(item.title);
                break;
            case 2:
                PointFactory.setEnd_latitude(item.latitude);
                PointFactory.setEnd_longitude(item.longitude);
                navigation.setEndPoint(item.longitude, item.latitude);
                editText_endpoint.setText(item.title);
                navigation.setPath();
                if (btService.getState() != 3) {
                    Toast toast = Toast.makeText(mapView.getContext(), "Blutooth connection failed", Toast.LENGTH_LONG);
                    toast.show();
                }
                else {
                    Toast toast = Toast.makeText(mapView.getContext(), "Blutooth connection failed", Toast.LENGTH_LONG);
                    toast.show();
                }
                break;
        }
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapCenterPoint) {
    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int zoomLevel) {
    }


}
