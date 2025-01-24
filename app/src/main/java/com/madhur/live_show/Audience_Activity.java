package com.madhur.live_show;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

public class Audience_Activity extends AppCompatActivity {

    private static final String APP_ID = "eebc31bc66a6436d90808240e3719c80"; // Replace with your Agora App ID
    private static final String CHANNEL_NAME = "Live_Video";
    private static final String TEMP_TOKEN = "007eJxTYMgImGoUwqX6k5+tg9+W0fAps/Rj27aXdz/3uZXLOwV/OazAkJqalGxsmJRsZpZoZmJslmJpYGFgYWRikGpsbmiZbGHw/1NvekMgI4P6lkRmRgYIBPG5GHwyy1LjwzJTUvMZGAAw4R+K";
    private RtcEngine mRtcEngine;
    private Button btn_join_leave;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audience);

        btn_join_leave = findViewById(R.id.btn_join_leave);
        btn_join_leave.setOnClickListener(v -> {
            mRtcEngine.stopPreview();
            mRtcEngine.leaveChannel();
            Intent i = new Intent(Audience_Activity.this, LiveStreaming.class);
            startActivity(i);
            finish();
        });

        initializeAndJoinChannel();

    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {

        // Callback when successfully joining the channel
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            runOnUiThread(() -> {
                Toast.makeText(Audience_Activity.this, "Join channel success", Toast.LENGTH_SHORT).show();
            });
        }

        // Callback when a remote user or host joins the current channel
        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> {
                setupRemoteVideo(uid);
            });
        }

        // Callback when a remote user or host leaves the current channel
        @Override
        public void onUserOffline(int uid, int reason) {
            super.onUserOffline(uid, reason);
            runOnUiThread(() -> {
                Toast.makeText(Audience_Activity.this, "User offline: " + uid, Toast.LENGTH_SHORT).show();
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

        ChannelMediaOptions options = new ChannelMediaOptions();
        options.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE; // Set role to Audience
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
        options.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_ULTRA_LOW_LATENCY;

        mRtcEngine.joinChannel(TEMP_TOKEN, CHANNEL_NAME, 0, options);
    }


    private void setupRemoteVideo(int uid) {
        FrameLayout container = findViewById(R.id.remote_video_view_container);
        SurfaceView surfaceView = new SurfaceView(getBaseContext());
        surfaceView.setZOrderMediaOverlay(true);
        container.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRtcEngine.stopPreview();
        mRtcEngine.leaveChannel();
    }
}