package com.vbank.vidyovideoview.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class DragFrameLayout extends FrameLayout {
    private int prevY;
    private int prevX;
    private float dX;
    private float dY;

    private static final float SCROLL_THRESHOLD = 10;

    private IVideoFrameListener mListener;

    private float mDownX;
    private float mDownY;

    private boolean isOnClick;

    public DragFrameLayout(Context context) {
        super(context);
    }

    public DragFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DragFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void Register(IVideoFrameListener listener) {
        mListener = listener;
    }

    //  @Override
    //   public boolean onTouch(final View v, final MotionEvent event)
    //   {
//        final FrameLayout.LayoutParams par=(FrameLayout.LayoutParams)v.getLayoutParams();
//        switch(event.getAction())
//        {
//            case MotionEvent.ACTION_MOVE:
//            {
//                par.topMargin+=(int)event.getRawY()-prevY;
//                prevY=(int)event.getRawY();
//                par.leftMargin+=(int)event.getRawX()-prevX;
//                prevX=(int)event.getRawX();
//                v.setLayoutParams(par);
//                return true;
//            }
//            case MotionEvent.ACTION_UP:
//            {
//                par.topMargin+=(int)event.getRawY()-prevY;
//                par.leftMargin+=(int)event.getRawX()-prevX;
//                v.setLayoutParams(par);
//                return true;
//            }
//            case MotionEvent.ACTION_DOWN:
//            {
//                prevX=(int)event.getRawX();
//                prevY=(int)event.getRawY();
//                par.bottomMargin=-2*v.getHeight();
//                par.rightMargin=-2*v.getWidth();
//                v.setLayoutParams(par);
//                return true;
//            }
//        }
//        return false;
    //   }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int width = this.getLayoutParams().width;

        int height = this.getLayoutParams().height;
        switch (event.getAction()) {

            case MotionEvent.ACTION_UP:
                if (isOnClick) {
                    performClick();
                } else {
                    int a = Math.round((float) getScreenWidth() / 2);

                    if (getX() + ((float) width / 2) < a) {
                        this.animate()
                                .x(0)
                                .y(this.getY())
                                .setDuration(100)
                                .start();
                    } else {
                        this.animate()
                                .x(getScreenWidth() - width)
                                .y(this.getY())
                                .setDuration(100)
                                .start();
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:

                mDownX = event.getX();
                mDownY = event.getY();
                isOnClick = true;

                dX = this.getX() - event.getRawX();
                dY = this.getY() - event.getRawY();

                return true;

            case MotionEvent.ACTION_MOVE:
                if (isOnClick && (Math.abs(mDownX - event.getX()) > SCROLL_THRESHOLD || Math.abs(mDownY - event.getY()) > SCROLL_THRESHOLD)) {
                    isOnClick = false;
                }

                if (width == getScreenWidth() && height == getScreenHeight()) {
                    return false;
                } else {
                    this.animate()
                            .x(event.getRawX() + dX)
                            .y(event.getRawY() + dY)
                            .setDuration(0)
                            .start();

                    if (event.getRawX() + dX + width > getScreenWidth()) {
                        this.animate()
                                .x(getScreenWidth() - width)
                                .setDuration(0)
                                .start();
                    }
                    if (event.getRawX() + dX < 0) {
                        this.animate()
                                .x(0)
                                .setDuration(0)
                                .start();
                    }
                    if (event.getRawY() + dY + height > getScreenHeight()) {
                        this.animate()
                                .y(getScreenHeight() - height)
                                .setDuration(0)
                                .start();
                    }
                    if (event.getRawY() + dY < 0) {
                        this.animate()
                                .y(0)
                                .setDuration(0)
                                .start();
                    }

                    return true;
                }
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    public int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }


    // Because we call this from onTouchEvent, this code will be executed for both
    // normal touch events and for when the system calls this using Accessibility
    @Override
    public boolean performClick() {
        super.performClick();
        mListener.onVideoFrameClicked();
        return true;
    }

}

