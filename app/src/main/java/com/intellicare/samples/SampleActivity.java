package com.intellicare.samples;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.intellicare.databinding.ActivitySampleBinding;
import com.intellicare.utils.Utils;
import com.intellicare.view.base.BaseActivity;

import org.apache.commons.validator.GenericValidator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class SampleActivity extends BaseActivity {
    private ActivitySampleBinding binding;
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            binding.btnGetOTP.setEnabled(isValidForm());
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
                Utils.toast(otp, SampleActivity.this);
                binding.etDate2.setText(otp);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySampleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            Objects.requireNonNull(getSupportActionBar()).hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
        binding.etPhoneNumber.addTextChangedListener(new TextChangeWatcher("US"));
        binding.etDate2.addTextChangedListener(Mask.insert("##/##/####", binding.etDate2));
//        testDates();
        extractOTP();
//        new MyAppSignatureHelper(this).getAppSignatures();

    }

    private void extractOTP() {
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

    private void testDates() {
        boolean isDate20190228 = GenericValidator.isDate("2019-02-28", "yyyy-MM-dd", true);
        boolean isDate20190229 = GenericValidator.isDate("2019-02-29", "yyyy-MM-dd", true);
        boolean isDate20191232 = GenericValidator.isDate("2019/12/32", "yyyy/MM/dd", true);
        boolean isDate12151990 = GenericValidator.isDate("12/15/1990", "MM/dd/yyyy", true);


        Log.e("TAG", "testDates: isDate20190228 " + isDate20190228);
        Log.e("TAG", "testDates: isDate20190229 " + isDate20190229);
        Log.e("TAG", "testDates: isDate20191232 " + isDate20191232);
        Log.e("TAG", "testDates: isDate12151990 " + isDate12151990);
    }

    private boolean isValidForm() {
        String phoneNumber = binding.etPhoneNumber.getText().toString().trim().replaceAll("\\D", "");
        String firstName = binding.etFirstName.getText().toString().trim();
        String lastName = binding.etLastName.getText().toString().trim();
        return !TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName) && !TextUtils.isEmpty(phoneNumber) && phoneNumber.length() >= 10;
    }

    private boolean isValidDate(String date) {
        Log.e("TAG", "date " + date);
        boolean isDateValid = GenericValidator.isDate(date, "MM/dd/yyyy", true);
        return isDateValid && !isFutureDate(date);
    }

    private boolean isFutureDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String todayString = sdf.format(new Date());

        try {

            Date date1 = sdf.parse(date);
            Date date2 = sdf.parse(todayString);
            return date1.after(date2);
        } catch (Exception e) {
            return true;
        }
    }

    private boolean showError() {
        Editable editableDate = binding.etDate.getText();
        if (editableDate != null) {
            String date = editableDate.toString().trim();
            if (date.length() == 10) {
                if (isValidDate(date)) {
                    Utils.toast("Valid date", SampleActivity.this);
                    return true;
                } else {
                    Utils.toast("Invalid date", SampleActivity.this);
                    return false;
                }
            }
        } else {
            return false;
        }
        return false;
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(SampleActivity.this).registerReceiver(receiver, new IntentFilter("com.intellicare.ACTION_OTP"));
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(SampleActivity.this).unregisterReceiver(receiver);
    }

    class DateTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            boolean isError = showError();
            binding.btnGetOTP.setEnabled(isValidForm() && !isError);
        }
    }

    class TextChangeWatcher extends PhoneNumberFormattingTextWatcher {
        public TextChangeWatcher(String us) {
            super(us);
        }

        @Override
        public synchronized void afterTextChanged(Editable s) {
            super.afterTextChanged(s);

            binding.btnGetOTP.setEnabled(isValidForm());
        }
    }
}