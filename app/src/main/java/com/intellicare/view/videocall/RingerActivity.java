package com.intellicare.view.videocall;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;

import androidx.appcompat.app.AppCompatDelegate;

import com.intellicare.R;
import com.intellicare.databinding.ActivityDemoRingerVideoCallBinding;
import com.intellicare.utils.Utils;
import com.intellicare.view.base.BaseActivity;

public class RingerActivity extends BaseActivity implements View.OnClickListener {
    private static final int RINGER_TIME_IN_MILLIS = 60000; //60 seconds
    private static final int VIBRATOR_TIME_IN_MILLIS = 500; //half second
    private static final String TAG = RingerActivity.class.getSimpleName();
    private ActivityDemoRingerVideoCallBinding binding;
    private MediaPlayer mp;
    private Vibrator vibrator;
    private Ringtone ringtone;
    private Handler handler;
    private Runnable r;
    private String agoraChannel = "";
    private String agoraAuthToken = "";
    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener touchListener = (v, event) -> {
        doPickUpCallAction();
        return false;
    };
    private boolean isFromFCM;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        getWindow().setWindowAnimations(android.R.style.Animation_Toast);
        clearNotification();
        doSetup();
        binding = ActivityDemoRingerVideoCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getIntentDataIfAvailable();
//        animatePickupCall();
        playRingerBasedOnUserSelectionMode();
//        startVibration();
//        binding.btnPickup.setOnClickListener(this);
        binding.btnPickup.setOnTouchListener(touchListener);
        binding.btnEndCall.setOnClickListener(this);

        prepareRunnable();
        handler = new Handler(getMainLooper());
        handler.postDelayed(r, RINGER_TIME_IN_MILLIS);
    }

    private void getIntentDataIfAvailable() {
        Intent data = getIntent();
        if (data != null) {
            Log.e(TAG, "getIntentDataIfAvailable: data");
            isFromFCM = data.getBooleanExtra("from", false);
            agoraAuthToken = data.getStringExtra("token");
            agoraChannel = data.getStringExtra("channel");
        } else {
            Log.e(TAG, "getIntentDataIfAvailable: null");
        }
    }

    private void playRingtone() {
        try {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mp = MediaPlayer.create(getApplicationContext(), alarmSound);
            mp.setLooping(true);
            mp.setScreenOnWhilePlaying(true);
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRingtone() {
        if (mp != null) {
            mp.stop();
            mp = null;
        }
    }


    private void playRingtoneNew() {
        try {
            Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL);
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), defaultUri);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone.setLooping(true);
            }
            ringtone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRingtoneNew() {
        try {
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
                ringtone = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doSetup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            setTurnScreenOn(true);
            setShowWhenLocked(true);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    private void startVibration() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(new long[]{2000, 1000}, 0));
            } else {
                vibrator.vibrate(VIBRATOR_TIME_IN_MILLIS);
            }
        }
    }

    private void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnPickup) {
            doPickUpCallAction();
        } else if (v.getId() == R.id.btnEndCall) {
            Utils.toast("Call rejected", RingerActivity.this);
            clearHandler();
            finishRinger();
        }
    }

    private void doPickUpCallAction() {
        clearHandler();
        redirectToVideoCall();
    }

    private void redirectToVideoCall() {
//        Utils.toast("Call accepted", RingerActivity.this);
        stopRingtoneNew();
        stopVibration();
        Intent intent = new Intent(RingerActivity.this, VideoChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("from", true);
        intent.putExtra("token", agoraAuthToken);
        intent.putExtra("channel", agoraChannel);
        startActivity(intent);
        finish();
    }

    private void finishRinger() {
        stopRingtoneNew();
        stopVibration();
        finish();
    }

    private void prepareRunnable() {
        r = binding.btnEndCall::performClick;
    }

    private void clearHandler() {
        if (handler != null && r != null) {
            handler.removeCallbacks(r);
        }
    }

    private void animatePickupCall() {
        ObjectAnimator buttonAnimator = ObjectAnimator.ofFloat(binding.btnPickup, "translationX", 0f, 100f);
        buttonAnimator.setDuration(1600);
        buttonAnimator.setInterpolator(new BounceInterpolator());
        buttonAnimator.setRepeatCount(ValueAnimator.INFINITE);
        buttonAnimator.start();
    }

    private void clearNotification() {
        try {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancelAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            finishRinger();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playRingerBasedOnUserSelectionMode() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        switch (am.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
//                Utils.toast("Silent mode", RingerActivity.this);
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
//                Utils.toast("Vibrate mode", RingerActivity.this);
                startVibration();
                break;
            case AudioManager.RINGER_MODE_NORMAL:
//                Utils.toast("Normal mode", RingerActivity.this);
                playRingtoneNew();
                startVibration();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e("onKeyDown", keyCode + " -> "+ event.getKeyCode()+ " -> "+ event.getAction());

        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            makeRingerSilent();
        } else if(keyCode == KeyEvent.KEYCODE_POWER) {
            Log.e("onKeyDown", "Power");
        }

        return super.onKeyDown(keyCode, event);
    }

    private void makeRingerSilent() {
        stopRingtoneNew();
        stopVibration();
    }
}