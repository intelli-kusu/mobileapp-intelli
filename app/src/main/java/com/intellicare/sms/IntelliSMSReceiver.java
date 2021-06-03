package com.intellicare.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BroadcastReceiver to wait for SMS messages. This can be registered either
 * in the AndroidManifest or at runtime.  Should filter Intents on
 * SmsRetriever.SMS_RETRIEVED_ACTION.
 */
public class IntelliSMSReceiver extends BroadcastReceiver {

    private final String TAG = "MySMSBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

            switch (status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    // Get SMS message contents
                    String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    // Extract one-time code from the message and complete verification
                    // by sending the code back to your server.
                    Log.e(TAG, "onReceive: " + message);
                    if (!TextUtils.isEmpty(message)) {
                        String OTP = parseCode(message);
                        Log.e(TAG, "onReceive: OTP" + OTP);

                        Intent broadcast = new Intent();
                        broadcast.setAction("com.intellicare.ACTION_OTP");
                        broadcast.putExtra("otp", OTP);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
                    }

                    break;
                case CommonStatusCodes.TIMEOUT:
                    // Waiting for SMS timed out (5 minutes)
                    // Handle the error ...
                    Log.e(TAG, "onReceive: TIMEOUT");
                    break;
            }
        }
    }

    private String parseCode(String message) {
        Pattern p = Pattern.compile("\\b\\d{5}\\b");
        Matcher m = p.matcher(message);
        String code = "";
        while (((Matcher) m).find()) {
            code = m.group(0);
        }
        return code;
    }
}