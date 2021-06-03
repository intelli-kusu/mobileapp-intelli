package com.intellicare.view.form;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.intellicare.databinding.ActivityFullImageBinding;
import com.intellicare.view.base.BaseActivity;

public class FullImageActivity extends BaseActivity {
    private ActivityFullImageBinding binding;
    private ScaleGestureDetector scaleGestureDetector;
    private float mScaleFactor = 4.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        scaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 10.0f));
            binding.imageView.setScaleX(mScaleFactor);
            binding.imageView.setScaleY(mScaleFactor);
            return true;
        }
    }
}