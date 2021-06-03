package com.intellicare.notificationsample;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.intellicare.R;
import com.intellicare.view.videocall.RingerActivity;

public class NotificationActivity extends AppCompatActivity {
private NotificationReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        findViewById(R.id.btnSendNotification).setOnClickListener(v ->
        {
            receiver = new NotificationReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.intellicare.accept");
            filter.addAction("com.intellicare.reject");
            registerReceiver(receiver, filter);
            new Handler(getMainLooper()).postDelayed(this::sendNotification,5000);

        });

    }

    @Override
    protected void onDestroy() {
        if(receiver != null)
            unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void sendNotification() {
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 1, new Intent(getApplicationContext(), NotificationActivity.class), 0);
        String channelId = "TestChannel-1234";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.chat1);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification_bg)
                .setContentTitle("Test Notification")
                .setContentText("Please click any button")
//                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) //set priority of notification
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setSound(defaultSoundUri, AudioManager.STREAM_RING)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bm))
                .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE);


        Intent intentA = new Intent("com.intellicare.accept");
        intentA.putExtra("id", 1000);
        intentA.putExtra("tag", "TAG1000");
        PendingIntent pendingIntentA = PendingIntent.getBroadcast(getApplicationContext(), 1, intentA, PendingIntent.FLAG_ONE_SHOT);
        notificationBuilder.addAction(new NotificationCompat.Action.Builder(android.R.drawable.sym_def_app_icon,
                "Accept", pendingIntentA).build());

        Intent intentB = new Intent("com.intellicare.reject");
        intentB.putExtra("id", 1001);
        intentB.putExtra("tag", "TAG1001");
        PendingIntent pendingIntentB = PendingIntent.getBroadcast(getApplicationContext(), 1, intentB, PendingIntent.FLAG_ONE_SHOT);
        notificationBuilder.addAction(new NotificationCompat.Action.Builder(android.R.drawable.sym_call_outgoing,
                "Reject", pendingIntentB).build());

        Intent intentFull = new Intent(getApplicationContext(), RingerActivity.class);
        intentFull.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intentFull.putExtra("token", "1003");
        intentFull.putExtra("channel", "TAG1003");
        PendingIntent pendingIntentFull = PendingIntent.getActivity(getApplicationContext(), 1, intentFull, 0);

        notificationBuilder.setContentIntent(pendingIntentFull);
        notificationBuilder.setFullScreenIntent(pendingIntentFull, true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Test Channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setShowBadge(true);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(1000, notificationBuilder.build());
    }

}