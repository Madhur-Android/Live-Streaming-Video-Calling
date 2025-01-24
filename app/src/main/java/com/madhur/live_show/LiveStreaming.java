package com.madhur.live_show;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class LiveStreaming extends AppCompatActivity {

    private Button btnSwitchRole;
    private Button btnJoinLeave;
    private static final int PERMISSION_REQ_ID = 22;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_streaming);

        btnSwitchRole = findViewById(R.id.btn_switch_role);
        btnJoinLeave = findViewById(R.id.btn_join_leave);
        ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSION_REQ_ID);

        btnSwitchRole.setOnClickListener(v -> {
            Intent i = new Intent(LiveStreaming.this, Broadcaster_Activity.class);
            startActivity(i);
        });

        btnJoinLeave.setOnClickListener(v -> {
            Intent i = new Intent(LiveStreaming.this, Audience_Activity.class);
            startActivity(i);
        });
    }

    private String[] getRequiredPermissions(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return new String[]{
                    android.Manifest.permission.RECORD_AUDIO, // Recording permission
                    android.Manifest.permission.CAMERA, // Camera permission
                    android.Manifest.permission.READ_PHONE_STATE, // Permission to read phone status
                    android.Manifest.permission.BLUETOOTH_CONNECT // Bluetooth connection permission
            };
        } else {
            return new String[]{
                    android.Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
            };
        }
    }

}
