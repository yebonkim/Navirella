package com.withcamp.soma6.navirella;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.withcamp.soma6.navirella.navigationutils.Navigation;

public class NavirellaActivity extends AppCompatActivity {

    private static final String TAG = "NavirellaActivity";
    Navigation navigation;
    public int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navirella);

        final Button btn_findstart = (Button) findViewById(R.id.btn_findstart);
        final Button btn_findend = (Button) findViewById(R.id.btn_findend);
        final Button btn_findPath = (Button) findViewById(R.id.btn_findpath);
        final TextView textview_startpoint = (TextView) findViewById(R.id.textview_startpoint);
        final TextView textView_endpoint = (TextView) findViewById(R.id.textview_endpoint);
        final GpsInfo gps = new GpsInfo(NavirellaActivity.this);

        navigation = new Navigation();

        // 출발지 설정
        btn_findstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();
                textview_startpoint.setText(String.valueOf(latitude) + " / " + String.valueOf(longitude));

                // latitude랑 longitude에 값이 들어오면
                // navigation.setStartPoint(longitude, latitude);
                navigation.setStartPoint(127.045345, 37.507912697052184);
            }
        });
        // 도착지 설정
        btn_findend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // latitude랑 longitude에 값이 들어오면
               // navigation.setEndPoint(longitude, latitude);
                navigation.setEndPoint(127.0466581823289, 37.50642400377133);
                navigation.setPath();
            }
        });
        // 경로 받아오기
        btn_findPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int command = navigation.testAction(index);
                
                index++;
            }
        });

    }


}
