package com.vbank.vidyovideoview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class VideoFrameLayout extends FrameLayout {

    public VideoFrameLayout(@NonNull Context context) {
        super(context);
    }

    public VideoFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

        @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
}