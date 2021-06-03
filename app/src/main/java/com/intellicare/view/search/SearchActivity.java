package com.intellicare.view.search;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;

import com.google.gson.Gson;
import com.intellicare.R;
import com.intellicare.databinding.ActivitySearchBinding;
import com.intellicare.model.other.ComplaintData;
import com.intellicare.model.other.PrefConstants;
import com.intellicare.net.ServiceHelper;
import com.intellicare.utils.DialogUtil;
import com.intellicare.utils.KeyboardUtility;
import com.intellicare.utils.PreferenceUtil;
import com.intellicare.view.base.BaseActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends BaseActivity {
    private ActivitySearchBinding binding;
    private ComplaintListAdapter adapter;
    private ComplaintData complaintData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getComplaints();

        binding.llMain.setOnClickListener(v -> KeyboardUtility.hideKeyboard(SearchActivity.this, binding.llMain));
        binding.btnBack.setOnClickListener(v -> {
            setResult(RESULT_CANCELED, null);
            finish();
        });
    }

    private void initSearchView() {
        adapter = new ComplaintListAdapter(complaintData.getComplaintList());
        binding.lvComplaints.setAdapter(adapter);

        binding.searchComplaint.setActivated(true);
        binding.searchComplaint.setQueryHint(getString(R.string.allergy_search_hint));
        binding.searchComplaint.onActionViewExpanded();
        binding.searchComplaint.setIconified(false);
        binding.searchComplaint.clearFocus();

        binding.searchComplaint.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.e("Search", query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.e("Search", newText);
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        binding.lvComplaints.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (parent != null) {
                    ComplaintData.Complaint complaint = (ComplaintData.Complaint) parent.getItemAtPosition(position);
                    Log.e("Search", complaint.getComplaintName());
                    Intent i = new Intent();
                    i.putExtra("complaint", complaint.getComplaintName());
                    i.putExtra("complaint_id", complaint.getId());
                    setResult(RESULT_OK, i);
                    finish();
                }
            }
        });
    }

    private void getComplaints() {
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, SearchActivity.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, SearchActivity.this);
        DialogUtil.showProgressDialog(this, getResources().getString(R.string.common_loader_title), getString(R.string.please_wait));
        Call<ComplaintData> call = new ServiceHelper().getComplaints(patient_id, token);
        call.enqueue(new Callback<ComplaintData>() {
            @Override
            public void onResponse(@NonNull Call<ComplaintData> call, @NonNull Response<ComplaintData> response) {
                DialogUtil.hideProgressDialog();
                if(response.isSuccessful()) {
                    complaintData = response.body();
                    String responseStr = new Gson().toJson(complaintData);
                    Log.e("onResponse", responseStr);
                    initSearchView();
                } else {
                    Log.e("onResponse", "Failed");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ComplaintData> call, @NonNull Throwable t) {
                DialogUtil.hideProgressDialog();
                Log.e("onFailure", t.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED, null);
        super.onBackPressed();
    }
}