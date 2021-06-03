package com.intellicare.view.login;

import android.os.Bundle;

import com.intellicare.databinding.ActivityAboutBinding;
import com.intellicare.view.base.BaseActivity;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAboutBinding binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.back.setOnClickListener(v -> finish());
    }
}