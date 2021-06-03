package com.intellicare.view.visit;

import android.os.Bundle;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.intellicare.R;
import com.intellicare.databinding.ActivityPastVisitsBinding;
import com.intellicare.model.other.PrefConstants;
import com.intellicare.model.pastvisits.Consult;
import com.intellicare.model.pastvisits.PastVisitsResponse;
import com.intellicare.net.ServiceHelper;
import com.intellicare.utils.DialogUtil;
import com.intellicare.utils.PreferenceUtil;
import com.intellicare.utils.Utils;
import com.intellicare.view.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PastVisitsActivity extends BaseActivity {
    private static final String TAG = "PastVisitsActivity";
    private ActivityPastVisitsBinding binding;
    private PastVisitsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPastVisitsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.back.setOnClickListener(v -> finish());
        setRecyclerView();
//        updateMockUI();
    }

    private void loadFirstPage() {
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, PastVisitsActivity.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, PastVisitsActivity.this);
        DialogUtil.showProgressDialog(this, getResources().getString(R.string.common_loader_title), getString(R.string.please_wait));

        Call<PastVisitsResponse> call = new ServiceHelper().getPastVisits(patient_id, "1", token);
        call.enqueue(new Callback<PastVisitsResponse>() {
            @Override
            public void onResponse(@NonNull Call<PastVisitsResponse> call, @NonNull Response<PastVisitsResponse> response) {
                DialogUtil.hideProgressDialog();
                PastVisitsResponse result = response.body();
                if (response.isSuccessful() && result != null) {
                    if(result.getStatus().equalsIgnoreCase("failure")) {
                        binding.tvError.setVisibility(View.VISIBLE);
                        binding.recyclerView.setVisibility(View.GONE);
                    } else {
                        binding.tvError.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        totalPages = result.getTotalPages();
                        currentPage = result.getCurrentPage();
                        updateListView(result, true);
                    }
                } else {
                    Log.e(TAG, "Unsuccessful");
                    binding.tvError.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PastVisitsResponse> call, @NonNull Throwable t) {
                DialogUtil.hideProgressDialog();
                Utils.toast("Network error", PastVisitsActivity.this);
//                finish();
            }
        });
    }

    private void loadNextPage() {
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, PastVisitsActivity.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, PastVisitsActivity.this);
        DialogUtil.showProgressDialog(this, getResources().getString(R.string.common_loader_title), getString(R.string.please_wait));

        Call<PastVisitsResponse> call = new ServiceHelper().getPastVisits(patient_id, currentPage+"", token);
        call.enqueue(new Callback<PastVisitsResponse>() {
            @Override
            public void onResponse(@NonNull Call<PastVisitsResponse> call, @NonNull Response<PastVisitsResponse> response) {
                DialogUtil.hideProgressDialog();
                PastVisitsResponse result = response.body();
                if (response.isSuccessful() && result != null) {
                    isLoading = false;
                    isLastPage = currentPage == totalPages;
                    updateListView(result, false);
                } else {
                    Log.e(TAG, "Unsuccessful");
                    binding.tvError.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PastVisitsResponse> call, @NonNull Throwable t) {
                DialogUtil.hideProgressDialog();
                Utils.toast("Network error", PastVisitsActivity.this);
//                finish();
            }
        });
    }

    private void updateListView(PastVisitsResponse response, boolean isFirstPage) {
        if (response.getStatus().equalsIgnoreCase("success")) {
            adapter.addVisits(response.getConsults());
        } else {
            //show some error text
            if(isFirstPage) {
                binding.tvError.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.GONE);
            }
        }
    }

    //sample mock data
    private List<Consult> createMockVisits() {
        List<Consult> list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Consult c = new Consult();
            c.setClinicianName("Dr.Patrick " + i);
            c.setCreatedAt("17-Nov-2020 06:30PM");
            c.setComplaint("Headache");
            c.setNotes("Sample notes for this visit goes here");
            list.add(c);
        }
        return list;
    }

    //update mockUI
    private void updateMockUI() {
        List<Consult> visits = createMockVisits();
        adapter = new PastVisitsAdapter(visits);
        ((SimpleItemAnimator) binding.recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setHasFixedSize(true);
    }

    //pagination
    private boolean isLoading;
    private boolean isLastPage;
    private int totalPages;
    private int currentPage;

    private void setRecyclerView() {
        List<Consult> visits = new ArrayList<>();
        adapter = new PastVisitsAdapter(visits);
        ((SimpleItemAnimator) binding.recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
//        binding.recyclerView.setHasFixedSize(true);

        binding.recyclerView.addOnScrollListener(new PaginationScrollListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;
                loadNextPage();
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
        loadFirstPage();
    }
}