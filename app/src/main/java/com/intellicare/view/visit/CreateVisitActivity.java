package com.intellicare.view.visit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.intellicare.R;
import com.intellicare.databinding.ActivityCreateVisitBinding;
import com.intellicare.model.other.AppConstants;
import com.intellicare.model.other.PrefConstants;
import com.intellicare.model.createvisitmodel.CreateVisitResponse;
import com.intellicare.model.patientinfonew.Member;
import com.intellicare.net.ServiceHelper;
import com.intellicare.utils.DialogUtil;
import com.intellicare.utils.PreferenceUtil;
import com.intellicare.utils.Utils;
import com.intellicare.view.base.BaseActivity;
import com.intellicare.view.search.SearchActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateVisitActivity extends BaseActivity implements View.OnClickListener {
    private ActivityCreateVisitBinding binding;
    private String complaint_id;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateVisitBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(this);
        binding.btnSubmit.setOnClickListener(this);
        binding.etComplaint.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnBack) {
            finish();
        } else if (v.getId() == R.id.btnSubmit) {
            createVisitServiceCall();
        } else if (v.getId() == R.id.etComplaint) {
            Intent intent = new Intent(CreateVisitActivity.this, SearchActivity.class);
            startActivityForResult(intent, 9002);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 9002) {
                String complaint = data.getStringExtra("complaint");
                complaint_id = data.getStringExtra("complaint_id");
                if (!complaint.isEmpty()) {
                    binding.etComplaint.setText(complaint);
                    binding.btnSubmit.setEnabled(true);
                }
            }
        }
    }

    private void createVisitServiceCall() {
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, CreateVisitActivity.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, CreateVisitActivity.this);
        AppConstants.createVisitRequest.setPatientId(patient_id);
        AppConstants.createVisitRequest.setBp(binding.etBP.getText().toString().trim());
        AppConstants.createVisitRequest.setTemperature(binding.etTemperature.getText().toString().trim());
        AppConstants.createVisitRequest.setComplaintId(complaint_id);

        //TODO: Temp fix for request
        /*Member m  = AppConstants.patientInfoNew.getPatient().getMembersMap().get("Self");
        AppConstants.createVisitRequest.setPharmacyId("11678833");
        AppConstants.createVisitRequest.setPatientImage(m.getProfileImage());
        AppConstants.createVisitRequest.setGender(m.getGender());*/

        DialogUtil.showProgressDialog(this, getResources().getString(R.string.common_loader_title), getString(R.string.please_wait));
        Call<CreateVisitResponse> call = new ServiceHelper().createVisit(AppConstants.createVisitRequest, token);
        call.enqueue(new Callback<CreateVisitResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateVisitResponse> call, @NonNull Response<CreateVisitResponse> response) {
                DialogUtil.hideProgressDialog();

                if (response.isSuccessful()) {
                    CreateVisitResponse visitResponse = response.body();
                    if (visitResponse != null && visitResponse.getStatus().equalsIgnoreCase("success")) {
                        gotoDashboard(visitResponse);
                    } else {
                        Utils.toast("Server error", CreateVisitActivity.this);
                    }
                } else {
                    Utils.toast("Server error", CreateVisitActivity.this);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreateVisitResponse> call, @NonNull Throwable t) {
                DialogUtil.hideProgressDialog();
                Utils.toast("Network error", CreateVisitActivity.this);
            }
        });
    }

    private void gotoDashboard(CreateVisitResponse visitResponse) {
        PreferenceUtil.saveData(PrefConstants.PREF_CURRENT_CONSULT_ID, visitResponse.getVisitId(), CreateVisitActivity.this);
        Intent i = new Intent();
        setResult(RESULT_OK, i);
        finish();
    }
}