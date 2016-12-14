package com.juniperphoton.myersplash.widget;


import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.juniperphoton.myersplash.R;

public class ProgressView extends View {

    private int mColor;
    private int mProgress;

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ProgressView, 0, 0);
        mColor = array.getInt(R.styleable.ProgressView_BackgroundColor, Color.WHITE);
        mProgress = array.getInt(R.styleable.ProgressView_Progress, 0);
        array.recycle();
    }

    public void setProgress(int progress) {
        mProgress = progress;
        invalidate();
    }

    public void setThemeColor(int color) {
        mColor = color;
    }

    public void animateProgressTo(int progress) {
        ValueAnimator animator = ValueAnimator.ofInt(mProgress, progress);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                setProgress((int) valueAnimator.getAnimatedValue());
            }
        });
        animator.start();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        Paint paint = new Paint();
        paint.setColor(mColor);

        int width = getWidth();
        int progressWidth = (int) (width * (mProgress / 100d));

        canvas.drawRect(0, 0, progressWidth, getHeight(), paint);
    }
}
