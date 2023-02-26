package com.example.wakeupapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

public class StatisticsActivity extends AppCompatActivity {
    private SharedPreferences sp4PS;
    private SharedPreferences.Editor spEditor;
    //private String sharedVersion = "anyangV1.1";

    //public static final int REQUEST_CODE = 102;  //button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);


        //Statistics
        sp4PS = getApplicationContext().getSharedPreferences("anyang.txt", MODE_PRIVATE);
        spEditor = sp4PS.edit();

        String vText1= sp4PS.getString("tvalue1", "0");
        String vText2= sp4PS.getString("tvalue2", "0");

        TextView tView1 = (TextView) findViewById(R.id.tvText1);
        TextView tView2 = (TextView) findViewById(R.id.tvText2);

        int n1 = FaceDetectorProcessor.eyes_counter;
        String t1 = String.valueOf(n1);
        vText1 = t1;

        int n2 = FaceDetectorProcessor.yawn_counter;
        String t2 = String.valueOf(n2);
        vText2 = t2;

        tView1.setText(vText1);
        tView2.setText(vText2);

        //Button
        Button Statistics_Preview_button = findViewById(R.id.Statistics_Preview_button);
        Statistics_Preview_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);; // 액티비티 띄우기
                finish();
            }
        });

    }
}