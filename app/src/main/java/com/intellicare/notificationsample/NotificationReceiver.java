package com.intellicare.notificationsample;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("NotificationReceiver", System.currentTimeMillis()+"");
        int notificationId = intent.getIntExtra("id", 0);
        String tag = intent.getStringExtra("tag");
        clearNotification(context);

        if(notificationId == 1000) {
            Intent intentA = new Intent(context, NotificationActivityA.class);
            intentA.putExtra("tag", tag);
            context.startActivity(intentA);
        } else if(notificationId == 1001) {
            Intent intentB = new Intent(context, NotificationActivityB.class);
            intentB.putExtra("tag", tag);
            context.startActivity(intentB);
        }
    }

    private void clearNotification(Context context) {
        try {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancelAll();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}