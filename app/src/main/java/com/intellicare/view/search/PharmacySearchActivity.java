package com.intellicare.view.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.intellicare.R;
import com.intellicare.databinding.ActivitySearchBinding;
import com.intellicare.model.other.PharmacyData;
import com.intellicare.net.ServiceHelper;
import com.intellicare.utils.DialogUtil;
import com.intellicare.utils.KeyboardUtility;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PharmacySearchActivity extends AppCompatActivity {
    private ActivitySearchBinding binding;
    private PharmacyListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initSearchView();
        binding.llMain.setOnClickListener(v -> KeyboardUtility.hideKeyboard(PharmacySearchActivity.this, binding.llMain));
        binding.btnBack.setOnClickListener(v -> {
            setResult(RESULT_CANCELED, null);
            finish();
        });
    }

    private void initSearchView() {
        TextView et = binding.searchComplaint.findViewById(R.id.search_src_text);
        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        binding.searchComplaint.setActivated(true);
        binding.searchComplaint.setQueryHint(getString(R.string.pharmacy_search_hint));
        binding.searchComplaint.onActionViewExpanded();
        binding.searchComplaint.setIconified(false);
        binding.searchComplaint.clearFocus();
        binding.searchComplaint.setInputType(InputType.TYPE_CLASS_NUMBER);

        binding.searchComplaint.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.e("Zip", query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.e("Zip", newText);
                if (newText.length() >= 3) {
                    getPharmacies(newText);
                } else {
                    updateSearchList(null);
                }

//                else {
//                    if (pharmacyData != null && pharmacyData.getPharmacyList() != null)
//                        pharmacyData.getPharmacyList().clear();
//                }
//                if (pharmacyData != null && pharmacyData.getPharmacyList() != null && adapter != null)
//                    adapter.refresh(pharmacyData.getPharmacyList());
                return false;
            }
        });

        binding.lvComplaints.setOnItemClickListener((parent, view, position, id) -> {
            if (parent != null) {
                PharmacyData.Pharmacy pharmacy = (PharmacyData.Pharmacy) parent.getItemAtPosition(position);
                Log.e("Zip", pharmacy.getZip());
                Intent i = new Intent();
                i.putExtra("pharmacy", pharmacy.getDisplayName());
                i.putExtra("pid", pharmacy.getId());
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }

    private void updateSearchList(PharmacyData pharmacyData) {
        if (adapter == null) {
            adapter = new PharmacyListAdapter(null);
            binding.lvComplaints.setAdapter(adapter);
        } else {
            if (pharmacyData != null && pharmacyData.getPharmacyList() != null)
                adapter.refresh(pharmacyData.getPharmacyList());
            else {
                adapter.refresh(null);
            }
        }
    }

    private void getPharmacies(String zip) {
        DialogUtil.showProgressDialog(this, getResources().getString(R.string.common_loader_title), getString(R.string.please_wait));

        Call<PharmacyData> call = new ServiceHelper().getPharmacies(zip);
        call.enqueue(new Callback<PharmacyData>() {
            @Override
            public void onResponse(@NonNull Call<PharmacyData> call, @NonNull Response<PharmacyData> response) {
                DialogUtil.hideProgressDialog();
                if (response.isSuccessful()) {
                    PharmacyData pharmacyData = response.body();
                    updateSearchList(pharmacyData);
                } else {
                    Log.e("onResponse", "Failed");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PharmacyData> call, @NonNull Throwable t) {
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