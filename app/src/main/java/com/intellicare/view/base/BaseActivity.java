package com.intellicare.view.base;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.intellicare.R;
import com.intellicare.utils.LocaleManager;

abstract public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "Base";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
    }

    public void showAlertDialog(String title, String message, String firstBtn, String secondBtn, boolean isCancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(title)
                .setMessage(message)
                .setPositiveButton(firstBtn, (dialog, which) -> {
                    onPositiveClick();
                })
                .setNegativeButton(secondBtn, (dialog, which) -> {
                    onNegativeClick();
                })
                .setCancelable(isCancelable);

        builder.show();
    }

    public void showPickerDialog(String title) {
        CharSequence[] items = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(title)
                .setIcon(R.drawable.ic_menu_gallery)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            onPositiveClick(); //camera
                        } else if (which == 1) {
                            onNegativeClick(); //Gallery
                        }
                    }
                });

        builder.show();
    }

    public void showPickerDialog(String title, boolean isDeleteOptionAvailable) {
        if (isDeleteOptionAvailable) {
            CharSequence[] items = {"Delete Photo","Camera", "Gallery"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(title)
                    .setIcon(R.drawable.ic_menu_gallery)
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                onDeletePhotoClick(); //delete photo
                            } else if (which == 1) {
                                onPositiveClick(); //camera
                            } else if (which == 2) {
                                onNegativeClick(); //Gallery
                            }
                        }
                    });
            builder.show();
        } else {
            this.showPickerDialog(title);
        }
    }

    protected void onDeletePhotoClick() {
        Log.i(TAG, "delete photo click");
    }

    protected void onPositiveClick() {
        Log.i(TAG, "positive click");
    }

    protected void onNegativeClick() {
        Log.i(TAG, "negative click");
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setAppLocale(newBase, LocaleManager.getLanguage(newBase)));
        Log.e("Base", "attachBaseContext");
    }
}