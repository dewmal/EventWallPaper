package com.juniperphoton.myersplash.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class VariableSizeLayout extends ViewGroup {
    public VariableSizeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private int widthM;
    private int heightM;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        widthM = widthMeasureSpec;
        heightM = heightMeasureSpec;
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            childView.measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getWidth();
        int widthSum = 0;
        int heightOffset = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            int targetWidth = widthSum + childView.getMeasuredWidth();
            if (targetWidth <= width) {
                childView.layout(widthSum, heightOffset, targetWidth, heightOffset + childView.getHeight());
            }
        }
    }
}
