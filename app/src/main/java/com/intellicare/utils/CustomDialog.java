package com.intellicare.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.LinearLayout;

/**
 * class to create the Custom Dialog
 **/
public class CustomDialog extends Dialog {
    /**
     * Constructor
     *
     * @param context
     * @param view
     */
    public CustomDialog(Context context, View view) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(view);
    }

    /**
     * Constructor
     *
     * @param context
     * @param view
     * @param lpW
     * @param lpH
     */
    public CustomDialog(Context context, View view, int lpW, int lpH) {
        this(context, view, lpW, lpH, true);
    }

    /**
     * Constructor
     *
     * @param context
     * @param view
     * @param lpW
     * @param lpH
     * @param isCancellable
     */
    public CustomDialog(Context context, View view, int lpW, int lpH, boolean isCancellable) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(view, new LayoutParams(lpW, lpH));

        this.setCancelable(isCancellable);
    }

    /**
     * Constructor for Animating the Dialog
     *
     * @param context       : context of activity
     * @param view          : The view which should render in Dialog window
     * @param width         : Width of View
     * @param height        : Height of View
     * @param isCancellable : If true it can be cancelled.
     * @param theme         : Theme for animating the Dialog
     * @author Vinod Kumar Kusu
     */

    public CustomDialog(Context context, View view, int width, int height, boolean isCancellable, int theme) {
        super(context, theme);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
//		params.gravity=Gravity.BOTTOM;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(view, params);

        this.setCancelable(isCancellable);
    }
}
