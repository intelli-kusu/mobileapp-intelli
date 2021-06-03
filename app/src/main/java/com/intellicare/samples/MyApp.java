package com.intellicare.samples;

import android.app.Application;

import com.intellicare.fcm.CallRejectReceiver;

public class MyApp extends Application {
    private CallRejectReceiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();
        receiver = new CallRejectReceiver();
        registerReceiver(receiver, null);
    }
}
