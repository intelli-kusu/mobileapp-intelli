package com.intellicare.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.intellicare.R;
import com.intellicare.model.other.PrefConstants;
import com.intellicare.model.fcmdata.FcmData;
import com.intellicare.notificationsample.CallService;
import com.intellicare.utils.PreferenceUtil;
import com.intellicare.view.videocall.RingerActivity;
import com.intellicare.view.dashboard.DashboardActivity;
import com.intellicare.view.videocall.VideoChatActivity;

import java.util.Map;

public class MyFirebaseMessagingServiceBkp11Mar extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private String agoraChannel = "";
    private String agoraAuthToken = "";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
//        wakeTheScreen();
        Log.e(TAG, "From: " + remoteMessage.getFrom());
        Map<String, String> data = remoteMessage.getData();
        Log.e(TAG, "onMessageReceived: " + data);
        if (data.size() > 0) {
            FcmData fcmData = null;
            String bodyData = data.get("body");
            if (!TextUtils.isEmpty(bodyData)) {
                fcmData = getFcmData(bodyData);
            }

            String type = data.get("type");
            if (!TextUtils.isEmpty(type) && type.equalsIgnoreCase("first")) {
                //when app foreground and in Dashboard refresh visit info
                if (PreferenceUtil.getBoolean(PrefConstants.PREF_IS_APP_FOREGROUND, this)) {
                    redirectToDashboard();
                } else {
                    if (fcmData != null) {
                        sendNotification(fcmData.getData().getBody());
                    }
                }
            } else if (!TextUtils.isEmpty(type) && type.equalsIgnoreCase("second")) {
                agoraChannel = data.get("channel");
                agoraAuthToken = data.get("token");
                //redirect to ringer activity straight away
                redirectToRingerScreen();
            }
        } else {
            Log.e(TAG, "Message data payload is null");
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    private FcmData getFcmData(String json) {
        return new Gson().fromJson(json, FcmData.class);
    }

    private void redirectToRingerScreen() {
        if (!TextUtils.isEmpty(agoraAuthToken) && !TextUtils.isEmpty(agoraChannel)) {
            Intent service = new Intent(this, CallService.class);
            Intent intent = new Intent(getApplicationContext(), RingerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("from", true);
            intent.putExtra("token", agoraAuthToken);
            intent.putExtra("channel", agoraChannel);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
//                showFullScreenIntent(intent);
                sendNotification();
//                startService(service);
            else
                startActivity(intent);
        } else {
            Log.e(TAG, "Incomplete data payload from FCM");
        }
    }

    private void redirectToDashboard() {
        Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from", true);
        startActivity(intent);
    }

    private void sendRegistrationToServer(String token) {
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification_bg)
                        .setContentTitle("IntelliCare")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0, notificationBuilder.build());
    }

    private void sendNotification() {
        String CHANNEL_ID = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_bg)
                .setContentTitle("IntelliCare")
                .setContentText("Health provider is ready")
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setOngoing(true)
                .setAutoCancel(true)
                .setSound(defaultSoundUri, AudioManager.STREAM_RING);


        Intent acceptIntent = new Intent(getApplicationContext(), VideoChatActivity.class);
        acceptIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        acceptIntent.putExtra("from", true);
        acceptIntent.putExtra("token", agoraAuthToken);
        acceptIntent.putExtra("channel", agoraChannel);
        PendingIntent acceptPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1,
                acceptIntent, PendingIntent.FLAG_ONE_SHOT);
        notificationBuilder.addAction(new NotificationCompat.Action.Builder(android.R.drawable.sym_action_call,
                "Accept", acceptPendingIntent).build());
        PendingIntent rejectPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1,
                new Intent(), PendingIntent.FLAG_ONE_SHOT);
        notificationBuilder.addAction(new NotificationCompat.Action.Builder(android.R.drawable.stat_notify_error,
                "Reject", rejectPendingIntent).build());

        Intent ringerIntent = new Intent(getApplicationContext(), RingerActivity.class);
        ringerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ringerIntent.putExtra("from", true);
        ringerIntent.putExtra("token", agoraAuthToken);
        ringerIntent.putExtra("channel", agoraChannel);
        PendingIntent pendingRingerIntent = PendingIntent.getActivity(getApplicationContext(), 1,
                ringerIntent, 0);

        notificationBuilder.setContentIntent(pendingRingerIntent);
        notificationBuilder.setFullScreenIntent(pendingRingerIntent, true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "IntelliCare Channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setShowBadge(true);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(1000, notificationBuilder.build());
    }


    private void showFullScreenIntent(Intent intent) {
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 1234,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent acceptIntent = new Intent(getApplicationContext(), VideoChatActivity.class);
        acceptIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        acceptIntent.putExtra("from", true);
        acceptIntent.putExtra("token", agoraAuthToken);
        acceptIntent.putExtra("channel", agoraChannel);
        PendingIntent acceptPendingIntent = PendingIntent.getActivity(this, 2345,
                acceptIntent, PendingIntent.FLAG_ONE_SHOT);
        String CHANNEL_ID = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_bg)
                        .setContentTitle("IntelliCare")
                        .setContentText("Health provider is ready")
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .addAction(R.drawable.btn_startcall_normal, "Accept", acceptPendingIntent)
                        .addAction(R.drawable.btn_endcall_normal, "Reject", null)
                        .setAutoCancel(true)

                        // Use a full-screen intent only for the highest-priority alerts where you
                        // have an associated activity that you would like to launch after the user
                        // interacts with the notification. Also, if your app targets Android 10
                        // or higher, you need to request the USE_FULL_SCREEN_INTENT permission in
                        // order for the platform to invoke this notification.
                        .setFullScreenIntent(fullScreenPendingIntent, true);

        Notification incomingCallNotification = notificationBuilder.build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0, incomingCallNotification);
    }

    private void wakeTheScreen() {
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        boolean isInteractive = pm.isInteractive();
        if (!isInteractive) {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "IntelliApp:WakeLock");
            wl.acquire(5000);
            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "IntelliApp:CpuWakeLock");
            wl_cpu.acquire(5000);
        }
    }
}