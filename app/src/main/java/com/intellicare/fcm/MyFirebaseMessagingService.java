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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.intellicare.samples.NoUiActivity;
import com.intellicare.R;
import com.intellicare.model.other.PrefConstants;
import com.intellicare.model.fcmdata.FcmData;
import com.intellicare.utils.PreferenceUtil;
import com.intellicare.view.videocall.RingerActivity;
import com.intellicare.view.dashboard.DashboardActivity;
import com.intellicare.view.videocall.VideoChatActivity;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private String agoraChannel = "";
    private String agoraAuthToken = "";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        clearNotification();
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

                try {
                    Thread.sleep(5000);
                    //when app foreground and in Dashboard refresh visit info
                    if (PreferenceUtil.getBoolean(PrefConstants.PREF_IS_APP_FOREGROUND, this)) {
                        redirectToDashboard();
                    } else {
                        if (fcmData != null) {
                            sendNotification(fcmData.getData().getBody());
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else if (!TextUtils.isEmpty(type) && type.equalsIgnoreCase("second")) {
                agoraChannel = data.get("channel");
                agoraAuthToken = data.get("token");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    sendNotificationOnDevicesTargetAboveVersionQ();
                else //redirect to ringer activity straight away
                    redirectToRingerScreen();
            } else {
                //provider resumes session
                //when app foreground and in VideoChat close hold message screen
                if (PreferenceUtil.getBoolean(PrefConstants.PREF_IS_APP_FOREGROUND, this)) {
                    sendBroadcastOnCallResume();
                } else {
                    if (fcmData != null) {
                        sendNotification(fcmData.getData().getBody());
                    }
                }
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
            Intent intent = new Intent(getApplicationContext(), RingerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("from", true);
            intent.putExtra("token", agoraAuthToken);
            intent.putExtra("channel", agoraChannel);

            startActivity(intent);
        } else {
            Log.e(TAG, "Incomplete data payload from FCM");
        }
    }

    private void redirectToDashboard() {
        Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from", true);
        startActivity(intent);
    }

    private void sendBroadcastOnCallResume() {
        Intent broadcast = new Intent("hold");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
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

    private void sendNotificationOnDevicesTargetAboveVersionQ() {
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
                .setOngoing(false)
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

        Intent rejectIntent = new Intent(this, NoUiActivity.class);
        rejectIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        rejectIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        PendingIntent rejectPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1,
                rejectIntent, PendingIntent.FLAG_ONE_SHOT);
        /*PendingIntent rejectPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1,
                new Intent(), PendingIntent.FLAG_ONE_SHOT);*/
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
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(1000, notificationBuilder.build());
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


    private void clearNotification() {
        try {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancelAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}