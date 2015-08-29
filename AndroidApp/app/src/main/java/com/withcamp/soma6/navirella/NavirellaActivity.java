package com.withcamp.soma6.navirella;

import android.content.Intent;
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
        final Button btn_findend = (Button) findViewById(R.id.btn_findend);
        final TextView textview_startpoint = (TextView) findViewById(R.id.textview_startpoint);
        final TextView textview_endpoint = (TextView) findViewById(R.id.textview_endpoint);
        final Intent intent = new Intent(NavirellaActivity.this, SearchActivity.class);

        btn_findstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("tag", 1);
                startActivity(intent);
            }
        });

        btn_findend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("tag", 2);
                startActivity(intent);
            }
        });

        textview_startpoint.setText(PointFactory.getStart_latitude() + " / " + PointFactory.getStart_longitude());
        textview_endpoint.setText(PointFactory.getEnd_latitude() + " / " + PointFactory.getEnd_longitude());
    }
}
