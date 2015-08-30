package com.withcamp.soma6.navirella;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.withcamp.soma6.navirella.navigationutils.Navigation;
import com.withcamp.soma6.navirella.navigationutils.Point;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;

import java.util.List;

public class NavirellaActivity extends AppCompatActivity implements MapView.MapViewEventListener{

    private static final String TAG = "NavirellaActivity";

    private Navigation navigation;
    private com.withcamp.soma6.navirella.BluetoothService btService;
    private Handler mHandler;

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private MapView mapView;

    public int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navirella);

        final Button btn_findstart = (Button) findViewById(R.id.btn_findstart);
        final Button btn_findend = (Button) findViewById(R.id.btn_findend);
        final Button btn_findPath = (Button) findViewById(R.id.btn_findpath);

        final TextView textview_startpoint = (TextView) findViewById(R.id.textview_startpoint);

        final TextView textview_endpoint = (TextView) findViewById(R.id.textview_endpoint);
        final Intent intent = new Intent(NavirellaActivity.this, SearchActivity.class);

        mHandler = new Handler() {

            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        btService = new BluetoothService(NavirellaActivity.this, mHandler);

        mapView = (MapView)findViewById(R.id.map_view);
        mapView.setDaumMapApiKey("4abc597591421f87535a41b5eef18c5e");
        mapView.setMapViewEventListener(this);

        startConnection();

        navigation = new Navigation();


        // 출발지 설정
        btn_findstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
<<<<<<< HEAD
                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();
               // textview_startpoint.setText(String.valueOf(latitude) + " / " + String.valueOf(longitude));
                textview_startpoint.setText("디캠프");
                // latitude랑 longitude에 값이 들어오면
                // navigation.setStartPoint(longitude, latitude);
                navigation.setStartPoint(127.045345, 37.507912697052184);
=======
                intent.putExtra("tag", 1);
                startActivity(intent);
>>>>>>> 2a7c9ac492ffe737896c4e4d57e126e159890e1e
            }
        });
        // 도착지 설정
        btn_findend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
<<<<<<< HEAD

                // latitude랑 longitude에 값이 들어오면
               // navigation.setEndPoint(longitude, latitude);
                navigation.setEndPoint(127.0466581823289, 37.50642400377133);
                navigation.setPath();
                if(btService.getState() != 3) {
                    Toast toast = Toast.makeText(mapView.getContext(), "Bluetooth connection failed", Toast.LENGTH_LONG);
                    toast.show();
                }
                else {
                    Toast toast = Toast.makeText(mapView.getContext(), "Bluetooth connection success", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
        // 경로 받아오기
        btn_findPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addPolyline();
                int command = navigation.testAction(index);
                sendCommandAction(command);
                index++;
            }
        });

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


    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

=======
                intent.putExtra("tag", 2);
                startActivity(intent);
            }
        });

        textview_startpoint.setText(PointFactory.getStart_latitude() + " / " + PointFactory.getStart_longitude());
        textview_endpoint.setText(PointFactory.getEnd_latitude() + " / " + PointFactory.getEnd_longitude());
>>>>>>> 2a7c9ac492ffe737896c4e4d57e126e159890e1e
    }
}
