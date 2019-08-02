package com.kakaopay.kidongyun;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class YunAnimation extends Animation {

    // Class : YunAnimation.
    // Description : Animation 클래스를 활용하여 Custom Animation을 제작하기 위한 클래스.
    private View view;
    private Context context;

    private float fromWidth;
    private float fromHeight;
    private float toWidth;
    private float toHeight;

    private int fromPadding;
    private int toPadding;

    public YunAnimation(View view, Context context, int duration) {
        this.view = view;
        this.context = context;

        this.fromWidth = view.getWidth();
        this.toWidth = view.getWidth();
        this.fromHeight = view.getHeight();
        this.toHeight = dipToPixels(context, 50);

        this.fromPadding = view.getPaddingLeft();
        this.toPadding = 0;

        setDuration(duration);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);

        float height = (toHeight - fromHeight) * interpolatedTime + fromHeight;
        float width = (toWidth - fromWidth) * interpolatedTime + fromWidth;

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        layoutParams.height = (int) height;
        layoutParams.width = (int) width;

        view.setPadding(Math.round((toPadding - fromPadding) * interpolatedTime + fromPadding), 0, Math.round((toPadding - fromPadding) * interpolatedTime + fromPadding), 0);
        view.requestLayout();

    }

    public float dipToPixels(Context context, float dipValue){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,  dipValue, metrics);
    }
}
