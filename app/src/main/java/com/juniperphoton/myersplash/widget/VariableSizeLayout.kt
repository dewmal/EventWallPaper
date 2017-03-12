package com.juniperphoton.myersplash.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

class VariableSizeLayout(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {
    private var widthM: Int = 0
    private var heightM: Int = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        widthM = widthMeasureSpec
        heightM = heightMeasureSpec
        (0..childCount - 1)
                .map { getChildAt(it) }
                .forEach { it.measure(widthMeasureSpec, heightMeasureSpec) }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = width
        val widthSum = 0
        val heightOffset = 0
        for (i in 0..childCount - 1) {
            val childView = getChildAt(i)
            val targetWidth = widthSum + childView.measuredWidth
            if (targetWidth <= width) {
                childView.layout(widthSum, heightOffset, targetWidth, heightOffset + childView.height)
            }
        }
    }
}
