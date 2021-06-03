package com.intellicare.notificationsample;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import com.intellicare.R;

public class NotificationActivityA extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clearNotification();
        setContentView(R.layout.activity_notification_a);
    }

    private void clearNotification() {
        try {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancelAll();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}