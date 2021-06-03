package com.intellicare.utils;

import android.app.ProgressDialog;
import android.content.Context;

public class DialogUtil {
    private static ProgressDialog progressDialog;

    public static void showProgressDialog(Context context, String title, String message) {
        try {
            if (null == progressDialog) {
                progressDialog = new ProgressDialog(context);
            }

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
            progressDialog.setIndeterminate(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hideProgressDialog() {
        try {
            if (null != progressDialog || progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}