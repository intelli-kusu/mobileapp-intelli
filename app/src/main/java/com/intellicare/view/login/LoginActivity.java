package com.intellicare.view.login;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.intellicare.R;
import com.intellicare.databinding.ActivityLoginBinding;
import com.intellicare.model.other.PrefConstants;
import com.intellicare.model.response.CheckCredentialsResponse;
import com.intellicare.net.ServiceHelper;
import com.intellicare.utils.DialogUtil;
import com.intellicare.utils.KeyboardUtility;
import com.intellicare.utils.LocaleManager;
import com.intellicare.utils.PreferenceUtil;
import com.intellicare.utils.Utils;
import com.intellicare.view.base.BaseActivity;
import com.intellicare.view.settings.SettingsActivity;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private ActivityLoginBinding binding;
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
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getStringExtra("from").equalsIgnoreCase("login"))
                finish();
        }
    };

    private int selectedYear;
    private int selectedMonth;
    private int selectedDay;
    private boolean isDateSelected;
    private String appLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        if(PreferenceUtil.getBoolean(PrefConstants.PREF_WELCOME_NOTE_ACCEPTED, LoginActivity.this)) {
            binding.llWelcomeNote.setVisibility(View.GONE);
            binding.llLogin.setVisibility(View.VISIBLE);
//            binding.tvHeading.setVisibility(View.VISIBLE);
            binding.ivLogo.setVisibility(View.VISIBLE);
            loadFromPrefs();
        } else {
            binding.tvHeading.setVisibility(View.GONE);
            binding.ivLogo.setVisibility(View.GONE);
            binding.llLogin.setVisibility(View.GONE);
            binding.llWelcomeNote.setVisibility(View.VISIBLE);
            updateLanguageLabels();
        }

        try {
            Objects.requireNonNull(getSupportActionBar()).hide();
        } catch (Exception e) {
            e.printStackTrace();
        }

        binding.tvAbout.setOnClickListener(this);
        binding.btnGetOTP.setOnClickListener(this);
        binding.etDOB.setOnClickListener(this);
        binding.llMain.setOnClickListener(this);
        binding.btnContinue.setOnClickListener(this);
        binding.tvEnglish.setOnClickListener(this);
        binding.tvEspanol.setOnClickListener(this);
        binding.etDOB.addTextChangedListener(textWatcher);
        binding.etFirstName.addTextChangedListener(textWatcher);
        binding.etLastName.addTextChangedListener(textWatcher);
        binding.etPhoneNumber.addTextChangedListener(new TextChangeWatcher("US"));
        getFirebaseToken();
    }

    private void updateLanguageLabels() {
        appLanguage = LocaleManager.getLanguage(LoginActivity.this);
        if(appLanguage.equalsIgnoreCase(LocaleManager.ENGLISH)) {
            binding.tvEnglish.setTypeface(null, Typeface.BOLD);
            binding.tvEspanol.setTypeface(null, Typeface.NORMAL);
        } else {
            binding.tvEnglish.setTypeface(null, Typeface.NORMAL);
            binding.tvEspanol.setTypeface(null, Typeface.BOLD);
        }
    }

    private void loadFromPrefs() {
        try {
            Intent data =  getIntent();
            if(data != null) {
                String from = data.getStringExtra("from");
                if(!from.equalsIgnoreCase("splash")){
                    String fName = PreferenceUtil.getData(PrefConstants.PREF_FIRST_NAME, LoginActivity.this);
                    String lName = PreferenceUtil.getData(PrefConstants.PREF_LAST_NAME, LoginActivity.this);
                    String dob = PreferenceUtil.getData(PrefConstants.PREF_DOB, LoginActivity.this);
                    String phone = PreferenceUtil.getData(PrefConstants.PREF_PHONE_NUMBER, LoginActivity.this);

                    binding.etFirstName.setText(fName);
                    binding.etLastName.setText(lName);
                    binding.etDOB.setText(dob);
                    binding.etPhoneNumber.setText(phone);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.intellicare.ACTION_FINISH");
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tvAbout) {
            KeyboardUtility.hideKeyboard(LoginActivity.this, binding.llMain);
            redirectToAboutScreen();
        } else if (view.getId() == R.id.btnGetOTP) {
            KeyboardUtility.hideKeyboard(LoginActivity.this, binding.llMain);
            checkUserCredentialsServiceCall();
        } else if (view.getId() == R.id.etDOB) {
            KeyboardUtility.hideKeyboard(LoginActivity.this, binding.llMain);
            showDatePicker();
        } else if (view.getId() == R.id.llMain) {
            KeyboardUtility.hideKeyboard(LoginActivity.this, binding.llMain);
        } else if (view.getId() == R.id.btnContinue) {
            PreferenceUtil.saveBoolean(PrefConstants.PREF_WELCOME_NOTE_ACCEPTED, true, LoginActivity.this);
            binding.llWelcomeNote.setVisibility(View.GONE);
            binding.llLogin.setVisibility(View.VISIBLE);
//            binding.tvHeading.setVisibility(View.VISIBLE);
            binding.ivLogo.setVisibility(View.VISIBLE);
        } else if (view.getId() == R.id.tvEnglish) {
            appLanguage = LocaleManager.ENGLISH;
            LocaleManager.setAppLocale(LoginActivity.this, appLanguage);
            recreate();
        } else if (view.getId() == R.id.tvEspanol) {
            appLanguage = LocaleManager.SPANISH;
            LocaleManager.setAppLocale(LoginActivity.this, appLanguage);
            recreate();
        }
    }

    private void redirectToAboutScreen() {
        Intent intent = new Intent(LoginActivity.this, AboutActivity.class);
        startActivity(intent);
    }


    private void showDatePicker() {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear;
        int mMonth;
        int mDay;
        if (isDateSelected) {
            mYear = selectedYear;
            mMonth = selectedMonth - 1;
            mDay = selectedDay;
            c.set(Calendar.YEAR, c.get(Calendar.YEAR) - 10);
        } else {
            mYear = c.get(Calendar.YEAR) - 10;
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            c.set(Calendar.YEAR, mYear);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        selectedYear = year;
                        selectedMonth = monthOfYear + 1;
                        selectedDay = dayOfMonth;
                        isDateSelected = true;
                        binding.etDOB.setText(String.format(Locale.US, "%02d/%02d/%d", monthOfYear + 1, dayOfMonth, year));
                    }
                }, mYear, mMonth, mDay);

        datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        datePickerDialog.show();
    }

    private boolean isValidForm() {
        String phoneNumber = binding.etPhoneNumber.getText().toString().trim().replaceAll("\\D", "");
        String dob = binding.etDOB.getText().toString().trim();
        String firstName = binding.etFirstName.getText().toString().trim();
        String lastName = binding.etLastName.getText().toString().trim();
        return !TextUtils.isEmpty(dob) && !TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName) && !TextUtils.isEmpty(phoneNumber) && phoneNumber.length() >= 10;
    }

    private void moveToVerifyAndLogin() {
        Intent intent = new Intent(LoginActivity.this, OTPVerificationActivity.class);
        startActivityForResult(intent, 2000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            binding.etFirstName.getText().clear();
            binding.etLastName.getText().clear();
            binding.etDOB.getText().clear();
            binding.etPhoneNumber.getText().clear();
        }
    }

    private void getFirebaseToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.e("TOKEN", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        PreferenceUtil.saveData(PrefConstants.PREF_FCM_TOKEN, token, LoginActivity.this);
                        Log.e("TOKEN", "FCM registration Token" + token);
                    }
                });
    }

    //04Feb2021 New service flow
    private void checkUserCredentialsServiceCall() {
        String phoneNumber = binding.etPhoneNumber.getText().toString().trim();//.replaceAll("\\D","");
        //String dob = binding.etDOB.getText().toString().trim().replace("-", "/");
        String dob = binding.etDOB.getText().toString().trim(); //selectedDOB;
        String firstName = binding.etFirstName.getText().toString().trim(); //TODO
        String lastName = binding.etLastName.getText().toString().trim();
        String deviceToken = PreferenceUtil.getData(PrefConstants.PREF_FCM_TOKEN, LoginActivity.this);
        DialogUtil.showProgressDialog(this, getResources().getString(R.string.common_loader_title), getString(R.string.please_wait));

        Call<CheckCredentialsResponse> call = new ServiceHelper().checkCredentials2(firstName, lastName, dob, phoneNumber, deviceToken);
        call.enqueue(new Callback<CheckCredentialsResponse>() {
            @Override
            public void onResponse(@NonNull Call<CheckCredentialsResponse> call, @NonNull Response<CheckCredentialsResponse> response) {
                DialogUtil.hideProgressDialog();

                if (response.isSuccessful()) {
                    CheckCredentialsResponse result = response.body();
                    if (result != null && result.getStatus().equalsIgnoreCase("success")) {
                        analyzeTheUserCredentials(result);
                        savePrefs(result);
                        moveToVerifyAndLogin();
                    } else {
                        Utils.toast("Server error", LoginActivity.this);
                        savePrefs(null);
                    }
                } else {
                    if (response.code() == 404) {
                        String errorMsg = "Your member verification has failed. Our Care Coordinator will get in touch with you shortly.";
                        Utils.toast(errorMsg, LoginActivity.this);
                    } else {
                        Utils.toast("Server error", LoginActivity.this);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CheckCredentialsResponse> call, @NonNull Throwable t) {
                DialogUtil.hideProgressDialog();

                Utils.toast("Network error", LoginActivity.this);
            }
        });
    }

    private void analyzeTheUserCredentials(CheckCredentialsResponse result) {
        PreferenceUtil.saveBoolean(PrefConstants.PREF_IS_EMPTY_FIELD_AVAILABLE, result.getEmptyFields() != 0, LoginActivity.this);
    }

    private void savePrefs(CheckCredentialsResponse result) {
        if (result == null) { //clear prefs
            PreferenceUtil.saveData(PrefConstants.PREF_APP_TOKEN, "", LoginActivity.this);
            PreferenceUtil.saveData(PrefConstants.PREF_PATIENT_ID, "", LoginActivity.this);
            PreferenceUtil.saveData(PrefConstants.PREF_DOB, "", LoginActivity.this);
            PreferenceUtil.saveData(PrefConstants.PREF_PHONE_NUMBER, "", LoginActivity.this);
            PreferenceUtil.saveData(PrefConstants.PREF_FIRST_NAME, "", LoginActivity.this);
            PreferenceUtil.saveData(PrefConstants.PREF_LAST_NAME, "", LoginActivity.this);
        } else { //update prefs
            PreferenceUtil.saveData(PrefConstants.PREF_APP_TOKEN, result.getToken(), LoginActivity.this);
            PreferenceUtil.saveData(PrefConstants.PREF_PATIENT_ID, result.getPatientId(), LoginActivity.this);
            PreferenceUtil.saveData(PrefConstants.PREF_DOB, binding.etDOB.getText().toString().trim(), LoginActivity.this);
            PreferenceUtil.saveData(PrefConstants.PREF_PHONE_NUMBER, binding.etPhoneNumber.getText().toString().trim(), LoginActivity.this);
            PreferenceUtil.saveData(PrefConstants.PREF_FIRST_NAME, binding.etFirstName.getText().toString().trim(), LoginActivity.this);
            PreferenceUtil.saveData(PrefConstants.PREF_LAST_NAME, binding.etLastName.getText().toString().trim(), LoginActivity.this);
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