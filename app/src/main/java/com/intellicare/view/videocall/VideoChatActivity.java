package com.intellicare.view.videocall;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.intellicare.R;
import com.intellicare.databinding.ActivityVideoChatViewBinding;
import com.intellicare.model.other.PrefConstants;
import com.intellicare.model.response.CommonResponse;
import com.intellicare.model.visitinfo.VisitInfoResponse;
import com.intellicare.net.ServiceHelper;
import com.intellicare.utils.DialogUtil;
import com.intellicare.utils.PreferenceUtil;
import com.intellicare.utils.Utils;
import com.intellicare.view.base.BaseActivity;
import com.intellicare.view.feedback.FeedbackActivity;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.agora.rtc.Constants.REMOTE_VIDEO_STATE_REASON_REMOTE_MUTED;
import static io.agora.rtc.Constants.REMOTE_VIDEO_STATE_REASON_REMOTE_UNMUTED;

public class VideoChatActivity extends BaseActivity {
    private static final String TAG = VideoChatActivity.class.getSimpleName();
    private static final int PERMISSION_REQ_ID = 1000;
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    //    private int uid;
    private RtcEngine mRtcEngine;
    private boolean mCallEnd;
    private boolean mMuted;
    private boolean mVideoPaused = false;
    private boolean mLoudSpeakerEnabled = true;

    //    private FrameLayout mFrameLocalVideo;
//    private FrameLayout mFrameRemoteVideo;
    private VideoCanvas mLocalVideoCanvas;
    private VideoCanvas mRemoteVideoCanvas;
    //    private ImageView btnCall;
//    private ImageView btnMute;
//    private ImageView btnLoudSpeaker;
//    private ImageView btnSwitchCamera;
//    private ImageView btnVideo;
//    private ImageView localImageOnPause;
    private String agoraChannel = "";
    private String agoraAuthToken = "";
    private boolean isFromFCM;
    private boolean isHold;
    private ActivityVideoChatViewBinding binding;
//    private boolean isHoldCallAlert = false;
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
                    Log.e(TAG, "onJoinChannelSuccess uid: " + (uid & 0xFFFFFFFFL));
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
                    Log.e(TAG, "onFirstRemoteVideoDecoded uid: " + (uid & 0xFFFFFFFFL));
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
                    Log.e(TAG, "onFirstRemoteVideoFrame uid: " + (uid & 0xFFFFFFFFL));
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
                    Log.e(TAG, "onUserOffline uid: " + (uid & 0xFFFFFFFFL));
                    Log.e(TAG, "onUserOffline reason: " + reason);
                    onRemoteUserLeft(uid);
                }
            });
        }

        @Override
        public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed);
            if (reason == REMOTE_VIDEO_STATE_REASON_REMOTE_MUTED) {
                Log.e(TAG, "onRemoteVideoStateChanged: " + reason);
            } else if (reason == REMOTE_VIDEO_STATE_REASON_REMOTE_UNMUTED) {
                Log.e(TAG, "onRemoteVideoStateChanged: " + reason);
            }
        }
    };
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showCallHoldMessage(false, false);
        }
    };
    private String status = "Available";

    private void requestPermissions() {
        // Ask for permissions at runtime.
        // This is just an example set of permissions. Other permissions
        // may be needed, and please refer to our online documents.
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            Log.e(TAG, "requestPermissions: success");
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
            initEngineAndJoinChannel();
        }
    }

    private void showLongToast(final String msg) {
        this.runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show());
    }

    /*private void initUI() {
        mFrameLocalVideo = findViewById(R.id.frameLocalVideo);
        mFrameRemoteVideo = findViewById(R.id.frameRemoteVideo);
        btnCall = findViewById(R.id.btnCall);
        btnMute = findViewById(R.id.btnMute);
        btnLoudSpeaker = findViewById(R.id.btnLoudSpeaker);
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera);
        btnVideo = findViewById(R.id.btnVideo);
        localImageOnPause = findViewById(R.id.localImageOnPause);
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        getWindow().setWindowAnimations(android.R.style.Animation_Toast);
        doSetup();
        clearNotification();
        binding = ActivityVideoChatViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getIntentDataIfAvailable();
        showCallHoldMessage(isHold, true);
//        initUI();
//        requestPermissions(); //TODO: Hold functionality related call
    }

    private void clearNotification() {
        try {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancelAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getIntentDataIfAvailable() {
        Intent data = getIntent();
        if (data != null) {
            Log.e(TAG, "getIntentDataIfAvailable: data");
            isFromFCM = data.getBooleanExtra("from", false);
            isHold = data.getBooleanExtra("isHold", false);
            agoraAuthToken = data.getStringExtra("token");
            agoraChannel = data.getStringExtra("channel");
        } else {
            Log.e(TAG, "getIntentDataIfAvailable: null");
        }
    }

    private void initEngineAndJoinChannel() {
        // This is our usual steps for joining
        // a channel and starting a call.
        initializeEngine();
        setupVideoConfig();
        startCall();
    }

    private void startCall() {
        setupLocalVideo(); //TODO: 24Feb
        joinChannel();
        onVideoIconClicked(binding.btnVideo);
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

    private void initializeEngine() {
        Log.e(TAG, "initializeEngine");
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupVideoConfig() {
        Log.e(TAG, "setupVideoConfig");
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
        Log.e(TAG, "setupLocalVideo");
        //24Feb
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, VideoChatActivity.this);
        int uid = Integer.parseInt(patient_id);


        // This is used to set a local preview.
        // The steps setting local and remote view are very similar.
        // But note that if the local user do not have a uid or do
        // not care what the uid is, he can set his uid as ZERO.
        // Our server will assign one and return the uid via the event
        // handler callback function (onJoinChannelSuccess) after
        // joining the channel successfully.
        SurfaceView view = RtcEngine.CreateRendererView(getBaseContext());
        view.setZOrderMediaOverlay(true);
        binding.frameLocalVideo.addView(view);
        // Initializes the local video view.
        // RENDER_MODE_HIDDEN: Uniformly scale the video until it fills the visible boundaries. One dimension of the video may have clipped contents.
        mLocalVideoCanvas = new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, /*uid*/0);
        mRtcEngine.setupLocalVideo(mLocalVideoCanvas);
    }

    private void setupRemoteVideo(int uid) {
        Log.e(TAG, "setupRemoteVideo");
        ViewGroup parent = binding.frameRemoteVideo;
        if (parent.indexOfChild(mLocalVideoCanvas.view) > -1) {
            parent = binding.frameLocalVideo;
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
        view.setZOrderMediaOverlay(parent == binding.frameLocalVideo);
        parent.addView(view);
        mRemoteVideoCanvas = new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, uid);
        // Initializes the video view of a remote user.
        mRtcEngine.setupRemoteVideo(mRemoteVideoCanvas);
    }

    private void onRemoteUserLeft(int uid) {
        Log.e(TAG, "onRemoteUserLeft");
        if (mRemoteVideoCanvas != null /*&& mRemoteVideoCanvas.uid == uid*/) {
            removeFromParent(mRemoteVideoCanvas);
            // Destroys remote view
            mRemoteVideoCanvas = null;
//            endCall(); //TODO : Check for HOLD call functionality
            getVisitInfoServiceCall("0");
        }
    }

    private void onRemoteUserLeft(int uid, int reason) {
        if (mRemoteVideoCanvas != null && mRemoteVideoCanvas.uid == uid) {
            removeFromParent(mRemoteVideoCanvas);
            // Destroys remote view
            mRemoteVideoCanvas = null;
        }
    }

    private void joinChannel() {
        Log.e(TAG, "joinChannel");
        // 1. Users can only see each other after they join the
        // same channel successfully using the same app id.
        // 2. One token is only valid for the channel name that
        // you use to generate this token.
//        String token = getString(R.string.agora_access_token); //POC
        String token = agoraAuthToken;
        if (TextUtils.isEmpty(token) || TextUtils.equals(token, "token")) {
            token = null; // default, no token
        }
        //TODO: Test
        try {
            String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, VideoChatActivity.this);
            int uid = Integer.parseInt(patient_id);
            mRtcEngine.joinChannel(token, agoraChannel, "Extra Optional Data", /*uid*/0);//new
        } catch (NumberFormatException e) {
            e.printStackTrace();
            mRtcEngine.joinChannel(token, agoraChannel, "Extra Optional Data", 0);//old
        }
    }

    private void leaveChannel() {
        try {
            mRtcEngine.leaveChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onLocalAudioMuteClicked(View view) {
        mMuted = !mMuted;
        // Stops/Resumes sending the local audio stream.
        mRtcEngine.muteLocalAudioStream(mMuted);
//        mRtcEngine.setDefaultAudioRoutetoSpeakerphone(mLoudSpeakerEnabled);
        int res = mMuted ? R.drawable.ic_mic_off : R.drawable.ic_mic_on;
        binding.btnMute.setImageResource(res);
    }

    public void onLoudSpeakerClicked(View view) {
        mLoudSpeakerEnabled = !mLoudSpeakerEnabled;
//        mRtcEngine.setDefaultAudioRoutetoSpeakerphone(mLoudSpeakerEnabled);
        mRtcEngine.setEnableSpeakerphone(mLoudSpeakerEnabled);
        int res = mLoudSpeakerEnabled ? R.drawable.ic_speaker_on : R.drawable.ic_speaker_off;
        binding.btnLoudSpeaker.setImageResource(res);
    }

    public void onSwitchCameraClicked(View view) {
        // Switches between front and rear cameras.
        mRtcEngine.switchCamera();
    }

    public void onCallClicked(View view) {
        if (mCallEnd) {
            startCall();
            mCallEnd = false;
            binding.btnCall.setImageResource(R.drawable.btn_endcall);
        } else {
            endCall();
            mCallEnd = true;
            binding.btnCall.setImageResource(R.drawable.btn_startcall);
        }

        showButtons(!mCallEnd);
    }

    public void onVideoIconClicked(View view) {
        Log.e(TAG, "onVideoIconClicked");
        if (mVideoPaused) { //already paused

            /*if(!isLocalVideoSetup) {
                isLocalVideoSetup = true;
                setupLocalVideo();
            }*/


            Log.e(TAG, "mVideoPaused true");
            mVideoPaused = false;
            binding.btnVideo.setImageResource(R.drawable.ic_camera_on);
            resumeLocalVideo();

        } else { //in resume
            Log.e(TAG, "mVideoPaused false");
            mVideoPaused = true;
            binding.btnVideo.setImageResource(R.drawable.ic_camera_off);
            pauseLocalVideo();
        }
    }

    //video camera pause/resume logic for POC
    public void onVideoIconClickedPOC(View view) {
        if (mVideoPaused) { //already paused
            binding.frameLocalVideo.setBackgroundResource(R.drawable.img_pat);
            mVideoPaused = false;
            binding.btnVideo.setImageResource(R.drawable.ic_cameraon);
        } else { //in resume
            binding.frameLocalVideo.setBackgroundColor(getResources().getColor(R.color.light_grey));
            mVideoPaused = true;
            binding.btnVideo.setImageResource(R.drawable.ic_cameraoff);
        }

//        showButtons(!mCallEnd);
    }

    private void showButtons(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        binding.btnMute.setVisibility(visibility);
        binding.btnSwitchCamera.setVisibility(visibility);
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
        if (parent == binding.frameLocalVideo) {
            if (canvas.view instanceof SurfaceView) {
                ((SurfaceView) canvas.view).setZOrderMediaOverlay(false);
            }
            binding.frameRemoteVideo.addView(canvas.view);
        } else if (parent == binding.frameRemoteVideo) {
            if (canvas.view instanceof SurfaceView) {
                ((SurfaceView) canvas.view).setZOrderMediaOverlay(true);
            }
            binding.frameLocalVideo.addView(canvas.view);
        }
    }

    public void onLocalContainerClick(View view) {
        /*switchView(mLocalVideoCanvas);
        switchView(mRemoteVideoCanvas);*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (!mCallEnd)
                leaveChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
          Destroys the RtcEngine instance and releases all resources used by the Agora SDK.

          This method is useful for apps that occasionally make voice or video calls,
          to free up resources for other operations when not making calls.
         */
        RtcEngine.destroy();
    }

    private void moveToFeedActivity() {
        Intent intent = new Intent(VideoChatActivity.this, FeedbackActivity.class);
        startActivity(intent);
        finish();
    }

    private void pauseLocalVideo() {
        if (mRtcEngine != null) {
//            mRtcEngine.enableLocalVideo(false);
            mRtcEngine.muteLocalVideoStream(true);
        }
        binding.localImageOnPause.setVisibility(View.VISIBLE);
    }

    private void resumeLocalVideo() {
        if (mRtcEngine != null) {
//            mRtcEngine.enableLocalVideo(true);
            mRtcEngine.muteLocalVideoStream(false);
        }
        binding.localImageOnPause.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        /*super.onBackPressed();
        endCall();*/

        showAlertDialog(getString(R.string.warning_text), getString(R.string.video_call_exit_warning), getString(R.string.yes), getString(R.string.cancel_text), true);
    }

    @Override
    protected void onPositiveClick() {
        /*if (isHoldCallAlert) {
            patientAvailableServiceCall();
        } else */
        {
            endCall();
        }
    }

    @Override
    protected void onNegativeClick() {

    }

    private void doSetup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            setTurnScreenOn(true);
            setShowWhenLocked(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    //HOLD functionality based on visit info service
    private void getVisitInfoServiceCall(String consult_id) {
        DialogUtil.showProgressDialog(this, getResources().getString(R.string.common_loader_title), getString(R.string.please_wait));
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, VideoChatActivity.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, VideoChatActivity.this);
        Call<VisitInfoResponse> call = new ServiceHelper().getVisitInfo(patient_id, consult_id, token);
        call.enqueue(new Callback<VisitInfoResponse>() {
            @Override
            public void onResponse(@NonNull Call<VisitInfoResponse> call, @NonNull Response<VisitInfoResponse> response) {
                DialogUtil.hideProgressDialog();
                if (response.isSuccessful()) {
                    updateUiOnProviderOffline(response.body()); //TODO: New UI
                } else {
                    updateUiOnProviderOffline(null); //TODO: New UI
                }
            }

            @Override
            public void onFailure(@NonNull Call<VisitInfoResponse> call, @NonNull Throwable t) {
                DialogUtil.hideProgressDialog();
                updateUiOnProviderOffline(null); //TODO: New UI
            }
        });
    }

    //HOLD functionality based on visit info service
    private void patientAvailableServiceCall() {
        DialogUtil.showProgressDialog(this, getResources().getString(R.string.common_loader_title), getString(R.string.please_wait));
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, VideoChatActivity.this);
        String consult_id = PreferenceUtil.getData(PrefConstants.PREF_CURRENT_CONSULT_ID, VideoChatActivity.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, VideoChatActivity.this);
        Call<CommonResponse> call = new ServiceHelper().getPatientAvailable(patient_id, consult_id, status, token);
        call.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(@NonNull Call<CommonResponse> call, @NonNull Response<CommonResponse> response) {
                DialogUtil.hideProgressDialog();
                /*if (response.isSuccessful()) { //commented as no need of UI change
                    updateUiOnPatientAvailable(response.body());
                } else {
                    updateUiOnPatientAvailable(null);
                }*/
            }

            @Override
            public void onFailure(@NonNull Call<CommonResponse> call, @NonNull Throwable t) {
                DialogUtil.hideProgressDialog();
                /*updateUiOnPatientAvailable(null);*/
            }
        });
    }

    //Not using as of 19may
    private void updateUiOnPatientAvailable(CommonResponse body) {
        if (body == null || body.getStatus().equalsIgnoreCase("failure")) {
            Utils.toast("Server error", VideoChatActivity.this);
        } else {
            Utils.toast(body.getMessage(), VideoChatActivity.this);
            showCallHoldMessage(false, false);
        }
    }

    private void updateUiOnProviderOffline(VisitInfoResponse body) {
        if (body == null || body.getStatus().equalsIgnoreCase("failure")) {
//            Utils.toastLong("Server error", VideoChatActivity.this);
            try {
                endCall();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            PreferenceUtil.saveData(PrefConstants.PREF_CURRENT_CONSULT_ID, body.getConsult().getConsultId(), VideoChatActivity.this);
            agoraChannel = body.getConsult().getChannel();
            agoraAuthToken = body.getConsult().getToken();

            String visitStatus = body.getConsult().getStatus();
            if (!TextUtils.isEmpty(visitStatus) && visitStatus.equalsIgnoreCase("hold")) {
                showCallHoldMessage(true, false);
            } else {
                endCall();
            }
        }
    }

    private void showCallHoldMessage(boolean isHold, boolean fromOnCreate) {
        if (fromOnCreate) {
            if (isHold) {
                binding.llCallHoldMessage.setVisibility(View.VISIBLE);
                binding.frameLocalVideo.setVisibility(View.GONE);
                binding.frameRemoteVideo.setVisibility(View.GONE);
                binding.llControlPanel.setVisibility(View.GONE);
            } else {
//                mVideoPaused = !mVideoPaused;
                requestPermissions();
            }
        } else {
            binding.llCallHoldMessage.setVisibility(isHold ? View.VISIBLE : View.GONE);
            binding.frameLocalVideo.setVisibility(isHold ? View.GONE : View.VISIBLE);
            binding.frameRemoteVideo.setVisibility(isHold ? View.GONE : View.VISIBLE);
            binding.llControlPanel.setVisibility(isHold ? View.GONE : View.VISIBLE);
        }
    }

    public void onAvailableButtonClicked(View view) {
        patientAvailableServiceCall();
    }

    public void onSwitchClicked(View view) {
        status = ((SwitchCompat) view).isChecked() ? "Available" : "Away";
        patientAvailableServiceCall();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(VideoChatActivity.this).registerReceiver(receiver, new IntentFilter("hold"));
        PreferenceUtil.saveBoolean(PrefConstants.PREF_IS_APP_FOREGROUND, true, VideoChatActivity.this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(VideoChatActivity.this).unregisterReceiver(receiver);
        PreferenceUtil.saveBoolean(PrefConstants.PREF_IS_APP_FOREGROUND, false, VideoChatActivity.this);
    }
}