package com.kakaopay.kidongyun;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class YunScrollAnimation {
    public static int SENSITIVITY = 5;

    private View targetView;
    private Context context;

    private int fromValue;
    private int toValue;

    private int state;

    public YunScrollAnimation(View targetView, Context context, float fromValue, float toValue) {
        this.targetView = targetView;
        this.context = context;

        this.fromValue = dpToPixels(context, fromValue);
        this.toValue = dpToPixels(context, toValue);
    }

    private int dpToPixels(Context context, float dpValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float temp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, metrics);
        return Math.round(temp);
    }

    public void start(int state) {
        this.state = state;
        android.view.ViewGroup.LayoutParams targetViewLayoutParams = targetView.getLayoutParams();

        if(fromValue > toValue) {
            if(targetViewLayoutParams.height >= toValue + SENSITIVITY) {
                targetViewLayoutParams.height = targetViewLayoutParams.height - SENSITIVITY;
            } else if(targetViewLayoutParams.height < (toValue + SENSITIVITY) && targetViewLayoutParams.height > toValue) {
                targetViewLayoutParams.height = toValue;
            }
        } else {
            if(targetViewLayoutParams.height <= toValue - SENSITIVITY) {
                targetViewLayoutParams.height = targetViewLayoutParams.height + SENSITIVITY;
            } else if(targetViewLayoutParams.height > (toValue + SENSITIVITY) && targetViewLayoutParams.height < toValue) {
                targetViewLayoutParams.height = toValue;
            }
        }

        targetView.setLayoutParams(targetViewLayoutParams);
    }
    public void actionUp() {
        android.view.ViewGroup.LayoutParams targetViewLayoutParams = targetView.getLayoutParams();

        if (fromValue != toValue) {
            targetViewLayoutParams.height = toValue;

            if(state == 1) {
                targetView.setVisibility(View.GONE);
            }
        }

        targetView.setLayoutParams(targetViewLayoutParams);
    }

    public View getTargetView() { return targetView; }
}

class YunScrollAnimationController {
    private final int SCROLL_UP_STATE = 1;
    private final int SCROLL_DOWN_STATE = 2;
    private final int NULL_STATE = 16;

    private View controllerView;
    private int action_up_sensitivity = 30;

    private int y = 0;
    private int prevY;

    private int state = NULL_STATE;
    private int stateValueY = 0;

    private boolean IS_FIRST_FLAG = false;

    private YunScrollAnimation scrollUpAnimation;
    private YunScrollAnimation scrollDownAnimation;

    public YunScrollAnimationController(View controllerView) { this.controllerView = controllerView; }

    public void addScrollUpAnimation(YunScrollAnimation scrollUpAnimation) { this.scrollUpAnimation = scrollUpAnimation; }
    public void addScrollDownAnimation(YunScrollAnimation scrollDownAnimation) { this.scrollDownAnimation = scrollDownAnimation; }

    public void start() {
        onListening();
    }

    private void onListening() {
        controllerView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_MOVE:

                        if (!IS_FIRST_FLAG) {
                            setY(motionEvent);
                            IS_FIRST_FLAG = true;
                        } else {
                            setPrevY();
                            setY(motionEvent);
                            setState();
                        }

                        if (prevY > y + YunScrollAnimation.SENSITIVITY) {
                            scrollUp();
                        } else if (prevY + YunScrollAnimation.SENSITIVITY < y) {
                            scrollDown();
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        if (state == SCROLL_UP_STATE) {
                            if (scrollUpAnimation != null) { scrollUpAnimation.actionUp(); }
                        } else if (state == SCROLL_DOWN_STATE) {
                            if (scrollDownAnimation != null) { scrollDownAnimation.actionUp(); }
                        }

                        y = 0;
                        prevY = 0;
                        state = NULL_STATE;
                        stateValueY = 0;
                        IS_FIRST_FLAG = false;
                        break;
                }

                return false;
            }
        });
    }

    private void scrollUp() {
        if (scrollUpAnimation == null) { return; }
        scrollUpAnimation.start(SCROLL_UP_STATE);
    }

    private void scrollDown() {
        if (scrollDownAnimation == null) { return; }
        scrollDownAnimation.getTargetView().setVisibility(View.VISIBLE);
        scrollDownAnimation.start(SCROLL_DOWN_STATE);
    }

    private void setY(MotionEvent motionEvent) {
        this.y = (int) motionEvent.getRawY();
    }

    private void setPrevY() {
        this.prevY = y;
    }

    private void setState() {
        stateValueY = stateValueY + (y - prevY);

        if (stateValueY > action_up_sensitivity) {
            state = SCROLL_DOWN_STATE;
        } else if (stateValueY < -action_up_sensitivity) {
            state = SCROLL_UP_STATE;
        }
    }
}

