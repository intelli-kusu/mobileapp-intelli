package com.intellicare.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import com.intellicare.model.other.PrefConstants;

import java.util.Locale;

public class LocaleManager {
    public static final String ENGLISH = "en";
    public static final String SPANISH = "es";
    public static String mainAppLang = "";

    public static Context setAppLocale(Context c, String language) {
        saveLanguage(c, language);
        return updateResources(c, language);
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        context = context.createConfigurationContext(config);
        return context;
    }

    @SuppressLint("ApplySharedPref")
    private static void saveLanguage(Context context, String language) {
        PreferenceUtil.saveData(PrefConstants.PREF_APP_LANGUAGE, language, context);
    }

    public static String getLanguage(Context context) {
        String language = PreferenceUtil.getData(PrefConstants.PREF_APP_LANGUAGE, context);
        return TextUtils.isEmpty(language) ? ENGLISH : language;
    }
}