package com.intellicare.view.videocall;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.intellicare.R;
import com.intellicare.view.feedback.FeedbackActivity;
import com.themahi.logger.LoggerRecyclerView;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class VideoChatActivityBkp extends AppCompatActivity {
    private static final String TAG = VideoChatActivityBkp.class.getSimpleName();
    private static final int PERMISSION_REQ_ID = 1000;
    // Permission WRITE_EXTERNAL_STORAGE is not mandatory
    // for Agora RTC SDK, just in case if you wanna save
    // logs to external sdcard.
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    //    private int uid;
    private RtcEngine mRtcEngine;
    private boolean mCallEnd;
    private boolean mMuted;
    private boolean mVideoPaused;
    private FrameLayout mFrameLocalVideo;
    private FrameLayout mFrameRemoteVideo;
    private VideoCanvas mLocalVideoCanvas;
    private VideoCanvas mRemoteVideoCanvas;
    private ImageView btnCall;
    private ImageView btnMute;
    private ImageView btnSwitchCamera;
    private ImageView btnVideo;
    private String agoraChannel = "";
    private String agoraAuthToken = "";
    private boolean isFromFCM;
    // Customized logger view
    private LoggerRecyclerView loggerRecyclerView;
    /**
     * Event handler registered into RTC engine for RTC callbacks.
     * Note that UI operations needs to be in UI thread because RTC
     * engine deals with the events in a separate thread.
     */
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        /**
         * Occurs when the local user joins a specified channel.
         * The channel name assignment is based on channelName specified in the joinChannel method.
         * If the uid is not specified when joinChannel is called, the server automatically assigns a uid.
         *
         * @param channel Channel name.
         * @param uid User ID.
         * @param elapsed Time elapsed (ms) from the user calling joinChannel until this callback is triggered.
         */
        @Override
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loggerRecyclerView.logI("Join channel success, uid: " + (uid & 0xFFFFFFFFL));
                }
            });
        }

        /**
         * Occurs when the first remote video frame is received and decoded.
         * This callback is triggered in either of the following scenarios:
         *
         *     The remote user joins the channel and sends the video stream.
         *     The remote user stops sending the video stream and re-sends it after 15 seconds. Possible reasons include:
         *         The remote user leaves channel.
         *         The remote user drops offline.
         *         The remote user calls the muteLocalVideoStream method.
         *         The remote user calls the disableVideo method.
         *
         * @param uid User ID of the remote user sending the video streams.
         * @param width Width (pixels) of the video stream.
         * @param height Height (pixels) of the video stream.
         * @param elapsed Time elapsed (ms) from the local user calling the joinChannel method until this callback is triggered.
         */
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loggerRecyclerView.logI("First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
            super.onFirstRemoteVideoFrame(uid, width, height, elapsed);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loggerRecyclerView.logI("First remote video frame, uid: " + (uid & 0xFFFFFFFFL));
                    setupRemoteVideo(uid);
                }
            });
        }

        /**
         * Occurs when a remote user (Communication)/host (Live Broadcast) leaves the channel.
         *
         * There are two reasons for users to become offline:
         *
         *     Leave the channel: When the user/host leaves the channel, the user/host sends a
         *     goodbye message. When this message is received, the SDK determines that the
         *     user/host leaves the channel.
         *
         *     Drop offline: When no data packet of the user or host is received for a certain
         *     period of time (20 seconds for the communication profile, and more for the live
         *     broadcast profile), the SDK assumes that the user/host drops offline. A poor
         *     network connection may lead to false detections, so we recommend using the
         *     Agora RTM SDK for reliable offline detection.
         *
         * @param uid ID of the user or host who leaves the channel or goes offline.
         * @param reason Reason why the user goes offline:
         *
         *     USER_OFFLINE_QUIT(0): The user left the current channel.
         *     USER_OFFLINE_DROPPED(1): The SDK timed out and the user dropped offline because no data packet was received within a certain period of time. If a user quits the call and the message is not passed to the SDK (due to an unreliable channel), the SDK assumes the user dropped offline.
         *     USER_OFFLINE_BECOME_AUDIENCE(2): (Live broadcast only.) The client role switched from the host to the audience.
         */
        @Override
        public void onUserOffline(final int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loggerRecyclerView.logI("User offline, uid: " + (uid & 0xFFFFFFFFL));
                    Log.e(TAG, "onUserOffline: reason"+ reason);
                    onRemoteUserLeft(uid);
                }
            });
        }
    };

    private void requestPermissions() {
        // Ask for permissions at runtime.
        // This is just an example set of permissions. Other permissions
        // may be needed, and please refer to our online documents.
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
//            startVideoChat();
            initEngineAndJoinChannel();
        }
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                showLongToast("Need permissions " + Manifest.permission.RECORD_AUDIO +
                        "/" + Manifest.permission.CAMERA + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                finish();
                return;
            }

            // Here we continue only if all permissions are granted.
            // The permissions can also be granted in the system settings manually.
//            startVideoChat();
            initEngineAndJoinChannel();
        }
    }

    private void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat_view);
        getIntentDataIfAvailable();
        initUI();
        initEngineAndJoinChannel();
        requestPermissions();
    }

    private void getIntentDataIfAvailable() {
        Intent data = getIntent();
        if (data != null) {
            isFromFCM = data.getBooleanExtra("from", false);
            agoraAuthToken = data.getStringExtra("token");
            agoraChannel = data.getStringExtra("channel");
        }
    }

    private void initUI() {
        mFrameLocalVideo = findViewById(R.id.frameLocalVideo);
        mFrameRemoteVideo = findViewById(R.id.frameRemoteVideo);
        btnCall = findViewById(R.id.btnCall);
        btnMute = findViewById(R.id.btnMute);
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera);
        btnVideo = findViewById(R.id.btnVideo);
        loggerRecyclerView = findViewById(R.id.loggerRecyclerView);

        // Sample logs are optional.
        showSampleLogs();
    }

    private void showSampleLogs() {
        loggerRecyclerView.logI("Hi KVK");
    }

    private void initEngineAndJoinChannel() {
        // This is our usual steps for joining
        // a channel and starting a call.
        initializeEngine();
        setupVideoConfig();
        setupLocalVideo();
        joinChannel();
        startCall(); //receivecall
    }

    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupVideoConfig() {
        // In simple use cases, we only need to enable video capturing
        // and rendering once at the initialization step.
        // Note: audio recording and playing is enabled by default.
        mRtcEngine.enableVideo();

        // Please go to this page for detailed explanation
        // https://docs.agora.io/en/Video/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_rtc_engine.html#af5f4de754e2c1f493096641c5c5c1d8f
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    private void setupLocalVideo() {
        // This is used to set a local preview.
        // The steps setting local and remote view are very similar.
        // But note that if the local user do not have a uid or do
        // not care what the uid is, he can set his uid as ZERO.
        // Our server will assign one and return the uid via the event
        // handler callback function (onJoinChannelSuccess) after
        // joining the channel successfully.
        SurfaceView view = RtcEngine.CreateRendererView(getBaseContext());
        view.setZOrderMediaOverlay(true);
        mFrameLocalVideo.addView(view);
        // Initializes the local video view.
        // RENDER_MODE_HIDDEN: Uniformly scale the video until it fills the visible boundaries. One dimension of the video may have clipped contents.
        mLocalVideoCanvas = new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, 0);
        mRtcEngine.setupLocalVideo(mLocalVideoCanvas);
    }

    private void setupRemoteVideo(int uid) {
        ViewGroup parent = mFrameRemoteVideo;
        if (parent.indexOfChild(mLocalVideoCanvas.view) > -1) {
            parent = mFrameLocalVideo;
        }

        // Only one remote video view is available for this
        // tutorial. Here we check if there exists a surface
        // view tagged as this uid.
        if (mRemoteVideoCanvas != null) {
            return;
        }

        /*
          Creates the video renderer view.
          CreateRendererView returns the SurfaceView type. The operation and layout of the view
          are managed by the app, and the Agora SDK renders the view provided by the app.
          The video display view must be created using this method instead of directly
          calling SurfaceView.
         */
        SurfaceView view = RtcEngine.CreateRendererView(getBaseContext());
        view.setZOrderMediaOverlay(parent == mFrameLocalVideo);
        parent.addView(view);
        mRemoteVideoCanvas = new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, uid);
        // Initializes the video view of a remote user.
        mRtcEngine.setupRemoteVideo(mRemoteVideoCanvas);
    }

    private void onRemoteUserLeft(int uid) {
        if (mRemoteVideoCanvas != null && mRemoteVideoCanvas.uid == uid) {
            removeFromParent(mRemoteVideoCanvas);
            // Destroys remote view
            mRemoteVideoCanvas = null;
        }
    }

    private void joinChannel() {
        // 1. Users can only see each other after they join the
        // same channel successfully using the same app id.
        // 2. One token is only valid for the channel name that
        // you use to generate this token.
//        String token = getString(R.string.agora_access_token); //POC
        String token = agoraAuthToken;
        if (TextUtils.isEmpty(token) || TextUtils.equals(token, "token")) {
            token = null; // default, no token
        }
        mRtcEngine.joinChannel(token, agoraChannel, "Extra Optional Data", 0);
    }

    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    public void onLocalAudioMuteClicked(View view) {
        mMuted = !mMuted;
        // Stops/Resumes sending the local audio stream.
        mRtcEngine.muteLocalAudioStream(mMuted);
        int res = mMuted ? R.drawable.btn_mute : R.drawable.btn_unmute;
        btnMute.setImageResource(res);
    }

    public void onSwitchCameraClicked(View view) {
        // Switches between front and rear cameras.
        mRtcEngine.switchCamera();
    }

    public void onCallClicked(View view) {
        if (mCallEnd) {
            startCall();
            mCallEnd = false;
            btnCall.setImageResource(R.drawable.btn_endcall);
        } else {
            endCall();
            mCallEnd = true;
            btnCall.setImageResource(R.drawable.btn_startcall);
        }

        showButtons(!mCallEnd);
    }

    //video camera pause/resume logic for production
    public void onVideoIconClicked(View view) {
        if (mVideoPaused) { //already paused
            mVideoPaused = false;
            btnVideo.setImageResource(R.drawable.ic_cameraon);
            resumeLocalVideo();

        } else { //in resume
            mVideoPaused = true;
            btnVideo.setImageResource(R.drawable.ic_cameraoff);
            pauseLocalVideo();
            mFrameLocalVideo.setBackgroundResource(R.drawable.img_pat);

        }
    }

    //video camera pause/resume logic for POC
    public void onVideoIconClickedPOC(View view) {
        if (mVideoPaused) { //already paused
            mFrameLocalVideo.setBackgroundResource(R.drawable.img_pat);
            mVideoPaused = false;
            btnVideo.setImageResource(R.drawable.ic_cameraon);
        } else { //in resume
            mFrameLocalVideo.setBackgroundColor(getResources().getColor(R.color.light_grey));
            mVideoPaused = true;
            btnVideo.setImageResource(R.drawable.ic_cameraoff);
        }

//        showButtons(!mCallEnd);
    }

    private void startCall() {
        setupLocalVideo(); //commented for POC
        joinChannel();
    }

    private void endCall() {
        removeFromParent(mLocalVideoCanvas);
        mLocalVideoCanvas = null;
        removeFromParent(mRemoteVideoCanvas);
        mRemoteVideoCanvas = null;
        leaveChannel();

        //finish video call and goto feedback for POC
        moveToFeedActivity();
    }

    private void showButtons(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        btnMute.setVisibility(visibility);
        btnSwitchCamera.setVisibility(visibility);
    }

    private ViewGroup removeFromParent(VideoCanvas canvas) {
        if (canvas != null) {
            ViewParent parent = canvas.view.getParent();
            if (parent != null) {
                ViewGroup group = (ViewGroup) parent;
                group.removeView(canvas.view);
                return group;
            }
        }
        return null;
    }

    private void switchView(VideoCanvas canvas) {
        ViewGroup parent = removeFromParent(canvas);
        if (parent == mFrameLocalVideo) {
            if (canvas.view instanceof SurfaceView) {
                ((SurfaceView) canvas.view).setZOrderMediaOverlay(false);
            }
            mFrameRemoteVideo.addView(canvas.view);
        } else if (parent == mFrameRemoteVideo) {
            if (canvas.view instanceof SurfaceView) {
                ((SurfaceView) canvas.view).setZOrderMediaOverlay(true);
            }
            mFrameLocalVideo.addView(canvas.view);
        }
    }

    public void onLocalContainerClick(View view) {
        switchView(mLocalVideoCanvas);
        switchView(mRemoteVideoCanvas);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mCallEnd) {
            leaveChannel();
        }
        /*
          Destroys the RtcEngine instance and releases all resources used by the Agora SDK.

          This method is useful for apps that occasionally make voice or video calls,
          to free up resources for other operations when not making calls.
         */
        RtcEngine.destroy();
    }

    private void moveToFeedActivity() {
        Intent intent = new Intent(VideoChatActivityBkp.this, FeedbackActivity.class);
        startActivity(intent);
        finish();
    }

    private void pauseLocalVideo() {
        if (mRtcEngine != null)
            mRtcEngine.enableLocalVideo(false);
    }

    private void resumeLocalVideo() {
        if (mRtcEngine != null)
            mRtcEngine.enableLocalVideo(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        endCall();
    }
}