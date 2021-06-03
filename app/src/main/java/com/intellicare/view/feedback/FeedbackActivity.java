package com.intellicare.view.feedback;

import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.intellicare.R;
import com.intellicare.databinding.ActivityFeedbackBinding;
import com.intellicare.model.other.PrefConstants;
import com.intellicare.model.response.CommonResponse;
import com.intellicare.net.ServiceHelper;
import com.intellicare.utils.DialogUtil;
import com.intellicare.utils.KeyboardUtility;
import com.intellicare.utils.PreferenceUtil;
import com.intellicare.utils.Utils;
import com.intellicare.view.base.BaseActivity;
import com.intellicare.view.dashboard.DashboardActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackActivity extends BaseActivity implements View.OnClickListener {
    private ActivityFeedbackBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        getWindow().setWindowAnimations(android.R.style.Animation_Toast);

        doSetup();
        binding = ActivityFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        doRatingStuff();
        binding.btnSubmit.setOnClickListener(this);
        binding.tvSkip.setOnClickListener(this);
        binding.llMain.setOnClickListener(this);
    }

    private void doRatingStuff() {
        binding.ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                binding.tvRatingScale.setText(String.valueOf(v));
                switch ((int) ratingBar.getRating()) {
                    case 1:
                        binding.tvRatingScale.setText("Very bad");
                        break;
                    case 2:
                        binding.tvRatingScale.setText("Need some improvement");
                        break;
                    case 3:
                        binding.tvRatingScale.setText("Good");
                        break;
                    case 4:
                        binding.tvRatingScale.setText("Great");
                        break;
                    case 5:
                        binding.tvRatingScale.setText("Awesome. I love it");
                        break;
                    default:
                        binding.tvRatingScale.setText("");
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSubmit) {
            submitFeedback();
        } else if (v.getId() == R.id.tvSkip) {
            moveToUserOptionsActivity();
        } else if (v.getId() == R.id.llMain) {
            KeyboardUtility.hideKeyboard(FeedbackActivity.this, binding.llMain);
        }
    }

    private void submitFeedback() {
        int stars = (int) binding.ratingBar.getRating();
        String feedback = binding.etFeedback.getText().toString().trim();
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, FeedbackActivity.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, FeedbackActivity.this);
        String consult_id = PreferenceUtil.getData(PrefConstants.PREF_CURRENT_CONSULT_ID, FeedbackActivity.this);

        if (TextUtils.isEmpty(feedback)) {
            Utils.toast(getString(R.string.feedback_empty_message), FeedbackActivity.this);
            return;
        }

        DialogUtil.showProgressDialog(this, getResources().getString(R.string.common_loader_title), getString(R.string.please_wait));


        Call<CommonResponse> call = new ServiceHelper().feedback(patient_id, stars + "", feedback, consult_id, token);
        call.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(@NonNull Call<CommonResponse> call, @NonNull Response<CommonResponse> response) {
                DialogUtil.hideProgressDialog();
                if (response.isSuccessful()) {
                    Utils.toast(getString(R.string.feedback_success), FeedbackActivity.this);
                    moveToUserOptionsActivity();
                } else {
                    Utils.toast("Server error", FeedbackActivity.this);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CommonResponse> call, @NonNull Throwable t) {
                DialogUtil.hideProgressDialog();
                Log.e("Feedback", t.getMessage());
                Utils.toast("Network error", FeedbackActivity.this);
            }
        });
    }

    private void moveToUserOptionsActivity() {
        if (!isDeviceScreenLocked()) {
            Intent intent = new Intent(FeedbackActivity.this, DashboardActivity.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
//        PreferenceUtil.saveData(PrefConstants.PREF_CURRENT_CONSULT_ID, "", FeedbackActivity.this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        moveToUserOptionsActivity();
        super.onBackPressed();
    }

    private void doSetup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    private boolean isDeviceScreenLocked() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        return keyguardManager.isKeyguardLocked();
    }

}