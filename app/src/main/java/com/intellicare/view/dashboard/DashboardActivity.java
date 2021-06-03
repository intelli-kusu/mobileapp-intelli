package com.intellicare.view.dashboard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import com.intellicare.R;
import com.intellicare.databinding.ActivityDashboardBinding;
import com.intellicare.model.other.AppConstants;
import com.intellicare.model.other.PrefConstants;
import com.intellicare.model.patientinfonew.Member;
import com.intellicare.model.patientinfonew.PatientInfoNew;
import com.intellicare.model.response.CommonResponse;
import com.intellicare.model.visitinfo.VisitInfoResponse;
import com.intellicare.net.ServiceHelper;
import com.intellicare.utils.DialogUtil;
import com.intellicare.utils.LocaleManager;
import com.intellicare.utils.PreferenceUtil;
import com.intellicare.utils.Utils;
import com.intellicare.view.base.BaseActivity;
import com.intellicare.view.form.UserFormActivity;
import com.intellicare.view.login.LoginActivity;
import com.intellicare.view.settings.SettingsActivity;
import com.intellicare.view.videocall.VideoChatActivity;
import com.intellicare.view.visit.CreateVisitActivity;
import com.intellicare.view.visit.PastVisitsActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends BaseActivity implements View.OnClickListener {
    private static final int PERMISSION_REQ_ID = 1000;
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private ActivityDashboardBinding binding;
    private PatientInfoNew patientInfoNew;
    private String agoraChannel = "";
    private String agoraAuthToken = "";
    private boolean isFromFCM;
    private DialogClick DIALOG_CLICK = DialogClick.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            boolean showAnim = getIntent().getBooleanExtra("from", false);
            if (showAnim)
                getWindow().setWindowAnimations(android.R.style.Animation_Toast);
        } catch (Exception e) {
            e.printStackTrace();
        }

        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        PreferenceUtil.saveBoolean(PrefConstants.PREF_IS_APP_FOREGROUND, true, DashboardActivity.this);
        binding.llCreateVisit.setOnClickListener(this);
        binding.llUpcomingVisit.setOnClickListener(this);
        binding.llConsultationHistory.setOnClickListener(this);
        binding.llManageProfile.setOnClickListener(this);
        binding.tvSettings.setOnClickListener(this);
        binding.ivCancel.setOnClickListener(this);
        binding.ivCreateVisit.setOnClickListener(this);
        getIntentDataIfAvailable();
        updateUserNameAndId();

//        getPatientInfoNew();
    }

    private void showConsultationPanel(boolean isVisible) {
        binding.flConsultation.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    private void getIntentDataIfAvailable() {
        Intent data = getIntent();
        if (data != null) {
            isFromFCM = data.getBooleanExtra("from", false);
            if (isFromFCM) {
                agoraAuthToken = data.getStringExtra("token");
                agoraChannel = data.getStringExtra("channel");
                engageTheCall();
            } else {
                binding.llUpcomingVisit.setEnabled(false);
            }
        }
    }

    private void engageTheCall() {
        binding.tvVisitTitle.setText(getString(R.string.provider_ready_message));
        binding.tvVisitDesc.setText(getString(R.string.engage_text));
        binding.llUpcomingVisit.setEnabled(true);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ivCreateVisit) {
            moveToCreateVisit();
        } else if (view.getId() == R.id.llUpcomingVisit) {
            requestPermissions();
        } else if (view.getId() == R.id.llConsultationHistory) {
            moveToPastVisits();
        } else if (view.getId() == R.id.llManageProfile) {
            moveToUserFormScreen();
        }  else if (view.getId() == R.id.tvSettings) {
            moveToSettingsScreen();
        } /*else if (view.getId() == R.id.tvLogout) {
            DIALOG_CLICK = DialogClick.LOGOUT;
            showAlertDialog("Warning!", "Do you want to logout?", "LOGOUT", "CANCEL", true);
        } */else if (view.getId() == R.id.ivCancel) {
            DIALOG_CLICK = DialogClick.CANCEL_VISIT;
            showAlertDialog(getString(R.string.warning_text), getString(R.string.consult_cancellation_warning_text), getString(R.string.yes), getString(R.string.no), true);
        }
    }

    private void moveToSettingsScreen() {
        Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void requestPermissions() {
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            moveToVideoCall();
        }
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                showLongToast("Need permissions " + Manifest.permission.RECORD_AUDIO +
                        "/" + Manifest.permission.CAMERA + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                finish();
                return;
            }

            // Here we continue only if all permissions are granted.
            // The permissions can also be granted in the system settings manually.
            moveToVideoCall();
        }
    }

    private void showLongToast(final String msg) {
        this.runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show());
    }

    private void moveToUserFormScreen() {
        Intent intent = new Intent(DashboardActivity.this, UserFormActivity.class);
        intent.putExtra("isFromDashboard", true);
        startActivity(intent);
    }

    private void moveToCreateVisit() {
        Intent intent = new Intent(DashboardActivity.this, CreateVisitActivity.class);
        startActivity(intent);
    }

    private void moveToPastVisits() {
        Intent intent = new Intent(DashboardActivity.this, PastVisitsActivity.class);
        startActivity(intent);
    }

    private void moveToVideoCall() {
        if (areTheTokenAndChannelValid()) {
            Intent intent = new Intent(DashboardActivity.this, VideoChatActivity.class);
            intent.putExtra("from", true);
            intent.putExtra("token", agoraAuthToken);
            intent.putExtra("channel", agoraChannel);
            intent.putExtra("isHold", "hold".equalsIgnoreCase(visitStatus));
            startActivity(intent);
            finish();
        } else {
            getVisitInfoServiceCall("0");
        }
    }

    private boolean areTheTokenAndChannelValid() {
        return !TextUtils.isEmpty(agoraAuthToken) && !TextUtils.isEmpty(agoraChannel);
    }

    private void logout() {
        PreferenceUtil.saveBoolean(PrefConstants.PREF_IS_USER_LOGGED_IN, false, DashboardActivity.this);
        Utils.toast("Logged out successfully", DashboardActivity.this);
        startActivity(new Intent(DashboardActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    //Not using
    private void getPatientInfoNew() { //03Jan
        DialogUtil.showProgressDialog(this, getResources().getString(R.string.common_loader_title), getString(R.string.please_wait));
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, DashboardActivity.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, DashboardActivity.this);
        Call<PatientInfoNew> call = new ServiceHelper().getPatientInfo(patient_id, token);
        call.enqueue(new Callback<PatientInfoNew>() {
            @Override
            public void onResponse(@NonNull Call<PatientInfoNew> call, @NonNull Response<PatientInfoNew> response) {
                DialogUtil.hideProgressDialog();
                if (response.isSuccessful()) {
                    patientInfoNew = response.body();
                    AppConstants.patientInfoNew = patientInfoNew;
//                    updateUserWelcomeName();
                    if (patientInfoNew.getPatient().getEmptyFields() != 0) {
                        binding.llManageProfile.performClick();
                    }
                } else {
                    Log.e("Dash", "onResponse: unsuccess");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PatientInfoNew> call, @NonNull Throwable t) {
                DialogUtil.hideProgressDialog();
                Log.e("Dash", "onFailure: " + t.getMessage());
            }
        });
    }

    private void updateUserWelcomeName() {
        Member m = AppConstants.patientInfoNew.getPatient().getMembersMap().get(/*AppConstants.PATIENT_ID*/ "Self");
        if (m != null) {
            String name = m.getFirstName() + " " + m.getLastName() + "!";
            binding.tvUserWelcomeName.append(name.trim());
        }
    }

    private void updateUserNameAndId() {
        String firstName = PreferenceUtil.getData(PrefConstants.PREF_FIRST_NAME, DashboardActivity.this);
        String lastName = PreferenceUtil.getData(PrefConstants.PREF_LAST_NAME, DashboardActivity.this);
        String patientId = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, DashboardActivity.this);
        String name = firstName + " " + lastName + "!";
        binding.tvUserWelcomeName.setText(String.format(getString(R.string.hi_text)+" %s", name.trim()));
//        binding.tvMemberId.setText(patientId);
    }

    private void getVisitInfoServiceCall(String consult_id) {
        DialogUtil.showProgressDialog(this, getResources().getString(R.string.common_loader_title), getString(R.string.please_wait));
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, DashboardActivity.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, DashboardActivity.this);
        Call<VisitInfoResponse> call = new ServiceHelper().getVisitInfo(patient_id, consult_id, token);
        call.enqueue(new Callback<VisitInfoResponse>() {
            @Override
            public void onResponse(@NonNull Call<VisitInfoResponse> call, @NonNull Response<VisitInfoResponse> response) {
                DialogUtil.hideProgressDialog();
                if (response.isSuccessful()) {
                    updateUpcomingVisitUI(response.body());
                } else {
                    updateUpcomingVisitUI(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<VisitInfoResponse> call, @NonNull Throwable t) {
                DialogUtil.hideProgressDialog();
                updateUpcomingVisitUI(null);
            }
        });
    }

    String visitStatus = null;
    private void updateUpcomingVisitUI(VisitInfoResponse body) {
//        showConsultationPanel(true);
        if (body == null || body.getStatus().equalsIgnoreCase("failure")) {
            if (body != null) {
                updateMemberId(body.getMemberId());
                updateLastVisitInfo(body.getLastConsultTime());
            }
            binding.llCreateVisit.setVisibility(View.VISIBLE);
            binding.llCreateVisitDesc.setVisibility(View.VISIBLE);
            binding.tvEngageVisitDescription.setVisibility(View.GONE);
            binding.llUpcomingVisit.setVisibility(View.GONE);
        } else {
            PreferenceUtil.saveData(PrefConstants.PREF_CURRENT_CONSULT_ID, body.getConsult().getConsultId(), DashboardActivity.this);
            agoraChannel = body.getConsult().getChannel();
            agoraAuthToken = body.getConsult().getToken();

            visitStatus = body.getConsult().getStatus();
            if (!TextUtils.isEmpty(visitStatus) && (visitStatus.equalsIgnoreCase("engage") ||
                    visitStatus.equalsIgnoreCase("hold") || visitStatus.equalsIgnoreCase("call"))) {
                engageTheCall();
                binding.llCreateVisit.setVisibility(View.GONE);
                binding.llCreateVisitDesc.setVisibility(View.GONE);
                binding.llUpcomingVisit.setVisibility(View.VISIBLE);
                binding.tvEngageVisitDescription.setVisibility(View.VISIBLE);
               /* binding.tvEngageVisitDescription.setText(HtmlCompat.fromHtml(
                        getString(R.string.formatted_engage_visit_description), HtmlCompat.FROM_HTML_MODE_COMPACT
                ));*/
            } else if (!TextUtils.isEmpty(visitStatus) && visitStatus.equalsIgnoreCase("waiting")) {
                String waitTime =body.getConsult().getWaitTime() + " "+getString(R.string.minutes_text);
                binding.llCreateVisit.setVisibility(View.GONE);
                binding.llCreateVisitDesc.setVisibility(View.GONE);
                binding.llUpcomingVisit.setVisibility(View.VISIBLE);
                binding.llUpcomingVisit.setEnabled(false);
                binding.tvVisitTitle.setText(getString(R.string.consultation_begin_note));
                binding.tvVisitDesc.setText(waitTime);
                binding.tvEngageVisitDescription.setVisibility(View.VISIBLE);
                /*binding.tvEngageVisitDescription.setText(HtmlCompat.fromHtml(
                        getString(R.string.formatted_engage_visit_description), HtmlCompat.FROM_HTML_MODE_COMPACT
                ));*/
            } else if (!TextUtils.isEmpty(visitStatus) && visitStatus.equalsIgnoreCase("completed")) {
                binding.llCreateVisit.setVisibility(View.VISIBLE);
                binding.llCreateVisitDesc.setVisibility(View.VISIBLE);
                binding.llUpcomingVisit.setVisibility(View.GONE);
                binding.tvEngageVisitDescription.setVisibility(View.GONE);
            }

            updateMemberId(body.getMemberId());
            updateLastVisitInfo(body.getLastConsultTime());
        }
    }

    private void updateMemberId(String memberId) {
        if (/*binding.llCreateVisit.getVisibility() == View.VISIBLE &&*/ !TextUtils.isEmpty(memberId))
            binding.tvMemberId.setText(memberId);
    }

    private void updateLastVisitInfo(String lastConsultTime) {
        if (!TextUtils.isEmpty(lastConsultTime))
            binding.tvLastConsultationTime.setText(lastConsultTime);
    }

    private void cancelVisitServiceCall() {
        DialogUtil.showProgressDialog(this, getResources().getString(R.string.common_loader_title), getString(R.string.please_wait));
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, DashboardActivity.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, DashboardActivity.this);
        String consult_id = PreferenceUtil.getData(PrefConstants.PREF_CURRENT_CONSULT_ID, DashboardActivity.this);
        Call<CommonResponse> call = new ServiceHelper().cancelVisit(patient_id, consult_id, token);
        call.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(@NonNull Call<CommonResponse> call, @NonNull Response<CommonResponse> response) {
                DialogUtil.hideProgressDialog();
                if (response.isSuccessful()) {
                    Utils.toast(getString(R.string.cancel_consultation_success_message), DashboardActivity.this);
                    updateUpcomingVisitUI(null);
                } else {
                    updateUpcomingVisitUI(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CommonResponse> call, @NonNull Throwable t) {
                DialogUtil.hideProgressDialog();
                updateUpcomingVisitUI(null);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocaleManager.mainAppLang = LocaleManager.getLanguage(this);
        PreferenceUtil.saveBoolean(PrefConstants.PREF_IS_APP_FOREGROUND, true, DashboardActivity.this);
//        showConsultationPanel(false);

        getVisitInfoServiceCall("0");

    }

    @Override
    protected void onStop() {
        PreferenceUtil.saveBoolean(PrefConstants.PREF_IS_APP_FOREGROUND, false, DashboardActivity.this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        PreferenceUtil.saveBoolean(PrefConstants.PREF_IS_APP_FOREGROUND, false, DashboardActivity.this);
        super.onDestroy();
    }

    @Override
    protected void onPositiveClick() {
        if (DIALOG_CLICK == DialogClick.LOGOUT) {
            logout();
        } else if (DIALOG_CLICK == DialogClick.CANCEL_VISIT) {
            cancelVisitServiceCall();
        } else if (DIALOG_CLICK == DialogClick.EXIT) {
            finish();
        }
    }

    @Override
    protected void onNegativeClick() {

    }

    @Override
    public void onBackPressed() {
        DIALOG_CLICK = DialogClick.EXIT;
        showAlertDialog(getString(R.string.warning_text),getString(R.string.exit_app_warning_message),  getString(R.string.exit_text), getString(R.string.cancel_text), true);
    }

    private enum DialogClick {
        LOGOUT, CANCEL_VISIT, NONE, EXIT
    }
}