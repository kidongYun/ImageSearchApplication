package com.kakaopay.kidongyun;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class YunScrollAnimation {
    private View targetView;
    private int SENSITIVITY = 5;

    private int fromHeight;
    private int toHeight;

    public YunScrollAnimation(View targetView, Context context,int fromHeight, int toHeight) {
        this.targetView = targetView;

//        this.fromHeight = targetView.getHeight();
        this.fromHeight = Math.round(dipToPixels(context, fromHeight));
        this.toHeight = Math.round(dipToPixels(context, toHeight));
    }

    public void start() {

        android.view.ViewGroup.LayoutParams targetViewLayoutParams = targetView.getLayoutParams();

        if(fromHeight > toHeight) {
            if(targetViewLayoutParams.height >= toHeight + SENSITIVITY) {
                targetViewLayoutParams.height = targetViewLayoutParams.height - SENSITIVITY;
            } else if(targetViewLayoutParams.height < (toHeight + SENSITIVITY) && targetViewLayoutParams.height > toHeight) {
                targetViewLayoutParams.height = toHeight;
            }
        } else {
            if(targetViewLayoutParams.height <= toHeight - SENSITIVITY) {
                targetViewLayoutParams.height = targetViewLayoutParams.height + SENSITIVITY;
            } else if(targetViewLayoutParams.height > (toHeight + SENSITIVITY) && targetViewLayoutParams.height < toHeight) {
                targetViewLayoutParams.height = toHeight;
            }
        }

        targetView.setLayoutParams(targetViewLayoutParams);
    }

    public void actionUp() {
        android.view.ViewGroup.LayoutParams targetViewLayoutParams = targetView.getLayoutParams();

        if(fromHeight != toHeight) {
            targetViewLayoutParams.height = toHeight;
        }

        targetView.setLayoutParams(targetViewLayoutParams);
    }

    public void setWidthSensitivity(int sensitivity) {
        this.SENSITIVITY = sensitivity;
    }

    private float dipToPixels(Context context, float dipValue){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,  dipValue, metrics);
    }
}

class YunScrollAnimationController {

    private final int SCROLL_UP_STATE = 1;
    private final int SCROLL_DOWN_STATE = 2;
    private final int SCROLL_LEFT_STATE = 4;
    private final int SCROLL_RIGHT_STATE = 8;
    private final int NULL_STATE = 16;

    private View contorllerView;

    private int SENSITIVITY = 10;
    private int ACTION_UP_SENSITIVITY = 30;

    private int x = 0;
    private int y = 0;

    private int prevX;
    private int prevY;

    private int state = NULL_STATE;

    private int stateValueX = 0;
    private int stateValueY = 0;

    private boolean IS_FIRST_FLAG = false;

    private YunScrollAnimation scrollUpAnimation;
    private YunScrollAnimation scrollDownAnimation;

    public YunScrollAnimationController(View contorllerView) { this.contorllerView = contorllerView; }


    public void addAnimation(YunScrollAnimation scrollUpAnimation, YunScrollAnimation scrollDownAnimation) {
        this.scrollUpAnimation = scrollUpAnimation;
        this.scrollDownAnimation = scrollDownAnimation;
    }

    public void start() {
        onListening();
    }

    private void onListening() {
        contorllerView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_MOVE:

                        if (!IS_FIRST_FLAG) {
                            setXY(motionEvent);
                            IS_FIRST_FLAG = true;
                        } else {
                            setPrevXY();
                            setXY(motionEvent);
                            setState();
                        }

                        if (prevY > y + SENSITIVITY) {
                            scrollUp();
                        } else if (prevY + SENSITIVITY < y) {
                            scrollDown();
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        if (state == SCROLL_UP_STATE) {
                            if (scrollUpAnimation != null) { scrollUpAnimation.actionUp(); }
                        } else if (state == SCROLL_DOWN_STATE) {
                            if (scrollDownAnimation != null) { scrollDownAnimation.actionUp(); }
                        }

                        x = 0;
                        y = 0;
                        prevX = 0;
                        prevY = 0;
                        state = NULL_STATE;
                        stateValueX = 0;
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
        scrollUpAnimation.start();
    }

    private void scrollDown() {
        if (scrollDownAnimation == null) { return; }
        scrollDownAnimation.start();
    }

    private void setXY(MotionEvent motionEvent) {
        this.x = (int) motionEvent.getRawX();
        this.y = (int) motionEvent.getRawY();
    }

    private void setPrevXY() {
        this.prevX = x;
        this.prevY = y;
    }

    private void setState() {
        stateValueX = stateValueX + (x - prevX);
        stateValueY = stateValueY + (y - prevY);

        if (Math.abs(stateValueX) > Math.abs(stateValueY)) {
            if (stateValueX > ACTION_UP_SENSITIVITY) {
                state = SCROLL_RIGHT_STATE;
            } else if (stateValueX < -ACTION_UP_SENSITIVITY) {
                state = SCROLL_LEFT_STATE;
            }
        } else {
            if (stateValueY > ACTION_UP_SENSITIVITY) {
                state = SCROLL_DOWN_STATE;
            } else if (stateValueY < -ACTION_UP_SENSITIVITY) {
                state = SCROLL_UP_STATE;
            }
        }
    }

    public void setScrollSensitivity(int sensitivity) {
        this.SENSITIVITY = sensitivity;
    }
}