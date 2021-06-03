package com.intellicare.notificationsample;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.intellicare.R;
import com.intellicare.view.videocall.RingerActivity;

public class CallService extends Service {
    public CallService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        RemoteViews customView = new RemoteViews(getPackageName(), R.layout.custom_call_notification);
        Intent notificationIntent = new Intent(this, RingerActivity.class);
        Intent hangupIntent = new Intent(this, NotificationActivityB.class);
        Intent answerIntent = new Intent(this, NotificationActivityA.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent hangupPendingIntent = PendingIntent.getActivity(this, 0,
                hangupIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent answerPendingIntent = PendingIntent.getActivity(this, 0,
                answerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        customView.setOnClickPendingIntent(R.id.btnAnswer, answerPendingIntent);
        customView.setOnClickPendingIntent(R.id.btnDecline, hangupPendingIntent);
        showNotification(customView, pendingIntent, hangupPendingIntent);
        return START_STICKY;
    }

    private void showNotification(RemoteViews customView, PendingIntent pendingIntent, PendingIntent hangupPendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel("IncomingCall",
                    "IncomingCall", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setSound(null, null);
            notificationManager.createNotificationChannel(notificationChannel);
            NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "IncomingCall");
            notification.setContentTitle("Test1234");
            notification.setTicker("Call_STATUS");
            notification.setContentText("Incoming call from IntelliCare");
            notification.setSmallIcon(R.drawable.ic_notification_bg);
            notification.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);
            notification.setCategory(NotificationCompat.CATEGORY_CALL);
            notification.setVibrate(null);
            notification.setOngoing(true);
            notification.setFullScreenIntent(pendingIntent, true);
            notification.setPriority(NotificationCompat.PRIORITY_MAX);
            notification.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
            notification.setCustomContentView(customView);
            notification.setCustomBigContentView(customView);

            startForeground(1124, notification.build());
        } else {
            NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
            notification.setContentTitle("Test1234");
            notification.setTicker("Call_STATUS");
            notification.setContentText("IncomingCall");
            notification.setSmallIcon(R.drawable.ic_notification_bg);
            notification.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_avatar));
            notification.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);
            notification.setVibrate(null);
            notification.setContentIntent(pendingIntent);
            notification.setOngoing(true);
            notification.setCategory(NotificationCompat.CATEGORY_CALL);
            notification.setPriority(NotificationCompat.PRIORITY_MAX);

            NotificationCompat.Action hangupAction = new NotificationCompat.Action.Builder(android.R.drawable.sym_action_chat,
                    "HANG UP", hangupPendingIntent).build();
            notification.addAction(hangupAction);
            startForeground(1124, notification.build());
        }
    }
}