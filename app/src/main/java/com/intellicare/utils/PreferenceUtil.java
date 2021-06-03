package com.intellicare.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtil {
    private static final String PREFS_NAME = "LANGUAGE_PREFS";

    public static void saveData(String key, String data, Context context) {
        if (null != data) {
            SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, data);
            editor.commit();
        }
    }

    public static String getData(String key, Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(key, "");
    }

    public static void saveBoolean(String key, Boolean data, Context context) {
        if (null != data) {
            SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(key, data);
            editor.apply();
        }
    }

    public static boolean getBoolean(String key, Boolean defaultValue, Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(key, defaultValue);
    }

    public static boolean getBoolean(String key, Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(key, false);
    }
}
