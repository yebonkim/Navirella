package com.withcamp.soma6.navirella;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class NavirellaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navirella);

        final Button btn_findstart = (Button) findViewById(R.id.btn_findstart);
        final TextView textview_startpoint = (TextView) findViewById(R.id.textview_startpoint);
        final GpsInfo gps = new GpsInfo(NavirellaActivity.this);

        btn_findstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();
                textview_startpoint.setText(String.valueOf(latitude) + " / " + String.valueOf(longitude));
            }
        });
    }
}
