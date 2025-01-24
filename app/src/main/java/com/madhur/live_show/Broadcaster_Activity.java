package com.madhur.live_show;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.CameraCapturerConfiguration;
import io.agora.rtc2.video.VideoCanvas;

public class Broadcaster_Activity extends AppCompatActivity {

    private static final String APP_ID = "eebc31bc66a6436d90808240e3719c80"; // Replace with your Agora App ID
    private static final String CHANNEL_NAME = "Live_Video";
    private static final String TEMP_TOKEN =  "007eJxTYMgImGoUwqX6k5+tg9+W0fAps/Rj27aXdz/3uZXLOwV/OazAkJqalGxsmJRsZpZoZmJslmJpYGFgYWRikGpsbmiZbGHw/1NvekMgI4P6lkRmRgYIBPG5GHwyy1LjwzJTUvMZGAAw4R+K";
    private static final int PERMISSION_REQ_ID = 22;
    private RtcEngine mRtcEngine;
    private boolean isAudioMuted = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcaster);

        if (checkPermissions()) {
            initializeAndJoinChannel();
        } else {
            ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSION_REQ_ID);
        }

        ImageView switch_camera_btn = findViewById(R.id.switch_camera_btn);
        switch_camera_btn.setOnClickListener(v -> {
            if (mRtcEngine != null) {
                mRtcEngine.switchCamera(); // Switch the camera
            }
        });

      ImageView muteAudioButton = findViewById(R.id.muteAudioButton);
        muteAudioButton.setOnClickListener(v -> {
            if (mRtcEngine != null) {
                isAudioMuted = !isAudioMuted; // Toggle mute state
                mRtcEngine.muteLocalAudioStream(isAudioMuted);

                // Update the icon based on the current mute state
                if (isAudioMuted) {
                    muteAudioButton.setImageResource(R.drawable.baseline_mic_off_24); // Muted icon
                } else {
                    muteAudioButton.setImageResource(R.drawable.baseline_mic_24); // Unmuted icon
                }
            }
        });

        Button callEnd = findViewById(R.id.callEnd);
        callEnd.setOnClickListener(v -> {
            mRtcEngine.stopPreview();
            mRtcEngine.leaveChannel();
            Intent i = new Intent(Broadcaster_Activity.this, LiveStreaming.class);
            startActivity(i);
            finish();
        });
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        // Callback when successfully joining the channel
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            runOnUiThread(() -> {
                Toast.makeText(Broadcaster_Activity.this, "Join channel success", Toast.LENGTH_SHORT).show();
            });
        }

        // Callback when a remote user or host joins the current channel
        @Override
        public void onUserJoined(int uid, int elapsed) {

        }

        // Callback when a remote user or host leaves the current channel
        @Override
        public void onUserOffline(int uid, int reason) {
            super.onUserOffline(uid, reason);
            runOnUiThread(() -> {
                Toast.makeText(Broadcaster_Activity.this, "User offline: " + uid, Toast.LENGTH_SHORT).show();
            });
        }
    };

    private void initializeAndJoinChannel() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = APP_ID;
            config.mEventHandler = mRtcEventHandler;
            mRtcEngine = RtcEngine.create(config);

        } catch (Exception e) {
            throw new RuntimeException("Check the error.");
        }
        mRtcEngine.enableVideo();
        mRtcEngine.startPreview();
        FrameLayout container = findViewById(R.id.local_video_view_container);
        SurfaceView surfaceView = new SurfaceView (getBaseContext());
        container.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, Constants.VIDEO_SOURCE_CAMERA_PRIMARY));

        ChannelMediaOptions options = new ChannelMediaOptions();
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
        options.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_ULTRA_LOW_LATENCY;
        mRtcEngine.joinChannel(TEMP_TOKEN, CHANNEL_NAME, 0, options);
    }


    private String[] getRequiredPermissions(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return new String[]{
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.READ_PHONE_STATE,
                    android.Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            return new String[]{
                    android.Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
            };
        }
    }

    private boolean checkPermissions() {
        for (String permission : getRequiredPermissions()) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkPermissions()) {
            initializeAndJoinChannel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRtcEngine.stopPreview();
        mRtcEngine.leaveChannel();
    }
}