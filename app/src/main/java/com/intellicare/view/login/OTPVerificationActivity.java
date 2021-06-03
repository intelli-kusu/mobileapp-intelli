package com.intellicare.view.login;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.intellicare.R;
import com.intellicare.databinding.ActivityOtpVerificationBinding;
import com.intellicare.model.other.PrefConstants;
import com.intellicare.model.response.CommonResponse;
import com.intellicare.model.response.SendOTPResponse;
import com.intellicare.model.response.SendOTPResponse2;
import com.intellicare.net.ServiceHelper;
import com.intellicare.utils.DialogUtil;
import com.intellicare.utils.KeyboardUtility;
import com.intellicare.utils.PreferenceUtil;
import com.intellicare.utils.Utils;
import com.intellicare.view.base.BaseActivity;
import com.intellicare.view.dashboard.DashboardActivity;
import com.intellicare.view.form.UserFormActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OTPVerificationActivity extends BaseActivity implements View.OnClickListener {
    private NotificationManager notificationManager;
    private ActivityOtpVerificationBinding binding;
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            binding.btnVerifyOTP.setEnabled(isValidForm());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("com.intellicare.ACTION_OTP")) {
                String otp = intent.getStringExtra("otp");
                binding.etOTP.setText(otp);
            }
        }
    };
    private String phoneNumber;
    private String patient_id;
    private String token;
    private String last_name;
    private String dob;
    private boolean isEmptyFieldAvailable = false;

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(OTPVerificationActivity.this).registerReceiver(receiver, new IntentFilter("com.intellicare.ACTION_OTP"));
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(OTPVerificationActivity.this).unregisterReceiver(receiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        try {
            getSupportActionBar().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
        getPrefs();
        binding.tvHeading.append("\n" + phoneNumber);
        binding.btnVerifyOTP.setOnClickListener(this);
        binding.tvResendOTP.setOnClickListener(this);
        binding.btnChangePhoneNumber.setOnClickListener(this);
        binding.llMain.setOnClickListener(this);
        binding.etOTP.addTextChangedListener(textWatcher);
//        startSmsRetrieverClient();
    }

    private void getPrefs() {
        phoneNumber = PreferenceUtil.getData(PrefConstants.PREF_PHONE_NUMBER, OTPVerificationActivity.this);
        patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, OTPVerificationActivity.this);
        token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, OTPVerificationActivity.this);
        last_name = PreferenceUtil.getData(PrefConstants.PREF_LAST_NAME, OTPVerificationActivity.this);
        dob = PreferenceUtil.getData(PrefConstants.PREF_DOB, OTPVerificationActivity.this);
        isEmptyFieldAvailable = PreferenceUtil.getBoolean(PrefConstants.PREF_IS_EMPTY_FIELD_AVAILABLE, OTPVerificationActivity.this);
    }

    private void startSmsRetrieverClient() {
        // Get an instance of SmsRetrieverClient, used to start listening for a matching SMS message.
        SmsRetrieverClient client = SmsRetriever.getClient(this /* context */);

        // Starts SmsRetriever, which waits for ONE matching SMS message until timeout (5 minutes).
        // The matching SMS message will be sent via a Broadcast Intent with action
        // SmsRetriever#SMS_RETRIEVED_ACTION.
        Task<Void> task = client.startSmsRetriever();

        // Listen for success/failure of the start Task. If in a background thread, this
        // can be made blocking using Tasks.await(task, [timeout]);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Successfully started retriever, expect broadcast intent
                Log.e("Sample", "onSuccess");
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed to start retriever, inspect Exception for more details
                Log.e("Sample", "onFailure");
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnVerifyOTP) {
            KeyboardUtility.hideKeyboard(OTPVerificationActivity.this, binding.llMain);
            verifyOTPServiceCall();
        } else if (view.getId() == R.id.tvResendOTP) {
//            KeyboardUtility.hideKeyboard(OTPVerificationActivity.this, binding.llMain);
            //service call
            resendOTP();
        } else if (view.getId() == R.id.btnChangePhoneNumber) {
            KeyboardUtility.hideKeyboard(OTPVerificationActivity.this, binding.llMain);
            finish();
        } else if (view.getId() == R.id.llMain) {
            KeyboardUtility.hideKeyboard(OTPVerificationActivity.this, binding.llMain);
        }
    }

    private void resendOTP() {
        try {
            binding.etOTP.getText().clear();
            resendOTPServiceCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateOTPTitleUI() {
        binding.tvHeading.setText(getString(R.string.otp_resend_note) + phoneNumber);
        binding.tvHeading.setTextColor(getResources().getColor(R.color.green));
    }

    private void verifyOTP() {
        if (last_name.equalsIgnoreCase("unknown") || last_name.equalsIgnoreCase("test")) {
            showAlertDialog(getString(R.string.sorry_title),
                    getString(R.string.sorry_alert), "OK", "", true);
        } else {
            verifyOTPServiceCall();
        }


        //failure: show error toast
        //Utils.toast("OTP is not valid", LoginActivity.this);
    }

    private void verifyOTPServiceCall() {
        String otp = binding.etOTP.getText().toString().trim();
        DialogUtil.showProgressDialog(this, getResources().getString(R.string.common_loader_title), getString(R.string.please_wait));

        Call<CommonResponse> call = new ServiceHelper().verifyOtp(patient_id, otp, token);
        call.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(@NonNull Call<CommonResponse> call, @NonNull Response<CommonResponse> response) {
                DialogUtil.hideProgressDialog();
                if (response.isSuccessful()) {
                    CommonResponse result = response.body();
                    if (result != null && result.getStatus().equalsIgnoreCase("success")) {
                        moveForward();
                    } else {
                        Utils.toast("Server error", OTPVerificationActivity.this);
                    }
                } else {
                    if (response.code() == 404) {
                        Utils.toast("Invalid OTP", OTPVerificationActivity.this);
                    } else {
                        Utils.toast("Server error", OTPVerificationActivity.this);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CommonResponse> call, @NonNull Throwable t) {
                DialogUtil.hideProgressDialog();
                Utils.toast("Network error", OTPVerificationActivity.this);
            }
        });
    }

    private void moveForward() {
        PreferenceUtil.saveBoolean(PrefConstants.PREF_IS_USER_LOGGED_IN, true, OTPVerificationActivity.this);
        if (isEmptyFieldAvailable) {
            moveToUserFormScreen();
        } else {
            moveToDashboardScreen();
        }
    }

    private boolean isValidForm() {
        String otp = binding.etOTP.getText().toString().trim();
        return !TextUtils.isEmpty(otp) && otp.length() == 5;
    }

    private void moveToUserFormScreen() {
        //send broadcast
        sendBroadcast(new Intent("com.intellicare.ACTION_FINISH").putExtra("from", "login"));
        Intent intent = new Intent(OTPVerificationActivity.this, UserFormActivity.class);
        startActivity(intent);
        finish();
    }

    private void moveToDashboardScreen() {
        //send broadcast
        sendBroadcast(new Intent("com.intellicare.ACTION_FINISH").putExtra("from", "login"));
        Intent intent = new Intent(OTPVerificationActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPositiveClick() {
        Intent i = new Intent();
        setResult(RESULT_OK, null);
        finish();
    }

    @Override
    protected void onNegativeClick() {

    }

    private void showNotification(String title, String content) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "otp")
                        .setSmallIcon(R.mipmap.ic_launcher_round) //set icon for notification
                        .setContentTitle(title) //set title of notification
                        .setContentText(content)//this is notification message
                        .setAutoCancel(true) // makes auto cancel of notification if we set below
                        .setContentIntent((PendingIntent.getActivity(this, 0, new Intent(), 0)))

                        .setPriority(NotificationCompat.PRIORITY_DEFAULT) //set priority of notification
                        .setDefaults(NotificationCompat.DEFAULT_ALL);



        /*Intent notificationIntent = new Intent(this, NotificationView.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //notification message will get at NotificationView
        notificationIntent.putExtra("message", "This is a notification message");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);*/

        // Add as notification
        //NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    private void createNotificationChannel(String name, String description) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /*CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);*/
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("otp", name, importance);
            channel.setDescription(description);
            channel.setShowBadge(true);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void resendOTPServiceCall() {
        DialogUtil.showProgressDialog(this, getResources().getString(R.string.common_loader_title), getString(R.string.please_wait));

        Call<SendOTPResponse2> call = new ServiceHelper().sendOtp2(patient_id, token);
        call.enqueue(new Callback<SendOTPResponse2>() {
            @Override
            public void onResponse(@NonNull Call<SendOTPResponse2> call, @NonNull Response<SendOTPResponse2> response) {
                DialogUtil.hideProgressDialog();
                if (response.isSuccessful()) {
                    SendOTPResponse2 result = response.body();
                    if (result != null && result.getStatus().equalsIgnoreCase("success")) {
                        updateOTPTitleUI();
                    } else {
                        Utils.toast("Server error", OTPVerificationActivity.this);
                    }
                } else {
                    Utils.toast("Server error", OTPVerificationActivity.this);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SendOTPResponse2> call, @NonNull Throwable t) {
                DialogUtil.hideProgressDialog();
                Utils.toast("Network error", OTPVerificationActivity.this);
            }
        });
    }

    private void savePrefs(SendOTPResponse result) {
        if (result == null) { //clear prefs
            PreferenceUtil.saveData(PrefConstants.PREF_APP_TOKEN, "", OTPVerificationActivity.this);
            PreferenceUtil.saveData(PrefConstants.PREF_PATIENT_ID, "", OTPVerificationActivity.this);
            PreferenceUtil.saveData(PrefConstants.PREF_DOB, "", OTPVerificationActivity.this);
            PreferenceUtil.saveData(PrefConstants.PREF_PHONE_NUMBER, "", OTPVerificationActivity.this);
            PreferenceUtil.saveData(PrefConstants.PREF_LAST_NAME, "", OTPVerificationActivity.this);
        } else { //update prefs
            PreferenceUtil.saveData(PrefConstants.PREF_APP_TOKEN, result.getToken(), OTPVerificationActivity.this);
            PreferenceUtil.saveData(PrefConstants.PREF_PATIENT_ID, result.getPatientId(), OTPVerificationActivity.this);
        }
    }
}