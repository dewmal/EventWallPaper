package com.juniperphoton.myersplash.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.View;

public class RingProgressView extends View {
    private static final int STROKE_WIDTH = 5;

    private Paint mPaint;
    private int mProgress = 10;
    private int mColor = Color.WHITE;
    private RectF mRect;

    private Context mContext;

    public RingProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        mPaint = new Paint();
        mPaint.setColor(mColor);
        mPaint.setStrokeWidth(STROKE_WIDTH);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        mRect = new RectF();
    }

    public void setProgress(final int value) {
        mProgress = value;
        invalidate();
    }

    public void setColor(@ColorInt int color) {
        mColor = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int center = (int) (getWidth() / 2f);
        int radius = (int) (center - STROKE_WIDTH / 2f);

        mPaint.setColor(mColor);
        mRect.set(center - radius, center - radius, center + radius, center + radius);
        int angle = (int) (360 * mProgress / 100f);
        canvas.drawArc(mRect, -90, angle, false, mPaint);
    }
}

