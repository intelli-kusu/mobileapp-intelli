package com.intellicare.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.intellicare.R;
import com.intellicare.model.other.PrefConstants;
import com.intellicare.utils.PreferenceUtil;
import com.intellicare.view.dashboard.DashboardActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MyFirebaseMessagingServiceBkp28Feb extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private String agoraChannel = "";
    private String agoraAuthToken = "";
    private String type = "";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ


        // Check if message contains a data payload.

        if (remoteMessage != null) {
            Log.e(TAG, "From: " + remoteMessage.getFrom());
            Map<String, String> data = remoteMessage.getData();
            Log.e(TAG, "onMessageReceived: "+data);
            if (data.size() > 0) {
                type = data.get("type");
                agoraChannel = data.get("channel");
                agoraAuthToken = data.get("token");
                String bodyData = data.get("body");
                if(!TextUtils.isEmpty(bodyData) && type.equalsIgnoreCase("second")) {
                    try {
                        assert bodyData != null;
                        JSONObject object = new JSONObject(bodyData).getJSONObject("data");
                        String title = object.optString("title");
                        String body = object.optString("body");
                        String timestamp = object.optString("timestamp");
                        Log.e("FCM body data ", "title "+title +", "+ "body "+body +", "+ "timestamp "+timestamp);

                        if(PreferenceUtil.getBoolean(PrefConstants.PREF_IS_APP_FOREGROUND, this)) {
                            handleNow();
                        } else {
                            sendNotification(!TextUtils.isEmpty(body) ? body : "Provider is ready for the call.");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


            } else {
                Log.e(TAG, "Message data payload is null");
            }
        }

        // Check if message contains a notification payload.
        //if (remoteMessage.getNotification() != null) {
            //String messageBody = remoteMessage.getNotification().getBody();
            //Log.e(TAG, "Message Notification Body: " + messageBody);
//            sendNotification(messageBody);
        //}

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


    // [START on_new_token]

    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token);
    }
    // [END on_new_token]


    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        if (!TextUtils.isEmpty(agoraAuthToken) && !TextUtils.isEmpty(agoraChannel)) {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("from", true);
            intent.putExtra("token", agoraAuthToken);
            intent.putExtra("channel", agoraChannel);
            startActivity(intent);
        } else {
            Log.e(TAG, "Incomplete data payload from FCM");
        }
    }

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM registration token with any
     * server-side account maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        boolean isLevel8Plus = false;
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from", true);//t
        intent.putExtra("token", agoraAuthToken);//t
        intent.putExtra("channel", agoraChannel);//t
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification_bg)
//                        .setContentTitle(getString(R.string.fcm_message))
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
            isLevel8Plus = true;
        }

        //dismiss push
        try {
            //dismissPush(notificationManager, notificationBuilder, isLevel8Plus);
        } catch (Exception e) {
            e.printStackTrace();
        }
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void dismissPush(NotificationManager mgr, NotificationCompat.Builder builder, boolean isLevel8Plus) {
        if (isLevel8Plus) {
            builder.setTimeoutAfter(10000);
        } else {
            new Handler().postDelayed(() -> mgr.cancel(0), 10000);
        }
    }
}