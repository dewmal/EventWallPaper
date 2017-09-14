package com.juniperphoton.myersplash.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import com.juniperphoton.myersplash.R

class FixedSizeCardView(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {
    companion object {
        const val INVALID_RATIO = -1f
    }

    var ratio: Float = INVALID_RATIO

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.FixedSizeCardView)
        ratio = array.getFloat(R.styleable.FixedSizeCardView_aspect_ratio, INVALID_RATIO)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (ratio == INVALID_RATIO) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            val height = width / ratio
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height.toInt(), MeasureSpec.EXACTLY))
        }
    }
}