package com.kakaopay.kidongyun;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class YunScrollAnimation {

    // Class : YunScrollAnimation
    // Description : 스크롤 제스쳐를 기반으로 애니메이션이 동작하도록 구현한 클래스

    // 스크롤 제스처와 애니메이션 간의 감도를 의미
    public static int SENSITIVITY = 5;

    // 애니메이션 대상이 되는 뷰
    private View targetView;

    // 애니메이션의 시작값과 끝값
    private int fromValue;
    private int toValue;

    private int state;

    public YunScrollAnimation(View targetView, Context context, float fromValue, float toValue) {
        this.targetView = targetView;
        this.fromValue = dpToPixels(context, fromValue);
        this.toValue = dpToPixels(context, toValue);
    }

    private int dpToPixels(Context context, float dpValue) {
        // dp 수치를 pixel로 변환하는 함수.
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float temp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, metrics);
        return Math.round(temp);
    }

    public void start(int state) {
        // 실제적으로 애니메이션을 구현하는 함수.

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
        // 스크롤 제스처 중 손을 놓았을 때 처리를 경우를 위한 함수.
        android.view.ViewGroup.LayoutParams targetViewLayoutParams = targetView.getLayoutParams();

        if (fromValue != toValue) {
            targetViewLayoutParams.height = toValue;

            if(state == YunScrollAnimationController.SCROLL_UP_STATE) {
                targetView.setVisibility(View.GONE);
            }
        }

        targetView.setLayoutParams(targetViewLayoutParams);
    }

    public View getTargetView() { return targetView; }
}

class YunScrollAnimationController {

    // Class : YunScrollAnimationController
    // Description : Scroll Animation 에서 제스처 부분을 컨트롤하기 위한 클래스.

    public static final int SCROLL_UP_STATE = 1;
    public static final int SCROLL_DOWN_STATE = 2;
    public static final int NULL_STATE = 16;

    // 애니메이션을 위해 컨트롤러가 되는 뷰
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
        // 컨트롤러 뷰의 setOnTouchListener()를 통해서 스크롤 제스처를 기다리는 함수를 만든다.
        controllerView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        // 스크롤 할 때
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
                        // 손을 놓았을 때
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

