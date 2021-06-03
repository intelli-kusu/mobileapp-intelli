package com.intellicare.view.base;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.intellicare.databinding.ActivitySplashBinding;
import com.intellicare.model.other.PrefConstants;
import com.intellicare.samples.SampleActivity;
import com.intellicare.utils.PreferenceUtil;
import com.intellicare.utils.Utils;
import com.intellicare.view.dashboard.DashboardActivity;
import com.intellicare.view.login.LoginActivity;
import com.intellicare.view.visit.PastVisitsActivity;

public class SplashActivity extends BaseActivity {
    private static final int SPLASH_MILLIS = 500;
    private static final int OVERLAY_PERMISSION_RESULT_CODE = 1000;
    //24Feb To eliminate lag while in consultation page
    private static final int PERMISSION_REQ_ID = 1000;
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
//        doSetup();
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        printDeviceInfo();
//        getCountryCode(SplashActivity.this);
        new Handler(getMainLooper()).postDelayed(this::requestPermissions, SPLASH_MILLIS);
//        new Handler(getMainLooper()).postDelayed(this::moveToTestScreen, SPLASH_MILLIS);

        //checkPermission();
//        runOnUiThread(this::requestPermissions);
    }

    private void moveToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        intent.putExtra("from", "splash");
        startActivity(intent);
        finish();
    }

    private void moveToDashboard() {
        Intent intent = new Intent(SplashActivity.this, DashboardActivity.class);

        startActivity(intent);
        finish();
    }

    private void moveToTestScreen() {
        Intent intent = new Intent(SplashActivity.this, SampleActivity.class);
        startActivity(intent);
        finish();
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

    @SuppressLint("HardwareIds")
    private void printDeviceInfo() {

        String deviceInfo = "-------------------------------------\n" +
                "Model :" + Build.MODEL + "\n" +//The end-user-visible name for the end product.
                "Device: " + Build.DEVICE + "\n" +//The name of the industrial design.
                "Manufacturer: " + Build.MANUFACTURER + "\n" +//The manufacturer of the product/hardware.
                "Board: " + Build.BOARD + "\n" +//The name of the underlying board, like "goldfish".
                "Brand: " + Build.BRAND + "\n" +//The consumer-visible brand with which the product/hardware will be associated, if any.
                "Serial: " + Build.SERIAL + "\n" +
                "-------------------------------------\n";
        Log.e("Device Info", deviceInfo);
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_RESULT_CODE);
        } else {
            moveToLogin();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_RESULT_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (resultCode == RESULT_OK && Settings.canDrawOverlays(this)) {
                Utils.toast("Success", SplashActivity.this);
                moveToLogin();
            } else {
                Utils.toast("Can't use the app. Try again", SplashActivity.this);
                finish();
            }
        }
    }

    private void requestPermissions() {
        // Ask for permissions at runtime.
        // This is just an example set of permissions. Other permissions
        // may be needed, and please refer to our online documents.
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            if (isUserAlreadyLoggedIn())
                moveToDashboard();
            else
                moveToLogin();
        }
    }

    private boolean isUserAlreadyLoggedIn() {
        return PreferenceUtil.getBoolean(PrefConstants.PREF_IS_USER_LOGGED_IN, SplashActivity.this);
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
               /* showLongToast("Need permissions " + Manifest.permission.RECORD_AUDIO +
                        "/" + Manifest.permission.CAMERA + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                finish();*/
                showLongToast("Please grant the asked permissions to use the application features");
//                return;
            }

            // Here we continue only if all permissions are granted.
            // The permissions can also be granted in the system settings manually.
            moveToLogin();
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

    private void getCountryCode(Context context) {
        String iso = "null";

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        // Get network country iso
        if (telephonyManager.getNetworkCountryIso() != null && !telephonyManager.getNetworkCountryIso().equals("")) {
            iso = telephonyManager.getNetworkCountryIso();
        }
        Log.e("Code", "CountryIso: " + iso);
        Log.e("Code", "NetworkOperator: " + telephonyManager.getNetworkOperator());
    }
}