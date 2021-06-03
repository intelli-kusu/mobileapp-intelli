package com.intellicare.view.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.intellicare.R;
import com.intellicare.databinding.ActivitySettingsBinding;
import com.intellicare.model.other.PrefConstants;
import com.intellicare.utils.LocaleManager;
import com.intellicare.utils.PreferenceUtil;
import com.intellicare.utils.Utils;
import com.intellicare.view.base.BaseActivity;
import com.intellicare.view.dashboard.DashboardActivity;
import com.intellicare.view.login.LoginActivity;

public class SettingsActivity extends BaseActivity implements View.OnClickListener {
    private ActivitySettingsBinding binding;
    private String appLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvLogout.setOnClickListener(this);
        binding.btnBack.setOnClickListener(this);
        binding.tvContactUs.setOnClickListener(this);
        binding.tvTermsAndConditions.setOnClickListener(this);
        binding.tvHealthConsent.setOnClickListener(this);
        doLanguageSetup();
    }

    private void doLanguageSetup() {
        appLanguage = LocaleManager.getLanguage(this);
        Log.e("radio", "appLanguage " + appLanguage);
        if (appLanguage.equalsIgnoreCase(LocaleManager.SPANISH)) {
            binding.rbEspanol.setChecked(true);
        } else {
            binding.rbEnglish.setChecked(true);
        }
        binding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Log.e("radio", "called " + checkedId);
            if (checkedId == R.id.rbEnglish) {
                Log.e("radio", "rbEnglish " + checkedId);
                appLanguage = LocaleManager.ENGLISH;
                LocaleManager.setAppLocale(SettingsActivity.this, appLanguage);
                appLanguage = LocaleManager.getLanguage(this);
                Log.e("radio", "appLanguagexxxxxx " + appLanguage);
                recreate();
            } else if (checkedId == R.id.rbEspanol) {
                Log.e("radio", "rbEspanol " + checkedId);
                appLanguage = LocaleManager.SPANISH;
                LocaleManager.setAppLocale(SettingsActivity.this, appLanguage);
                appLanguage = LocaleManager.getLanguage(this);
                Log.e("radio", "appLanguageyyy " + appLanguage);
                recreate();
            }

        });
    }

    private void logout() {
        PreferenceUtil.saveBoolean(PrefConstants.PREF_IS_USER_LOGGED_IN, false, SettingsActivity.this);
        Utils.toast(getString(R.string.logout_message), SettingsActivity.this);
        startActivity(new Intent(SettingsActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tvLogout) {
            logout();
        } else if (view.getId() == R.id.btnBack) {
            moveToDashboard();
        } else if (view.getId() == R.id.tvHealthConsent) {
        } else if (view.getId() == R.id.tvTermsAndConditions) {
        } else if (view.getId() == R.id.tvContactUs) {
        }
    }

    private void moveToDashboard() {
        if (isLanguageChanged()) {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            finish();
        }
    }

    private boolean isLanguageChanged() {
        return !(LocaleManager.mainAppLang.equalsIgnoreCase(LocaleManager.getLanguage(this)));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveToDashboard();
    }
}